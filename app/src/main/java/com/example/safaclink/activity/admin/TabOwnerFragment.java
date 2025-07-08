package com.example.safaclink.activity.admin;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.safaclink.R;
import com.example.safaclink.adapter.UserAdapter;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.databinding.FragmentTabAdminBinding;
import com.example.safaclink.databinding.FragmentTabOwnerBinding;
import com.example.safaclink.model.UserModel;
import com.google.gson.Gson;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TabOwnerFragment extends Fragment implements DialogAddUserActivity.OnUserAddedListener {
    private FragmentTabOwnerBinding binding;
    private List<UserModel> userModelList;
    private UserAdapter userAdapter;
    private Dialog loadingDialog;
    private static final String URL_USER = ApiServer.site_url_admin + "getOwner.php";
    private static final String URL_SEARCH = ApiServer.site_url_admin + "searchOwner.php";
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTabOwnerBinding.inflate(inflater, container, false);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        userModelList = new ArrayList<>();
        mContext = requireContext();

        initLoadingDialog();
        dataOwner();

        return binding.getRoot();
    }

    private void dataOwner() {
        showLoading();
        AndroidNetworking.get(URL_USER)
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
                                userModelList.clear();
                                Gson gson = new Gson();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject beritaObject = array.getJSONObject(i);
                                    UserModel isi = gson.fromJson(beritaObject + "", UserModel.class);
                                    userModelList.add(isi);
                                }
                                userAdapter = new UserAdapter(mContext, userModelList);
                                userAdapter.setOnUserListChangedListener(() -> dataOwner());
                                binding.recyclerView.setAdapter(userAdapter);
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
    public void onUserAdded(String message) {
        showSuccessNotification(message);
    }

    @Override
    public void onUserError(String errorMessage) {
        showErrorNotification(errorMessage);
    }

    private void showSuccessNotification(String message) {
        PopupDialog.getInstance(mContext)
                .statusDialogBuilder()
                .createSuccessDialog()
                .setHeading("BERHASIL !!!")
                .setDescription(message)
                .setCancelable(false)
                .build(dialog -> {
                    dataOwner();
                    dialog.dismiss();
                })
                .show();
    }

    private void showErrorNotification(String message) {
        PopupDialog.getInstance(mContext)
                .statusDialogBuilder()
                .createErrorDialog()
                .setHeading("GAGAL !!!")
                .setDescription(message)
                .setCancelable(false)
                .build(dialog -> {
                    dataOwner();
                    dialog.dismiss();
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
}