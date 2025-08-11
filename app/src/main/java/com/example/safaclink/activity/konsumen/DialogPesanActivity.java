package com.example.safaclink.activity.konsumen;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.example.safaclink.R;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.apiserver.PrefManager;
import com.example.safaclink.model.PaketModel;
import com.example.safaclink.model.TransaksiModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme;
import com.midtrans.sdk.corekit.models.BillingAddress;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.ShippingAddress;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class DialogPesanActivity extends AppCompatDialogFragment implements TransactionFinishedCallback {
    private Context context;
    private PaketModel paketModel;
    private PrefManager prefManager;
    private FusedLocationProviderClient fusedLocationClient;

    // UI Elements - menggunakan findViewById seperti dialog yang working
    private TextView tvNamaPaket, tvTipePaket, tvDeskripsiPaket, tvHargaPerUnit, tvHargaPaket, tvSatuanItem, tvTotalHarga, tvStatusLokasi;
    private EditText etAlamat, etJumlahItem;
    private Button btnUploadFoto, btnAmbilLokasi, btnMinusItem, btnPlusItem;
    private ImageView ivFotoBarang;

    // Image handling
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int REQUEST_PERMISSIONS = 3;
    private static final int REQUEST_LOCATION_PERMISSION = 4;
    private static final int REQUEST_READ_MEDIA_IMAGES = 5;
    private String currentPhotoPath;
    private Bitmap selectedImageBitmap;

    // Location
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String locationString = "";

    // Calculation
    private double hargaPerUnit = 0.0;
    private double jumlahItem = 1.0;
    private double totalHarga = 0.0;
    private String unitType = "Kg"; // Default

    // Midtrans
    private String orderId;
    private String snapToken;

    private static final String MIDTRANS_CLIENT_KEY = "Mid-client-kGBlxxZLts-Z70Vb";
    private static final String base_url = ApiServer.site_url_konsumen + "presponse.php/";
    private static final String URL_CREATE_TRANSAKSI = ApiServer.site_url_konsumen + "createTransaksi.php";

    // konstanta map picker
    private static final int REQUEST_MAP_PICKER = 6;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_dialog_pesan, null);

        context = getActivity();
        prefManager = new PrefManager(context);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Initialize UI elements
        initializeViews(view);

        // Initialize Midtrans
        initializeMidtrans();

        // Get paket data from arguments
        if (getArguments() != null) {
            paketModel = new PaketModel(
                    getArguments().getString("id_paket"),
                    getArguments().getString("nama_paket"),
                    getArguments().getString("tipe_paket"),
                    getArguments().getString("deskripsi"),
                    getArguments().getString("harga")
            );
        }

        setupViews();
        setupListeners();

        builder.setView(view)
                .setTitle("Pesan Paket")
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("Bayar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        processPembayaran();
                    }
                });

        return builder.create();
    }

    private void initializeViews(View view) {
        // Initialize semua view dengan findViewById
        tvNamaPaket = view.findViewById(R.id.tvNamaPaket);
        tvTipePaket = view.findViewById(R.id.tvTipePaket);
        tvDeskripsiPaket = view.findViewById(R.id.tvDeskripsiPaket);
        tvHargaPerUnit = view.findViewById(R.id.tvHargaPerUnit);
        tvHargaPaket = view.findViewById(R.id.tvHargaPaket);
        tvSatuanItem = view.findViewById(R.id.tvSatuanItem);
        tvTotalHarga = view.findViewById(R.id.tvTotalHarga);
        tvStatusLokasi = view.findViewById(R.id.tvStatusLokasi);

        etAlamat = view.findViewById(R.id.etAlamat);
        etJumlahItem = view.findViewById(R.id.etJumlahItem);

        btnUploadFoto = view.findViewById(R.id.btnUploadFoto);
        btnAmbilLokasi = view.findViewById(R.id.btnAmbilLokasi);
        btnMinusItem = view.findViewById(R.id.btnMinusItem);
        btnPlusItem = view.findViewById(R.id.btnPlusItem);

        ivFotoBarang = view.findViewById(R.id.ivFotoBarang);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, check which permission was requested
                    if (permissions[0].equals(Manifest.permission.CAMERA)) {
                        openCamera();
                    } else if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        openGallery();
                    }
                } else {
                    showErrorDialog("Permission ditolak. Tidak dapat mengakses " +
                            (permissions[0].equals(Manifest.permission.CAMERA) ? "kamera" : "galeri"));
                }
                break;

            case REQUEST_READ_MEDIA_IMAGES:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    showErrorDialog("Permission ditolak. Tidak dapat mengakses galeri");
                }
                break;

            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else {
                    showErrorDialog("Permission lokasi ditolak. Tidak dapat mengambil lokasi GPS");
                }
                break;
        }
    }

    private void initializeMidtrans() {
        SdkUIFlowBuilder.init()
                .setClientKey(MIDTRANS_CLIENT_KEY)
                .setContext(context)
                .setTransactionFinishedCallback(this)
                .setMerchantBaseUrl(base_url)
                .enableLog(true)
                .setColorTheme(new CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
                .buildSDK();
    }

    private void setupViews() {
        if (paketModel != null) {
            tvNamaPaket.setText(paketModel.getNama_paket());
            tvTipePaket.setText(paketModel.getTipe_paket());
            tvDeskripsiPaket.setText(paketModel.getDeskripsi());

            // Parse harga
            try {
                hargaPerUnit = Double.parseDouble(paketModel.getHarga());
                tvHargaPerUnit.setText(formatRupiah(hargaPerUnit));
                tvHargaPaket.setText(formatRupiah(hargaPerUnit));
            } catch (NumberFormatException e) {
                hargaPerUnit = 0.0;
            }

            // Set unit type based on package type
            String tipePackage = paketModel.getTipe_paket().toLowerCase();
            if (tipePackage.contains("sofa")) {
                unitType = "Unit";
            } else if (tipePackage.contains("karpet") || tipePackage.contains("gorden")) {
                unitType = "Kg";
            } else {
                unitType = "Unit"; // Default untuk paket lainnya
            }
            tvSatuanItem.setText("Satuan: " + unitType);

            calculateTotal();
        }
    }

    private void setupListeners() {
        // Upload foto
        btnUploadFoto.setOnClickListener(v -> showImagePickerDialog());

        // Location - ubah ini untuk membuka map picker
        btnAmbilLokasi.setOnClickListener(v -> openMapPicker());

        // Item quantity
        btnMinusItem.setOnClickListener(v -> {
            try {
                double current = Double.parseDouble(etJumlahItem.getText().toString());
                if (current > 1) {
                    current -= 1;
                    etJumlahItem.setText(String.valueOf(current));
                }
            } catch (NumberFormatException e) {
                etJumlahItem.setText("1");
            }
        });

        btnPlusItem.setOnClickListener(v -> {
            try {
                double current = Double.parseDouble(etJumlahItem.getText().toString());
                current += 1;
                etJumlahItem.setText(String.valueOf(current));
            } catch (NumberFormatException e) {
                etJumlahItem.setText("1");
            }
        });

        // Text watcher for quantity
        etJumlahItem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void openMapPicker() {
        Intent intent = new Intent(context, MapPickerActivity.class);

        // Kirim current location jika sudah ada
        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            intent.putExtra("current_lat", currentLatitude);
            intent.putExtra("current_lng", currentLongitude);
        }

        startActivityForResult(intent, REQUEST_MAP_PICKER);
    }

    private void calculateTotal() {
        try {
            String jumlahText = etJumlahItem.getText().toString().trim();
            if (jumlahText.isEmpty()) {
                jumlahText = "1";
                etJumlahItem.setText(jumlahText);
            }

            jumlahItem = Double.parseDouble(jumlahText);
            if (jumlahItem <= 0) {
                jumlahItem = 1.0;
                etJumlahItem.setText("1");
            }

            totalHarga = hargaPerUnit * jumlahItem;
            tvTotalHarga.setText(formatRupiah(totalHarga));

        } catch (NumberFormatException e) {
            jumlahItem = 1.0;
            etJumlahItem.setText("1");
            totalHarga = hargaPerUnit;
            tvTotalHarga.setText(formatRupiah(totalHarga));
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Pilih Foto")
                .setItems(new String[]{"Kamera", "Galeri"}, (dialog, which) -> {
                    if (which == 0) {
                        if (checkCameraPermission()) {
                            openCamera();
                        }
                    } else {
                        if (checkStoragePermission()) {
                            openGallery();
                        }
                    }
                })
                .show();
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ menggunakan READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_MEDIA_IMAGES);
                return false;
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Android 6-12 menggunakan READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("Camera", "Error creating file", ex);
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context, "com.example.safaclink.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");

            // Tambahkan fallback jika ACTION_PICK tidak tersedia
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
            } else {
                // Fallback menggunakan ACTION_GET_CONTENT
                Intent fallbackIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fallbackIntent.setType("image/*");
                fallbackIntent.addCategory(Intent.CATEGORY_OPENABLE);

                if (fallbackIntent.resolveActivity(context.getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(fallbackIntent, "Pilih Gambar"), REQUEST_IMAGE_GALLERY);
                } else {
                    showErrorDialog("Tidak ada aplikasi galeri yang tersedia");
                }
            }
        } catch (Exception e) {
            Log.e("Gallery", "Error opening gallery: " + e.getMessage());
            showErrorDialog("Gagal membuka galeri: " + e.getMessage());
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void getCurrentLocation() {
        // Coba GPS terlebih dahulu
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Jika tidak ada permission, langsung buka map picker
            openMapPicker();
            return;
        }

        tvStatusLokasi.setText("Mengambil lokasi...");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        locationString = currentLatitude + "," + currentLongitude;
                        tvStatusLokasi.setText("Lokasi GPS berhasil diambil");
                        tvStatusLokasi.setTextColor(ContextCompat.getColor(context, R.color.success_color));
                    } else {
                        // Jika GPS gagal, buka map picker sebagai fallback
                        tvStatusLokasi.setText("GPS tidak tersedia, pilih lokasi manual");
                        tvStatusLokasi.setTextColor(ContextCompat.getColor(context, R.color.warning_color));
                        openMapPicker();
                    }
                })
                .addOnFailureListener(e -> {
                    // Jika GPS gagal, buka map picker sebagai fallback
                    tvStatusLokasi.setText("GPS gagal, pilih lokasi manual");
                    tvStatusLokasi.setTextColor(ContextCompat.getColor(context, R.color.warning_color));
                    openMapPicker();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (currentPhotoPath != null) {
                        try {
                            selectedImageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
                            if (selectedImageBitmap != null) {
                                ivFotoBarang.setImageBitmap(selectedImageBitmap);
                                Log.d("Camera", "Image captured successfully");
                            } else {
                                showErrorDialog("Gagal memuat foto dari kamera");
                            }
                        } catch (Exception e) {
                            Log.e("Camera", "Error loading camera image: " + e.getMessage());
                            showErrorDialog("Gagal memuat foto dari kamera");
                        }
                    }
                    break;

                case REQUEST_IMAGE_GALLERY:
                    if (data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                            if (selectedImageBitmap != null) {
                                ivFotoBarang.setImageBitmap(selectedImageBitmap);
                                Log.d("Gallery", "Image selected successfully");
                            } else {
                                showErrorDialog("Gagal memuat gambar dari galeri");
                            }
                        } catch (IOException e) {
                            Log.e("Gallery", "Error loading gallery image: " + e.getMessage());
                            showErrorDialog("Gagal memuat gambar dari galeri: " + e.getMessage());
                        } catch (SecurityException e) {
                            Log.e("Gallery", "Security error loading gallery image: " + e.getMessage());
                            showErrorDialog("Tidak memiliki izin untuk mengakses gambar ini");
                        }
                    } else {
                        Log.e("Gallery", "No data received from gallery");
                        showErrorDialog("Tidak ada gambar yang dipilih");
                    }
                    break;

                // Tambahkan case baru untuk map picker
                case REQUEST_MAP_PICKER:
                    if (data != null) {
                        double selectedLat = data.getDoubleExtra("selected_lat", 0.0);
                        double selectedLng = data.getDoubleExtra("selected_lng", 0.0);
                        String selectedAddress = data.getStringExtra("selected_address");

                        if (selectedLat != 0.0 && selectedLng != 0.0) {
                            currentLatitude = selectedLat;
                            currentLongitude = selectedLng;
                            locationString = currentLatitude + "," + currentLongitude;

                            // Update status lokasi
                            tvStatusLokasi.setText("Lokasi dipilih");
                            tvStatusLokasi.setTextColor(ContextCompat.getColor(context, R.color.success_color));

                            // Set alamat jika tersedia dan field alamat masih kosong
                            if (selectedAddress != null && !selectedAddress.isEmpty() &&
                                    etAlamat.getText().toString().trim().isEmpty()) {
                                etAlamat.setText(selectedAddress);
                            }

                            Log.d("MapPicker", "Location selected: " + selectedLat + ", " + selectedLng);
                        } else {
                            showErrorDialog("Gagal mendapatkan lokasi yang dipilih");
                        }
                    }
                    break;
            }
        } else {
            Log.d("ActivityResult", "Result not OK. RequestCode: " + requestCode + ", ResultCode: " + resultCode);
        }
    }

    private void processPembayaran() {
        if (!validateInput()) {
            return;
        }

        // Generate order ID
        orderId = "ORDER_" + System.currentTimeMillis();

        // Create transaction request
        createTransactionRequest();
    }

    // Improved validation method
    private boolean validateInput() {
        String alamat = etAlamat.getText().toString().trim();
        String jumlahText = etJumlahItem.getText().toString().trim();

        // Validate address
        if (alamat.isEmpty()) {
            etAlamat.setError("Alamat tidak boleh kosong");
            etAlamat.requestFocus();
            showEmptyFieldsDialog("Alamat");
            return false;
        }

        if (alamat.length() < 10) {
            etAlamat.setError("Alamat terlalu pendek. Minimal 10 karakter");
            etAlamat.requestFocus();
            showErrorDialog("Alamat terlalu pendek. Minimal 10 karakter");
            return false;
        }

        // Validate image
        if (selectedImageBitmap == null) {
            showEmptyFieldsDialog("Foto barang");
            return false;
        }

        // Validate location
        if (locationString.isEmpty()) {
            showEmptyFieldsDialog("Lokasi GPS");
            return false;
        }

        // Validate quantity
        if (jumlahText.isEmpty()) {
            etJumlahItem.setError("Jumlah item tidak boleh kosong");
            etJumlahItem.requestFocus();
            showEmptyFieldsDialog("Jumlah item");
            return false;
        }

        try {
            double jumlah = Double.parseDouble(jumlahText);
            if (jumlah <= 0) {
                etJumlahItem.setError("Jumlah item harus lebih dari 0");
                etJumlahItem.requestFocus();
                showErrorDialog("Jumlah item harus lebih dari 0");
                return false;
            }

            // Validate total harga
            if (totalHarga <= 0) {
                showErrorDialog("Total harga tidak valid");
                return false;
            }

            // Validate paket model
            if (paketModel == null || paketModel.getId_paket() == null || paketModel.getId_paket().isEmpty()) {
                showErrorDialog("Data paket tidak valid");
                return false;
            }

            // Validate user data
            if (prefManager.getId() == null || prefManager.getId().isEmpty()) {
                showErrorDialog("Data user tidak valid. Silakan login ulang.");
                return false;
            }

        } catch (NumberFormatException e) {
            etJumlahItem.setError("Jumlah item tidak valid");
            etJumlahItem.requestFocus();
            showErrorDialog("Jumlah item tidak valid");
            return false;
        }

        return true;
    }

    private void createTransactionRequest() {
        // Validasi ulang sebelum mengirim request
        if (!validateInput()) {
            return;
        }

        String imageBase64 = "";
        if (selectedImageBitmap != null) {
            imageBase64 = bitmapToBase64(selectedImageBitmap);
        }

        // Konversi jumlahItem ke integer untuk database
        int jumlahItemInt = (int) jumlahItem;

        // Konversi totalHarga ke integer untuk Midtrans
        int totalHargaInt = (int) totalHarga;

        // Validate data before sending
        if (prefManager.getId() == null || prefManager.getId().isEmpty()) {
            showErrorDialog("Data user tidak valid. Silakan login ulang.");
            return;
        }

        if (paketModel.getId_paket() == null || paketModel.getId_paket().isEmpty()) {
            showErrorDialog("Data paket tidak valid.");
            return;
        }

        // Show loading dialog
        showLoadingDialog("Memproses transaksi...");

        // Create the request with proper error handling
        AndroidNetworking.post(URL_CREATE_TRANSAKSI)
                .addBodyParameter("id_pelanggan", prefManager.getId())
                .addBodyParameter("id_paket", paketModel.getId_paket())
                .addBodyParameter("alamat", etAlamat.getText().toString().trim())
                .addBodyParameter("lokasi", locationString)
                .addBodyParameter("item", String.valueOf(jumlahItemInt))
                .addBodyParameter("foto_barang", imageBase64)
                .addBodyParameter("total_harga", String.valueOf(totalHargaInt))
                .addBodyParameter("order_id", orderId)
                .setTag("create_transaction")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dismissLoadingDialog();

                        // Log the response for debugging
                        Log.d("CreateTransaction", "Response: " + response.toString());

                        try {
                            // Check if response has the expected structure
                            if (!response.has("code")) {
                                showErrorDialog("Format response tidak valid dari server");
                                return;
                            }

                            int code = response.getInt("code");
                            String message = response.getString("message");

                            if (code == 1) {
                                // Success - check if snap_token exists
                                if (response.has("snap_token")) {
                                    snapToken = response.getString("snap_token");
                                    Log.d("Midtrans", "Snap Token: " + snapToken);

                                    // Validate snap token
                                    if (snapToken != null && !snapToken.trim().isEmpty()) {
                                        startMidtransPayment();
                                    } else {
                                        showErrorDialog("Snap token tidak valid");
                                    }
                                } else {
                                    showErrorDialog("Snap token tidak ditemukan dalam response");
                                }
                            } else {
                                // Error from server
                                showErrorDialog("Error: " + message);
                            }
                        } catch (JSONException e) {
                            Log.e("CreateTransaction", "JSON Parse Error: " + e.getMessage());
                            Log.e("CreateTransaction", "Response: " + response.toString());
                            showErrorDialog("Error parsing response. Silakan coba lagi.");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        dismissLoadingDialog();
                        String errorMessage = "Terjadi kesalahan";
                        showErrorDialog(errorMessage);
                    }
                });
    }

    private void startMidtransPayment() {
        if (snapToken == null || snapToken.trim().isEmpty()) {
            showErrorDialog("Snap token tidak valid");
            return;
        }

        try {
            // Konversi ke integer untuk Midtrans
            int totalHargaInt = (int) totalHarga;
            int jumlahItemInt = (int) jumlahItem;

            // Validate amounts
            if (totalHargaInt <= 0) {
                showErrorDialog("Total harga tidak valid");
                return;
            }

            if (jumlahItemInt <= 0) {
                showErrorDialog("Jumlah item tidak valid");
                return;
            }

            // Create transaction request
            TransactionRequest transactionRequest = new TransactionRequest(orderId, totalHargaInt);

            // Set item details
            ItemDetails itemDetails = new ItemDetails(
                    paketModel.getId_paket(),
                    (double) (totalHargaInt / jumlahItemInt), // Harga per unit
                    jumlahItemInt,
                    paketModel.getNama_paket()
            );

            ArrayList<ItemDetails> itemDetailsList = new ArrayList<>();
            itemDetailsList.add(itemDetails);
            transactionRequest.setItemDetails(itemDetailsList);

            // Set customer details
            CustomerDetails customerDetails = new CustomerDetails();
            customerDetails.setFirstName(prefManager.getNama());

            // Validasi email
            String email = prefManager.getEmail();
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                email = "customer@example.com";
            }
            customerDetails.setEmail(email);
            customerDetails.setPhone(prefManager.getNohp());

            // Set billing address
            BillingAddress billingAddress = new BillingAddress();
            billingAddress.setAddress(etAlamat.getText().toString().trim());
            billingAddress.setCity("Jakarta");
            billingAddress.setPostalCode("12345");
            customerDetails.setBillingAddress(billingAddress);

            // Set shipping address
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setAddress(etAlamat.getText().toString().trim());
            shippingAddress.setCity("Jakarta");
            shippingAddress.setPostalCode("12345");
            customerDetails.setShippingAddress(shippingAddress);

            transactionRequest.setCustomerDetails(customerDetails);

            // Set transaction request to MidtransSDK
            MidtransSDK.getInstance().setTransactionRequest(transactionRequest);

            // Start payment with snap token
            MidtransSDK.getInstance().startPaymentUiFlow(context, snapToken);

        } catch (Exception e) {
            Log.e("Midtrans", "Error starting payment: " + e.getMessage());
            showErrorDialog("Error memulai pembayaran: " + e.getMessage());
        }
    }

    @Override
    public void onTransactionFinished(TransactionResult result) {
        String resultMessage = "";
        String status = "";

        if (result.getResponse() != null) {
            switch (result.getStatus()) {
                case TransactionResult.STATUS_SUCCESS:
                    status = "paid";
                    resultMessage = "Pembayaran berhasil! Pesanan Anda sedang diproses.";
                    break;
                case TransactionResult.STATUS_PENDING:
                    status = "pending";
                    resultMessage = "Pembayaran tertunda. Silakan selesaikan pembayaran Anda.";
                    break;
                case TransactionResult.STATUS_FAILED:
                    status = "failed";
                    resultMessage = "Pembayaran gagal. Silakan coba lagi.";
                    break;
            }
        } else if (result.isTransactionCanceled()) {
            status = "cancelled";
            resultMessage = "Pembayaran dibatalkan.";
        } else {
            status = "failed";
            resultMessage = "Terjadi kesalahan pada pembayaran.";
        }

        // Update status transaksi
        updateTransactionStatus(status);

        // Berikan feedback ke user
        if (status.equals("paid")) {
            showSuccessDialog(resultMessage);
            if (onPacketPesanListener != null) {
                onPacketPesanListener.onPesanAdded(resultMessage);
            }
            dismiss();
        } else if (status.equals("pending")) {
            showInfoDialog(resultMessage);
            if (onPacketPesanListener != null) {
                onPacketPesanListener.onPesanAdded(resultMessage);
            }
            dismiss();
        } else {
            showErrorDialog(resultMessage);
            if (onPacketPesanListener != null) {
                onPacketPesanListener.onPesanError(resultMessage);
            }
            // Jangan dismiss dialog jika gagal, biarkan user coba lagi
        }
    }

    private Dialog loadingDialog;

    private void showLoadingDialog(String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(message);
        textView.setGravity(android.view.Gravity.CENTER);

        builder.setView(view);
        builder.setCancelable(false);

        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showSuccessDialog(String message) {
        if (context != null) {
            PopupDialog.getInstance(context)
                    .statusDialogBuilder()
                    .createSuccessDialog()
                    .setHeading("Berhasil!")
                    .setDescription(message)
                    .build(Dialog::dismiss)
                    .show();
        }
    }

    private void showInfoDialog(String message) {
        if (context != null) {
            PopupDialog.getInstance(context)
                    .statusDialogBuilder()
                    .createWarningDialog()
                    .setHeading("Info")
                    .setDescription(message)
                    .build(Dialog::dismiss)
                    .show();
        }
    }

    private void updateTransactionStatus(String status) {
        AndroidNetworking.post(ApiServer.site_url_konsumen + "updateTransactionStatus.php")
                .addBodyParameter("order_id", orderId)
                .addBodyParameter("status", status)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Update Status", "Status updated successfully");
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Update Status", "Failed to update status");
                    }
                });
    }

    // Improved bitmapToBase64 with compression
    private String bitmapToBase64(Bitmap bitmap) {
        try {
            // First resize the bitmap to reduce size
            int maxWidth = 800;
            int maxHeight = 600;

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Calculate scaling ratio
            float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

            // Only resize if image is larger than max dimensions
            if (ratio < 1.0f) {
                int newWidth = Math.round(width * ratio);
                int newHeight = Math.round(height * ratio);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Use higher compression (lower quality) to reduce size
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Check if still too large (limit to 1MB)
            if (byteArray.length > 1024 * 1024) {
                // Try with even lower quality
                byteArrayOutputStream.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
                byteArray = byteArrayOutputStream.toByteArray();
            }

            byteArrayOutputStream.close();

            Log.d("ImageCompression", "Original size: " + (bitmap.getByteCount() / 1024) + "KB, Compressed size: " + (byteArray.length / 1024) + "KB");

            return Base64.encodeToString(byteArray, Base64.DEFAULT);

        } catch (Exception e) {
            Log.e("BitmapToBase64", "Error converting bitmap: " + e.getMessage());
            return "";
        }
    }

    private String formatRupiah(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return "Rp " + formatter.format(amount);
    }

    private void showErrorDialog(String message) {
        if (context != null) {
            PopupDialog.getInstance(context)
                    .statusDialogBuilder()
                    .createErrorDialog()
                    .setHeading("Error")
                    .setDescription(message)
                    .build(Dialog::dismiss)
                    .show();
        }
    }

    private void showEmptyFieldsDialog(String fieldName) {
        if (context != null) {
            PopupDialog.getInstance(context)
                    .statusDialogBuilder()
                    .createWarningDialog()
                    .setHeading("WARNING !!!")
                    .setDescription(fieldName + " tidak boleh kosong.")
                    .setCancelable(false)
                    .build(Dialog::dismiss)
                    .show();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public interface OnPacketPesanListener {
        void onPesanAdded(String message);
        void onPesanError(String errorMessage);
    }

    private OnPacketPesanListener onPacketPesanListener;

    public void setOnPacketPesanListener(OnPacketPesanListener listener) {
        this.onPacketPesanListener = listener;
    }
}