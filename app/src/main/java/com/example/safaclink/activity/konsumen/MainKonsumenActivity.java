package com.example.safaclink.activity.konsumen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.androidnetworking.AndroidNetworking;
import com.example.safaclink.R;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.apiserver.PrefManager;
import com.example.safaclink.databinding.ActivityMainKonsumenBinding;
import com.squareup.picasso.Picasso;

public class MainKonsumenActivity extends AppCompatActivity {
    private ActivityMainKonsumenBinding binding;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainKonsumenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AndroidNetworking.initialize(this);
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
        Log.d("response", "response::" + prefManager.getImg());

        binding.cardPaket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainKonsumenActivity.this, PaketActivity.class);
                startActivity(intent);
            }
        });

        binding.cardOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainKonsumenActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });
    }
}