package com.example.safaclink.activity.owner;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.example.safaclink.MainActivity;
import com.example.safaclink.R;
import com.example.safaclink.activity.admin.LaporanActivity;
import com.example.safaclink.activity.admin.MainAdminActivity;
import com.example.safaclink.adapter.LaporanTransaksiAdapter;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.apiserver.PrefManager;
import com.example.safaclink.databinding.ActivityLaporanBinding;
import com.example.safaclink.databinding.ActivityMainOwnerBinding;
import com.example.safaclink.model.CombinedOrderModel;
import com.google.gson.Gson;
import com.saadahmedev.popupdialog.PopupDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainOwnerActivity extends AppCompatActivity {
    private ActivityMainOwnerBinding binding;
    private Dialog loadingDialog;
    private static final String URL_GET_LAPORAN = ApiServer.site_url_admin + "getLaporan.php";
    private LaporanTransaksiAdapter laporanAdapter;
    private List<CombinedOrderModel> combinedOrderModels;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String tanggalAwal = "";
    private String tanggalAkhir = "";
    private int totalTransaksi = 0;
    private long totalPendapatan = 0;
    private NumberFormat rupiah;
    private PrefManager prefManager;

    private ActivityResultLauncher<Intent> createDocumentLauncher;
    private static final String URL_UPDATE_PROFILE = ApiServer.site_url_admin + "updateProfile.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainOwnerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(MainOwnerActivity.this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        setupDatePickers();
        setupButtons();
        initLoadingDialog();
        prefManager = new PrefManager(this);
        if (prefManager.getImg() == null || prefManager.getImg().equals("null")) {
            binding.imgProfile.setImageResource(R.mipmap.icon_user_foreground);
        } else {
            Picasso.get()
                    .load(ApiServer.site_url_fotoProfile + prefManager.getImg())
                    .into(binding.imgProfile);
        }
        binding.textNama.setText(prefManager.getNama());
        binding.txtRole.setText(prefManager.getTipe());

        // Untuk menampilkan PopupMenu
        ImageView btnDropdown = findViewById(R.id.btnDropdown);
        btnDropdown.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.dropdown_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_profile) {
                    // Intent ke ProfileActivity
                    showEditProfileDialog();
                    return true;
                } else if (item.getItemId() == R.id.menu_logout) {
                    // Logout logic
                    performLogout();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // Initialize document launcher for PDF export
        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            generatePDF(uri);
                        }
                    }
                }
        );
    }

    private void showEditProfileDialog() {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        // Create layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Create EditText fields
        EditText etNama = new EditText(this);
        etNama.setHint("Nama");
        etNama.setText(prefManager.getNama());

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(prefManager.getEmail());

        EditText etNohp = new EditText(this);
        etNohp.setHint("No HP");
        etNohp.setText(prefManager.getNohp());

        EditText etUsername = new EditText(this);
        etUsername.setHint("Username");
        etUsername.setText(prefManager.getUsername());

        EditText etPassword = new EditText(this);
        etPassword.setHint("Password Baru (kosongkan jika tidak diubah)");
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Add views to layout
        layout.addView(etNama);
        layout.addView(etEmail);
        layout.addView(etNohp);
        layout.addView(etUsername);
        layout.addView(etPassword);

        builder.setView(layout);

        // Set buttons
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String nama = etNama.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String nohp = etNohp.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (nama.isEmpty() || email.isEmpty() || nohp.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi kecuali password", Toast.LENGTH_SHORT).show();
                return;
            }

            updateProfile(nama, email, nohp, username, password);
        });

        builder.setNegativeButton("Batal", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateProfile(String nama, String email, String nohp, String username, String password) {
        showLoading();

        AndroidNetworking.post(URL_UPDATE_PROFILE)
                .addBodyParameter("id_user", String.valueOf(prefManager.getId()))
                .addBodyParameter("nama", nama)
                .addBodyParameter("email", email)
                .addBodyParameter("nohp", nohp)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password) // Akan kosong jika tidak diubah
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        hideLoading();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("code").equals("1")) {
                                // Update berhasil
                                prefManager.setNama(nama);
                                prefManager.setEmail(email);
                                prefManager.setNohp(nohp);
                                prefManager.setUsername(username);

                                // Update UI
                                binding.textNama.setText(nama);

                                Toast.makeText(MainOwnerActivity.this, "Profile berhasil diperbarui", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainOwnerActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainOwnerActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Toast.makeText(MainOwnerActivity.this, "Terjadi kesalahan: " + anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews() {
        combinedOrderModels = new ArrayList<>();
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    }

    private void setupRecyclerView() {
        binding.rvTransaksi.setLayoutManager(new LinearLayoutManager(this));
        laporanAdapter = new LaporanTransaksiAdapter(combinedOrderModels);
        binding.rvTransaksi.setAdapter(laporanAdapter);
    }

    private void setupDatePickers() {
        binding.etTanggalAwal.setOnClickListener(v -> showDatePicker(true));
        binding.etTanggalAkhir.setOnClickListener(v -> showDatePicker(false));
    }

    private void setupButtons() {
        binding.btnFilter.setOnClickListener(v -> {
            if (validateDateRange()) {
                dataLaporan();
            }
        });

        binding.btnReset.setOnClickListener(v -> {
            binding.etTanggalAwal.setText("");
            binding.etTanggalAkhir.setText("");
            tanggalAwal = "";
            tanggalAkhir = "";
            resetViews();
        });

        binding.btnExportPdf.setOnClickListener(v -> showSaveDialog());
    }

    private void showDatePicker(boolean isStartDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = dateFormat.format(calendar.getTime());

                    if (isStartDate) {
                        tanggalAwal = selectedDate;
                        binding.etTanggalAwal.setText(formatDisplayDate(selectedDate));
                    } else {
                        tanggalAkhir = selectedDate;
                        binding.etTanggalAkhir.setText(formatDisplayDate(selectedDate));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private String formatDisplayDate(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (Exception e) {
            return date;
        }
    }

    private boolean validateDateRange() {
        if (tanggalAwal.isEmpty() || tanggalAkhir.isEmpty()) {
            Toast.makeText(this, "Silakan pilih tanggal awal dan akhir", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Date startDate = dateFormat.parse(tanggalAwal);
            Date endDate = dateFormat.parse(tanggalAkhir);

            if (startDate.after(endDate)) {
                Toast.makeText(this, "Tanggal awal tidak boleh lebih besar dari tanggal akhir", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Format tanggal tidak valid", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void dataLaporan() {
        showLoading();

        String url = URL_GET_LAPORAN;
        if (!tanggalAwal.isEmpty() && !tanggalAkhir.isEmpty()) {
            url += "?tanggal_awal=" + tanggalAwal + "&tanggal_akhir=" + tanggalAkhir;
        }

        AndroidNetworking.get(url)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hideLoading();
                            Log.d("response", "response::" + response);

                            if (response.getString("code").equalsIgnoreCase("1")) {
                                JSONArray array = response.getJSONArray("data");
                                combinedOrderModels.clear();

                                Gson gson = new Gson();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject orderObject = array.getJSONObject(i);
                                    CombinedOrderModel orderModel = gson.fromJson(orderObject.toString(), CombinedOrderModel.class);
                                    combinedOrderModels.add(orderModel);
                                }

                                // Update summary statistics
                                if (response.has("summary")) {
                                    JSONObject summary = response.getJSONObject("summary");
                                    totalTransaksi = summary.getInt("total_transaksi");
                                    totalPendapatan = summary.getLong("total_pendapatan");

                                    binding.tvTotalTransaksi.setText(String.valueOf(totalTransaksi));
                                    binding.tvTotalPendapatan.setText(rupiah.format(totalPendapatan));
                                }

                                // Show/hide views based on data
                                if (combinedOrderModels.isEmpty()) {
                                    showEmptyState();
                                } else {
                                    showDataState();
                                }

                                laporanAdapter.notifyDataSetChanged();
                            } else {
                                showEmptyState();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideLoading();
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        showEmptyState();
                        Toast.makeText(MainOwnerActivity.this, "Terjadi kesalahan saat mengambil data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEmptyState() {
        binding.emptyStateLayout.setVisibility(View.VISIBLE);
        binding.rvTransaksi.setVisibility(View.GONE);
        binding.statsCard.setVisibility(View.GONE);
        binding.btnExportPdf.setVisibility(View.GONE);
    }

    private void showDataState() {
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.rvTransaksi.setVisibility(View.VISIBLE);
        binding.statsCard.setVisibility(View.VISIBLE);
        binding.btnExportPdf.setVisibility(View.VISIBLE);
    }

    private void resetViews() {
        combinedOrderModels.clear();
        laporanAdapter.notifyDataSetChanged();
        showEmptyState();
    }

    private void showSaveDialog() {
        String fileName = "Laporan_Transaksi_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        createDocumentLauncher.launch(intent);
    }

    private void generatePDF(Uri uri) {
        try {
            // Create PDF document
            PdfDocument document = new PdfDocument();

            // Page info
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            // Paint objects
            Paint titlePaint = new Paint();
            titlePaint.setTextSize(18);
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            titlePaint.setColor(Color.BLACK);

            Paint headerPaint = new Paint();
            headerPaint.setTextSize(14);
            headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            headerPaint.setColor(Color.BLACK);

            Paint textPaint = new Paint();
            textPaint.setTextSize(12);
            textPaint.setColor(Color.BLACK);

            Paint smallTextPaint = new Paint();
            smallTextPaint.setTextSize(10);
            smallTextPaint.setColor(Color.GRAY);

            // Draw content
            int yPosition = 50;

            // Title
            canvas.drawText("LAPORAN TRANSAKSI", 50, yPosition, titlePaint);
            yPosition += 30;

            // Date range if filtered
            if (!tanggalAwal.isEmpty() && !tanggalAkhir.isEmpty()) {
                canvas.drawText("Periode: " + formatDisplayDate(tanggalAwal) + " - " + formatDisplayDate(tanggalAkhir), 50, yPosition, textPaint);
                yPosition += 20;
            }

            // Summary
            canvas.drawText("Total Transaksi: " + totalTransaksi, 50, yPosition, textPaint);
            yPosition += 20;
            canvas.drawText("Total Pendapatan: " + rupiah.format(totalPendapatan), 50, yPosition, textPaint);
            yPosition += 30;

            // Table header
            canvas.drawText("No", 50, yPosition, headerPaint);
            canvas.drawText("Order ID", 80, yPosition, headerPaint);
            canvas.drawText("Pelanggan", 200, yPosition, headerPaint);
            canvas.drawText("Paket", 300, yPosition, headerPaint);
            canvas.drawText("Harga", 450, yPosition, headerPaint);
            canvas.drawText("Tanggal", 520, yPosition, headerPaint);
            yPosition += 25;

            // Draw line
            canvas.drawLine(50, yPosition - 10, 545, yPosition - 10, textPaint);

            // Table content
            int maxItemsPerPage = 25;
            int currentPage = 1;

            for (int i = 0; i < combinedOrderModels.size(); i++) {
                if (i > 0 && i % maxItemsPerPage == 0) {
                    // Start new page
                    document.finishPage(page);
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPosition = 50;
                    currentPage++;
                }

                CombinedOrderModel item = combinedOrderModels.get(i);

                // Format date
                String formattedDate = "";
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    Date date = inputFormat.parse(item.getCreated_at());
                    if (date != null) {
                        formattedDate = outputFormat.format(date);
                    }
                } catch (Exception e) {
                    formattedDate = item.getCreated_at().substring(0, 10);
                }

                // Draw row
                canvas.drawText(String.valueOf(i + 1), 50, yPosition, textPaint);
                canvas.drawText(item.getOrder_id().length() > 15 ? item.getOrder_id().substring(0, 15) + "..." : item.getOrder_id(), 80, yPosition, textPaint);
                canvas.drawText(item.getNama_pelanggan().length() > 12 ? item.getNama_pelanggan().substring(0, 12) + "..." : item.getNama_pelanggan(), 200, yPosition, textPaint);
                canvas.drawText(item.getNama_paket().length() > 15 ? item.getNama_paket().substring(0, 15) + "..." : item.getNama_paket(), 300, yPosition, textPaint);
                canvas.drawText(rupiah.format(Long.parseLong(item.getTotal_harga())), 450, yPosition, textPaint);
                canvas.drawText(formattedDate, 520, yPosition, textPaint);

                yPosition += 20;

                // Add line every 5 rows
                if ((i + 1) % 5 == 0) {
                    canvas.drawLine(50, yPosition - 5, 545, yPosition - 5, smallTextPaint);
                }
            }

            // Footer
            canvas.drawText("Dicetak pada: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()), 50, 800, smallTextPaint);
            canvas.drawText("Halaman " + currentPage, 500, 800, smallTextPaint);

            document.finishPage(page);

            // Save to file using try-with-resources (Java syntax)
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    document.writeTo(outputStream);
                    outputStream.flush();
                }
            }

            document.close();

            Toast.makeText(this, "PDF berhasil disimpan", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initLoadingDialog() {
        loadingDialog = PopupDialog.getInstance(MainOwnerActivity.this)
                .progressDialogBuilder()
                .createProgressDialog()
                .setTint(R.color.red)
                .setCancelable(false)
                .build().getDialog();
    }

    private void showLoading() {
        if (loadingDialog == null) {
            initLoadingDialog();
        }
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void performLogout() {
        // Kembali ke MainActivity dan clear semua activity stack
        Intent intent = new Intent(MainOwnerActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}