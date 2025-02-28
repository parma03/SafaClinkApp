package com.example.safaclink;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.safaclink.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);

                Pair[] pairs = new Pair[10];
                pairs[0] = new Pair<View, String>(binding.logoImage,"logo_image");
                pairs[1] = new Pair<View, String>(binding.logoName1,"logo_text");
                pairs[2] = new Pair<View, String>(binding.nama,"nama_trans");
                pairs[3] = new Pair<View, String>(binding.nohp,"nohp_trans");
                pairs[4] = new Pair<View, String>(binding.email,"email_trans");
                pairs[5] = new Pair<View, String>(binding.username,"username_trans");
                pairs[6] = new Pair<View, String>(binding.password,"password_trans");
                pairs[7] = new Pair<View, String>(binding.buttonRegister,"buttonRegister_trans");
                pairs[8] = new Pair<View, String>(binding.textView1,"textView1_trans");
                pairs[9] = new Pair<View, String>(binding.btnLogin,"btnLogin_trans");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(RegisterActivity.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });
    }
}