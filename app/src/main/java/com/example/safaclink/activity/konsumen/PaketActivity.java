package com.example.safaclink.activity.konsumen;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

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
import com.example.safaclink.adapter.PaketKonsumenAdapter;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.databinding.ActivityPaketBinding;
import com.example.safaclink.model.PaketModel;
import com.google.gson.Gson;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PaketActivity extends AppCompatActivity implements DialogPesanActivity.OnPacketPesanListener {
    private ActivityPaketBinding binding;
    private List<PaketModel> paketModelList;
    private PaketKonsumenAdapter paketKonsumenAdapter;
    private Dialog loadingDialog;
    private static final String URL_PAKET = ApiServer.site_url_konsumen + "getPaket.php";
    private static final String URL_SEARCH = ApiServer.site_url_konsumen + "searchPaket.php";
    private static final String URL_COUNT = ApiServer.site_url_konsumen + "countPaket.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize binding
        binding = ActivityPaketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.rvPaket.setHasFixedSize(true);
        binding.rvPaket.setLayoutManager(new LinearLayoutManager(PaketActivity.this));
        paketModelList = new ArrayList<>();
        initLoadingDialog();
        dataPaket();
        countPaket();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    dataPaket(); // Reload all data when search is empty
                } else {
                    searchData(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void searchData(String keyword) {
        if (keyword.isEmpty()) {
            dataPaket();
            return;
        }

        showLoading();
        AndroidNetworking.post(URL_SEARCH)
                .addBodyParameter("keyword", keyword)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hideLoading();
                            int code = response.getInt("code");
                            Log.d("response", "response::" + response);
                            if (code == 1) {
                                JSONArray array = response.getJSONArray("data");
                                paketModelList.clear();
                                Gson gson = new Gson();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject userObject = array.getJSONObject(i);
                                    PaketModel paket = gson.fromJson(userObject.toString(), PaketModel.class);
                                    paketModelList.add(paket);
                                }
                                if (paketKonsumenAdapter == null) {
                                    paketKonsumenAdapter = new PaketKonsumenAdapter(PaketActivity.this, paketModelList);
                                    binding.rvPaket.setAdapter(paketKonsumenAdapter);
                                } else {
                                    paketKonsumenAdapter.notifyDataSetChanged();
                                }
                            } else {
                                paketModelList.clear();
                                if (paketKonsumenAdapter != null) {
                                    paketKonsumenAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideLoading();
                            Log.d("catch", "searchData error::" + e.toString());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Log.e("error", "onError: " + anError.getErrorBody());
                    }
                });
    }

    private void countPaket() {
        showLoading();
        AndroidNetworking.get(URL_COUNT)
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
                                if (array.length() > 0) {
                                    JSONObject obj = array.getJSONObject(0);
                                    int totalUser = obj.getInt("total_paket");
                                    binding.tvTotalPaket.setText(String.valueOf(totalUser));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideLoading();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Log.e("error", "onError: " + anError.getErrorBody());
                    }
                });
    }

    private void dataPaket() {
        showLoading();
        AndroidNetworking.get(URL_PAKET)
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
                                paketModelList.clear();
                                Gson gson = new Gson();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject beritaObject = array.getJSONObject(i);
                                    PaketModel isi = gson.fromJson(beritaObject + "", PaketModel.class);
                                    paketModelList.add(isi);
                                }
                                paketKonsumenAdapter = new PaketKonsumenAdapter(PaketActivity.this, paketModelList);
                                paketKonsumenAdapter.setOnPaketListChangedListener(() -> dataPaket());
                                binding.rvPaket.setAdapter(paketKonsumenAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideLoading();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Log.e("error", "onError: " + anError.getErrorBody());
                    }
                });
    }

    @Override
    public void onPesanAdded(String message) {
        showSuccessNotification(message);
    }

    @Override
    public void onPesanError(String errorMessage) {
        showErrorNotification(errorMessage);
    }

    private void showSuccessNotification(String message) {
        PopupDialog.getInstance(PaketActivity.this)
                .statusDialogBuilder()
                .createSuccessDialog()
                .setHeading("BERHASIL !!!")
                .setDescription(message)
                .setCancelable(false)
                .build(dialog -> {
                    dataPaket();
                    dialog.dismiss();
                })
                .show();
    }

    private void showErrorNotification(String message) {
        PopupDialog.getInstance(PaketActivity.this)
                .statusDialogBuilder()
                .createErrorDialog()
                .setHeading("GAGAL !!!")
                .setDescription(message)
                .setCancelable(false)
                .build(dialog -> {
                    dataPaket();
                    dialog.dismiss();
                })
                .show();
    }

    private void initLoadingDialog() {
        loadingDialog = PopupDialog.getInstance(PaketActivity.this)
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
}