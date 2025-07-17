package com.example.safaclink.activity.konsumen;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.safaclink.R;
import com.example.safaclink.adapter.OrderKonsumenAdapter;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.apiserver.PrefManager;
import com.example.safaclink.databinding.FragmentTabDikerjakanKonsumenBinding;
import com.example.safaclink.databinding.FragmentTabOrdersKonsumenBinding;
import com.example.safaclink.model.CombinedOrderModel;
import com.google.gson.Gson;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TabDikerjakanKonsumenFragment extends Fragment implements OrderKonsumenAdapter.OnPrintInvoiceListener {
    private FragmentTabDikerjakanKonsumenBinding binding;
    private List<CombinedOrderModel> combinedOrderModels;
    private Dialog loadingDialog;
    private static final String URL_DIKERJAKAN = ApiServer.site_url_konsumen + "getDikerjakan.php";
    private Context mContext;
    private OrderKonsumenAdapter orderKonsumenAdapter;
    private PrefManager prefManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTabDikerjakanKonsumenBinding.inflate(inflater, container, false);

        // Setup RecyclerView
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize list
        combinedOrderModels = new ArrayList<>();
        mContext = requireContext();
        prefManager = new PrefManager(mContext);

        // Initialize loading dialog
        initLoadingDialog();

        // Load data
        dataDikerjakan();

        return binding.getRoot();
    }

    private void dataDikerjakan() {
        showLoading();
        AndroidNetworking.get(URL_DIKERJAKAN + "?id_user=" + prefManager.getId())
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

                                // Set up adapter
                                orderKonsumenAdapter = new OrderKonsumenAdapter(mContext, combinedOrderModels);
                                orderKonsumenAdapter.setOnOrdersListChangedListener(() -> dataDikerjakan());
                                orderKonsumenAdapter.setOnPrintInvoiceListener(TabDikerjakanKonsumenFragment.this);
                                binding.recyclerView.setAdapter(orderKonsumenAdapter);

                                // Show/hide empty state
                                if (combinedOrderModels.isEmpty()) {
                                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                                    binding.recyclerView.setVisibility(View.GONE);
                                } else {
                                    binding.emptyStateLayout.setVisibility(View.GONE);
                                    binding.recyclerView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                // No data found
                                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideLoading();
                            Log.e("error", "JSON parsing error: " + e.getMessage());

                            // Show empty state on error
                            binding.emptyStateLayout.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Log.e("error", "onError: " + anError.getErrorBody());

                        // Show empty state on error
                        binding.emptyStateLayout.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onPrintInvoice(CombinedOrderModel orderModel) {
        // Buat intent untuk membuka InvoiceActivity
        Intent intent = new Intent(mContext, InvoiceActivity.class);

        // Kirim data order ke InvoiceActivity
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