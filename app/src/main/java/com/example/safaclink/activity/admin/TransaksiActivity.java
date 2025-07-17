package com.example.safaclink.activity.admin;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.safaclink.R;
import com.example.safaclink.activity.konsumen.InvoiceActivity;
import com.example.safaclink.adapter.OrderKonsumenAdapter;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.databinding.ActivityTransaksiBinding;
import com.example.safaclink.model.CombinedOrderModel;
import com.google.gson.Gson;
import com.saadahmedev.popupdialog.PopupDialog;
import com.saadahmedev.popupdialog.listener.StandardDialogActionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TransaksiActivity extends AppCompatActivity implements OrderKonsumenAdapter.OnPrintInvoiceListener, OrderKonsumenAdapter.OnKonfirmasiListener {
    private ActivityTransaksiBinding binding;
    private Dialog loadingDialog;
    private Context mContext;
    private OrderKonsumenAdapter orderKonsumenAdapter;
    private List<CombinedOrderModel> combinedOrderModels;
    private List<CombinedOrderModel> originalList;
    private static final String URL_TRANSAKSI = ApiServer.site_url_admin + "getTransaksi.php";
    private static final String URL_SEARCH_ORDERS = ApiServer.site_url_admin + "searchOrders.php";
    private static final String URL_KONFIRMASI = ApiServer.site_url_admin + "konfirmasiOrders.php";

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransaksiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mContext = this;
        combinedOrderModels = new ArrayList<>();
        originalList = new ArrayList<>();

        initLoadingDialog();
        setupSearchFunctionality();
        dataTransaksi();
    }

    private void setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (keyword.isEmpty()) {
                        restoreOriginalData();
                    } else {
                        searchData(keyword);
                    }
                };

                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchData(String keyword) {
        showLoading();
        AndroidNetworking.post(URL_SEARCH_ORDERS)
                .addBodyParameter("keyword", keyword)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hideLoading();
                            int code = response.getInt("code");
                            Log.d("response", "search response::" + response);

                            if (code == 1) {
                                JSONArray array = response.getJSONArray("data");
                                List<CombinedOrderModel> searchResults = new ArrayList<>();

                                Gson gson = new Gson();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject orderObject = array.getJSONObject(i);
                                    CombinedOrderModel orderModel = gson.fromJson(orderObject.toString(), CombinedOrderModel.class);
                                    searchResults.add(orderModel);
                                }

                                updateUI(searchResults);
                            } else {
                                updateUI(new ArrayList<>());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideLoading();
                            Log.d("catch", "searchData error: " + e.toString());
                            updateUI(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Log.e("error", "search onError: " + anError.getErrorBody());
                        updateUI(new ArrayList<>());
                    }
                });
    }

    private void restoreOriginalData() {
        updateUI(new ArrayList<>(originalList));
        updateTransactionCount(originalList.size());
    }

    private void updateUI(List<CombinedOrderModel> dataList) {
        combinedOrderModels.clear();
        combinedOrderModels.addAll(dataList);

        if (orderKonsumenAdapter != null) {
            orderKonsumenAdapter.notifyDataSetChanged();
        }

        if (combinedOrderModels.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.rvTransaksi.setVisibility(View.GONE);
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.rvTransaksi.setVisibility(View.VISIBLE);
        }
    }

    private void updateTransactionCount(int count) {
        binding.tvTotal.setText(String.valueOf(count));
    }
    private void dataTransaksi() {
        showLoading();
        AndroidNetworking.get(URL_TRANSAKSI)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hideLoading();
                            String code = response.getString("code");
                            if (code.equalsIgnoreCase("1")) {
                                JSONArray array = response.getJSONArray("data");
                                combinedOrderModels.clear();
                                originalList.clear();

                                Gson gson = new Gson();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject orderObject = array.getJSONObject(i);

                                    CombinedOrderModel orderModel = gson.fromJson(orderObject.toString(), CombinedOrderModel.class);
                                    combinedOrderModels.add(orderModel);
                                    originalList.add(orderModel);
                                }

                                // Update transaction count from server response
                                int totalCount = response.optInt("total_count", combinedOrderModels.size());
                                updateTransactionCount(totalCount);

                                // Set up adapter dengan role admin
                                orderKonsumenAdapter = new OrderKonsumenAdapter(TransaksiActivity.this, combinedOrderModels, "admin");
                                orderKonsumenAdapter.setOnOrdersListChangedListener(() -> dataTransaksi());
                                orderKonsumenAdapter.setOnPrintInvoiceListener(TransaksiActivity.this);
                                orderKonsumenAdapter.setOnKonfirmasiListener(TransaksiActivity.this);
                                binding.rvTransaksi.setLayoutManager(new LinearLayoutManager(TransaksiActivity.this));
                                binding.rvTransaksi.setAdapter(orderKonsumenAdapter);

                                // Debug tambahan
                                orderKonsumenAdapter.notifyDataSetChanged();
                                if (combinedOrderModels.isEmpty()) {
                                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                                    binding.rvTransaksi.setVisibility(View.GONE);
                                } else {
                                    binding.emptyStateLayout.setVisibility(View.GONE);
                                    binding.rvTransaksi.setVisibility(View.VISIBLE);
                                }
                            } else {
                                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                                binding.rvTransaksi.setVisibility(View.GONE);
                                updateTransactionCount(0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideLoading();
                            Log.e("DEBUG_EXCEPTION", "JSON parsing error: " + e.getMessage());
                            binding.emptyStateLayout.setVisibility(View.VISIBLE);
                            binding.rvTransaksi.setVisibility(View.GONE);
                            updateTransactionCount(0);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Log.e("DEBUG_ERROR", "Network error: " + anError.getErrorBody());
                        Log.e("DEBUG_ERROR", "Error code: " + anError.getErrorCode());
                        Log.e("DEBUG_ERROR", "Error detail: " + anError.getErrorDetail());
                        binding.emptyStateLayout.setVisibility(View.VISIBLE);
                        binding.rvTransaksi.setVisibility(View.GONE);
                        updateTransactionCount(0);
                    }
                });
    }

    @Override
    public void onPrintInvoice(CombinedOrderModel orderModel) {
        Intent intent = new Intent(mContext, InvoiceActivity.class);
        intent.putExtra("ORDER_ID", orderModel.getOrder_id());
        intent.putExtra("NAMA_PAKET", orderModel.getNama_paket());
        intent.putExtra("TIPE_PAKET", orderModel.getTipe_paket());
        intent.putExtra("TOTAL_HARGA", orderModel.getTotal_harga());
        intent.putExtra("DESKRIPSI", orderModel.getPaket_deskripsi());
        intent.putExtra("STATUS_ORDER", orderModel.getStatus_order());
        intent.putExtra("NAMA_PELANGGAN", orderModel.getNama_pelanggan());
        intent.putExtra("EMAIL_PELANGGAN", orderModel.getEmail_pelanggan());
        intent.putExtra("NOHP_PELANGGAN", orderModel.getNohp_pelanggan());
        intent.putExtra("TANGGAL_TRANSAKSI", orderModel.getCreated_at());
        startActivity(intent);
    }

    @Override
    public void onKonfirmasi(CombinedOrderModel orderModel) {
        String statusOrder = orderModel.getStatus_order();
        String statusTransaksi = orderModel.getStatus_transaksi();

        String dialogTitle = "";
        String dialogMessage = "";

        // Tentukan dialog berdasarkan status
        if (statusOrder == null && "paid".equals(statusTransaksi)) {
            dialogTitle = "Jemput Pesanan";
            dialogMessage = "Lakukan Penjemputan Barang?";
        } else if ("dijemput".equals(statusOrder)) {
            dialogTitle = "Kerjakan Pesanan";
            dialogMessage = "Mulai mengerjakan pesanan ini?";
        } else if ("dikerjakan".equals(statusOrder)) {
            dialogTitle = "Antar Pesanan";
            dialogMessage = "Antar pesanan kepada pelanggan?";
        } else if ("dikonfirmasi".equals(statusOrder)) {
            dialogTitle = "Selesaikan Pesanan";
            dialogMessage = "Selesaikan pesanan ini?";
        }

        PopupDialog.getInstance(mContext)
                .standardDialogBuilder()
                .createStandardDialog()
                .setHeading(dialogTitle)
                .setDescription(dialogMessage)
                .setIcon(com.midtrans.sdk.uikit.R.drawable.ic_tick_circle)
                .setPositiveButtonText("Ya, Konfirmasi")
                .setNegativeButtonText("Batal")
                .setPositiveButtonTextColor(R.color.white)
                .setNegativeButtonTextColor(R.color.red)
                .setPositiveButtonBackgroundColor(R.color.success_color)
                .setNegativeButtonBackgroundColor(R.color.white)
                .build(new StandardDialogActionListener() {
                    @Override
                    public void onPositiveButtonClicked(Dialog dialog) {
                        konfirmasiOrder(orderModel);
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegativeButtonClicked(Dialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void konfirmasiOrder(CombinedOrderModel orderModel) {
        showLoading();

        // Log untuk debugging
        Log.d("DEBUG_KONFIRMASI", "Order ID: " + orderModel.getOrder_id());
        Log.d("DEBUG_KONFIRMASI", "Current Status: " + orderModel.getStatus_order());
        Log.d("DEBUG_KONFIRMASI", "Transaction Status: " + orderModel.getStatus_transaksi());
        Log.d("DEBUG_KONFIRMASI", "URL: " + URL_KONFIRMASI);

        // Siapkan parameter
        String orderId = orderModel.getOrder_id() != null ? orderModel.getOrder_id() : "";
        String currentStatus = orderModel.getStatus_order() != null ? orderModel.getStatus_order() : "";
        String transactionStatus = orderModel.getStatus_transaksi() != null ? orderModel.getStatus_transaksi() : "";

        AndroidNetworking.post(URL_KONFIRMASI)
                .addBodyParameter("order_id", orderId)
                .addBodyParameter("current_status", currentStatus)
                .addBodyParameter("transaction_status", transactionStatus)
                .setPriority(Priority.HIGH)
                .setTag("konfirmasi_order")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hideLoading();
                            Log.d("DEBUG_KONFIRMASI", "Response: " + response.toString());

                            int code = response.optInt("code", 0);
                            String message = response.optString("message", "Tidak ada pesan");

                            if (code == 1) {
                                // Konfirmasi berhasil
                                PopupDialog.getInstance(mContext)
                                        .standardDialogBuilder()
                                        .createStandardDialog()
                                        .setHeading("Berhasil")
                                        .setDescription("Pesanan berhasil dikonfirmasi!")
                                        .setIcon(com.midtrans.sdk.uikit.R.drawable.ic_tick_circle)
                                        .setPositiveButtonText("OK")
                                        .setPositiveButtonTextColor(R.color.white)
                                        .setPositiveButtonBackgroundColor(R.color.success_color)
                                        .build(new StandardDialogActionListener() {
                                            @Override
                                            public void onPositiveButtonClicked(Dialog dialog) {
                                                // Refresh data
                                                dataTransaksi();
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onNegativeButtonClicked(Dialog dialog) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            } else {
                                // Konfirmasi gagal
                                showErrorDialog(message);
                            }
                        } catch (Exception e) {
                            hideLoading();
                            Log.e("DEBUG_KONFIRMASI", "JSON parsing error: " + e.getMessage());
                            e.printStackTrace();
                            showErrorDialog("Terjadi kesalahan saat memproses data");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Log.e("DEBUG_KONFIRMASI", "Network error - Code: " + anError.getErrorCode());
                        Log.e("DEBUG_KONFIRMASI", "Network error - Detail: " + anError.getErrorDetail());
                        Log.e("DEBUG_KONFIRMASI", "Network error - Body: " + anError.getErrorBody());

                        String errorMessage = "Gagal terhubung ke server";

                        // Cek jenis error
                        if (anError.getErrorCode() == 0) {
                            errorMessage = "Tidak ada koneksi internet";
                        } else if (anError.getErrorCode() >= 500) {
                            errorMessage = "Server sedang bermasalah";
                        } else if (anError.getErrorCode() >= 400) {
                            errorMessage = "Permintaan tidak valid";
                        }

                        showErrorDialog(errorMessage);
                    }
                });
    }

    private void showErrorDialog(String message) {
        PopupDialog.getInstance(mContext)
                .standardDialogBuilder()
                .createStandardDialog()
                .setHeading("Error")
                .setDescription(message)
                .setIcon(R.mipmap.ic_error_foreground)
                .setPositiveButtonText("OK")
                .setPositiveButtonTextColor(R.color.white)
                .setPositiveButtonBackgroundColor(R.color.red)
                .build(new StandardDialogActionListener() {
                    @Override
                    public void onPositiveButtonClicked(Dialog dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegativeButtonClicked(Dialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void initLoadingDialog() {
        loadingDialog = PopupDialog.getInstance(mContext)
                .progressDialogBuilder()
                .createProgressDialog()
                .setTint(R.color.red)
                .setCancelable(false)
                .build().getDialog();
    }

    private void showLoading() {
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
}