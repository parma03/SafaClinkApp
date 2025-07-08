package com.example.safaclink.activity.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.safaclink.R;
import com.example.safaclink.adapter.TabUserAdapter;
import com.example.safaclink.adapter.UserAdapter;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.databinding.ActivityUserBinding;
import com.example.safaclink.model.UserModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity implements DialogAddUserActivity.OnUserAddedListener {
    private ActivityUserBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabUserAdapter adapter;
    private Dialog loadingDialog;
    private static final String URL_SEARCH_ADMIN = ApiServer.site_url_admin + "searchAdmin.php";
    private static final String URL_COUNT = ApiServer.site_url_admin + "countUser.php";

    // Interface untuk komunikasi dengan fragments
    public interface SearchResultListener {
        void onSearchResult(List<UserModel> results, String userType);
        void onSearchCleared();
    }

    private SearchResultListener currentFragmentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLoadingDialog();
        setupTabsAndViewPager();
        setupSearchFunctionality();
        setupFab();
        countUser();
    }

    private void setupTabsAndViewPager() {
        tabLayout = binding.tabLayout;
        viewPager = binding.viewPager;

        adapter = new TabUserAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Administrator");
                    break;
                case 1:
                    tab.setText("Konsumen");
                    break;
                case 2:
                    tab.setText("Owner");
                    break;
            }
        }).attach();

        // Listen for tab changes to update current fragment listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateCurrentFragmentListener();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    // Clear search results
                    if (currentFragmentListener != null) {
                        currentFragmentListener.onSearchCleared();
                    }
                } else {
                    searchData(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFab() {
        binding.fabAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog();
            }
        });
    }

    private void updateCurrentFragmentListener() {
        // Get current fragment and set as listener
        int currentPosition = viewPager.getCurrentItem();
        try {
            String fragmentTag = "f" + currentPosition;
            androidx.fragment.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (fragment instanceof SearchResultListener) {
                currentFragmentListener = (SearchResultListener) fragment;
            }
        } catch (Exception e) {
            Log.e("UserActivity", "Error getting current fragment: " + e.getMessage());
        }
    }

    private void countUser() {
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
                                    int totalUser = obj.getInt("total_user");
                                    binding.tvTotalUser.setText(String.valueOf(totalUser));
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
                                List<UserModel> searchResults = new ArrayList<>();
                                Gson gson = new Gson();
                                String userType = "";

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject userObject = array.getJSONObject(i);
                                    UserModel user = gson.fromJson(userObject.toString(), UserModel.class);
                                    searchResults.add(user);

                                    // Determine user type from first result
                                    if (i == 0) {
                                        userType = determineUserType(user);
                                    }
                                }

                                // Switch to appropriate tab based on search results
                                switchToTabByUserType(userType);

                                // Send results to current fragment
                                if (currentFragmentListener != null) {
                                    currentFragmentListener.onSearchResult(searchResults, userType);
                                }
                            } else {
                                // No results found
                                if (currentFragmentListener != null) {
                                    currentFragmentListener.onSearchResult(new ArrayList<>(), "");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideLoading();
                            Log.d("catch", "searchData error: " + e.toString());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Log.e("error", "onError: " + anError.getErrorBody());
                    }
                });
    }

    private String determineUserType(UserModel user) {
        // Assuming UserModel has a field that indicates user type
        // Adjust this logic based on your actual UserModel structure
        if (user.getRole() != null) {
            String role = user.getRole().toLowerCase();
            if (role.contains("admin")) {
                return "Administrator";
            } else if (role.contains("konsumen")) {
                return "Konsumen";
            } else if (role.contains("owner")) {
                return "Owner";
            }
        }

        // Default fallback - you might need to adjust this logic
        // based on how your API returns user types
        return "Administrator";
    }

    private void switchToTabByUserType(String userType) {
        int tabPosition = 0; // default to Administrator

        switch (userType) {
            case "Administrator":
                tabPosition = 0;
                break;
            case "Konsumen":
                tabPosition = 1;
                break;
            case "Owner":
                tabPosition = 2;
                break;
        }

        viewPager.setCurrentItem(tabPosition, true);

        // Update the current fragment listener after tab switch
        viewPager.post(() -> updateCurrentFragmentListener());
    }

    public void setSearchResultListener(SearchResultListener listener) {
        this.currentFragmentListener = listener;
    }

    private void showAddDialog() {
        DialogAddUserActivity dialog = new DialogAddUserActivity();
        dialog.setOnUserAddedListener(this);
        dialog.show(getSupportFragmentManager(), "add_user");
    }

    @Override
    public void onUserAdded(String message) {
        // Refresh current fragment data
        if (currentFragmentListener != null) {
            currentFragmentListener.onSearchCleared(); // This will reload original data
        }
    }

    @Override
    public void onUserError(String errorMessage) {
        // Handle error if needed
        Log.e("UserActivity", "User add error: " + errorMessage);
    }

    private void initLoadingDialog() {
        loadingDialog = PopupDialog.getInstance(this)
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