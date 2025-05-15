package com.example.safaclink.activity.admin;

import android.app.AlertDialog;
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
import com.example.safaclink.MainActivity;
import com.example.safaclink.R;
import com.example.safaclink.adapter.UserAdapter;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.databinding.FragmentTabAdminBinding;
import com.example.safaclink.model.UserModel;
import com.google.gson.Gson;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class TabAdminFragment extends Fragment {
    private FragmentTabAdminBinding binding;
    private List<UserModel> userModelList;
    private UserAdapter userAdapter;
    private Dialog loadingDialog;
    private static final String URL_USER = ApiServer.site_url_admin + "getAdmin.php";
    private static final String URL_SEARCH_ADMIN = ApiServer.site_url_admin + "searchAdmin.php";
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTabAdminBinding.inflate(inflater, container, false);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        userModelList = new ArrayList<>();
        mContext = requireContext();

        initLoadingDialog();
        dataAdmin();

        binding.kolomSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                searchData(keyword);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return binding.getRoot();
    }

    private void searchData(String keyword) {
        showLoading();
        AndroidNetworking.post(URL_SEARCH_ADMIN)
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
                                userModelList.clear();
                                Gson gson = new Gson();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject userObject = array.getJSONObject(i);
                                    UserModel user = gson.fromJson(userObject.toString(), UserModel.class);
                                    userModelList.add(user);
                                }
                                userAdapter = new UserAdapter(requireContext(), userModelList);
                                binding.recyclerView.setAdapter(userAdapter);
                            } else {
                                userModelList.clear();
                                userAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("catch", "gambarModel::" + e.toString());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Log.e("error", "onError: " + anError.getErrorBody());
                    }
                });
    }

    private void dataAdmin() {
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
                                userAdapter.setOnUserListChangedListener(() -> dataAdmin());
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