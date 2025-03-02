package com.example.safaclink.activity.admin;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.androidnetworking.AndroidNetworking;
import com.example.safaclink.R;
import com.example.safaclink.adapter.TabUserAdapter;
import com.example.safaclink.databinding.ActivityMainAdminBinding;
import com.example.safaclink.databinding.ActivityUserBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class UserActivity extends AppCompatActivity {
    private ActivityUserBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabUserAdapter adapter;

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

        tabLayout = binding.tabLayout;
        viewPager = binding.viewPager;

        adapter = new TabUserAdapter(UserActivity.this);
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
    }
}