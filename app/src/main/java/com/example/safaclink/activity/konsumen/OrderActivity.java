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
import androidx.viewpager2.widget.ViewPager2;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.safaclink.R;
import com.example.safaclink.adapter.TabOrderKonsumen;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.databinding.ActivityOrderBinding;
import com.example.safaclink.model.OrderModel;
import com.example.safaclink.model.TransaksiModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity {
    private ActivityOrderBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Dialog loadingDialog;
    private TabOrderKonsumen adapter;
    private static final String URL_SEARCH_ORDERS = ApiServer.site_url_konsumen + "searchOrders.php";

    // Interface untuk komunikasi dengan fragments
    public interface SearchResultListener {
        void onSearchResult(List<TransaksiModel> transactionResults, List<OrderModel> orderResults, String statusType);
        void onSearchCleared();
    }

    private SearchResultListener currentFragmentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderBinding.inflate(getLayoutInflater());
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
    }

    private void setupTabsAndViewPager() {
        tabLayout = binding.tabLayout;
        viewPager = binding.viewPager;

        adapter = new TabOrderKonsumen(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Orders"); // For paid transactions (not yet in tb_orders)
                    break;
                case 1:
                    tab.setText("Dikerjakan"); // For orders with status 'dikerjakan'
                    break;
                case 2:
                    tab.setText("Konfirmasi"); // For orders with status 'dikonfirmasi'
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
                            Log.d("response", "response::" + response);

                            if (code == 1) {
                                JSONObject data = response.getJSONObject("data");

                                // Parse transactions
                                List<TransaksiModel> transactionResults = new ArrayList<>();
                                if (data.has("transactions")) {
                                    JSONArray transactionsArray = data.getJSONArray("transactions");
                                    Gson gson = new Gson();
                                    for (int i = 0; i < transactionsArray.length(); i++) {
                                        JSONObject transactionObject = transactionsArray.getJSONObject(i);
                                        TransaksiModel transaction = gson.fromJson(transactionObject.toString(), TransaksiModel.class);
                                        transactionResults.add(transaction);
                                    }
                                }

                                // Parse orders
                                List<OrderModel> orderResults = new ArrayList<>();
                                if (data.has("orders")) {
                                    JSONArray ordersArray = data.getJSONArray("orders");
                                    Gson gson = new Gson();
                                    for (int i = 0; i < ordersArray.length(); i++) {
                                        JSONObject orderObject = ordersArray.getJSONObject(i);
                                        OrderModel order = gson.fromJson(orderObject.toString(), OrderModel.class);
                                        orderResults.add(order);
                                    }
                                }

                                // Determine the best tab based on search results
                                String statusType = determineStatusType(transactionResults, orderResults);

                                // Switch to appropriate tab
                                switchToTabByStatus(statusType);

                                // Send results to current fragment
                                if (currentFragmentListener != null) {
                                    currentFragmentListener.onSearchResult(transactionResults, orderResults, statusType);
                                }
                            } else {
                                // No results found
                                if (currentFragmentListener != null) {
                                    currentFragmentListener.onSearchResult(new ArrayList<>(), new ArrayList<>(), "");
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

    private String determineStatusType(List<TransaksiModel> transactions, List<OrderModel> orders) {
        // Priority: Orders with status -> Paid transactions without orders
        if (!orders.isEmpty()) {
            // Check the most common status in orders
            String firstOrderStatus = orders.get(0).getStatus_order();
            switch (firstOrderStatus) {
                case "dikerjakan":
                    return "dikerjakan";
                case "dikonfirmasi":
                    return "dikonfirmasi";
                case "selesai":
                    return "dikonfirmasi"; // Map to konfirmasi tab
                default:
                    return "dikerjakan";
            }
        } else if (!transactions.isEmpty()) {
            // Check if transactions are paid but not in orders yet
            for (TransaksiModel transaction : transactions) {
                if ("paid".equals(transaction.getStatus_transaksi())) {
                    return "orders"; // These are paid but not yet processed
                }
            }
        }

        return "orders"; // Default to orders tab
    }

    private void switchToTabByStatus(String statusType) {
        int tabPosition = 0; // default to Orders

        switch (statusType) {
            case "orders":
                tabPosition = 0;
                break;
            case "dikerjakan":
                tabPosition = 1;
                break;
            case "dikonfirmasi":
            case "selesai":
                tabPosition = 2;
                break;
        }

        viewPager.setCurrentItem(tabPosition, true);

        // Update the current fragment listener after tab switch
        viewPager.post(() -> updateCurrentFragmentListener());
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
            Log.e("OrderActivity", "Error getting current fragment: " + e.getMessage());
        }
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