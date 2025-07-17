package com.example.safaclink.activity.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.example.safaclink.MainActivity;
import com.example.safaclink.R;
import com.example.safaclink.activity.owner.MainOwnerActivity;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.apiserver.PrefManager;
import com.example.safaclink.databinding.ActivityMainAdminBinding;
import com.example.safaclink.databinding.ActivityMainBinding;
import com.saadahmedev.popupdialog.PopupDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MainAdminActivity extends AppCompatActivity {
    private ActivityMainAdminBinding binding;
    private PrefManager prefManager;
    private static final String URL_UPDATE_PROFILE = ApiServer.site_url_admin + "updateProfile.php";
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AndroidNetworking.initialize(this);
        prefManager = new PrefManager(this);
        initLoadingDialog();

        if (prefManager.getImg() == null || prefManager.getImg().equals("null")) {
            binding.imgProfile.setImageResource(R.mipmap.icon_user_foreground);
        } else {
            Picasso.get()
                    .load(ApiServer.site_url_fotoProfile + prefManager.getImg())
                    .into(binding.imgProfile);
        }
        binding.textNama.setText(prefManager.getNama());
        binding.txtRole.setText(prefManager.getTipe());

        // Untuk menampilkan PopupMenu
        ImageView btnDropdown = findViewById(R.id.btnDropdown);
        btnDropdown.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.dropdown_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_profile) {
                    // Intent ke ProfileActivity
                    showEditProfileDialog();
                    return true;
                } else if (item.getItemId() == R.id.menu_logout) {
                    // Logout logic
                    performLogout();
                    return true;
                }
                return false;
            });
            popup.show();
        });
        binding.cardUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainAdminActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });

        binding.cardPaket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainAdminActivity.this, PaketActivity.class);
                startActivity(intent);
            }
        });

        binding.cardTransaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainAdminActivity.this, TransaksiActivity.class);
                startActivity(intent);
            }
        });

        binding.cardLaporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainAdminActivity.this, LaporanActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showEditProfileDialog() {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        // Create layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Create EditText fields
        EditText etNama = new EditText(this);
        etNama.setHint("Nama");
        etNama.setText(prefManager.getNama());

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(prefManager.getEmail());

        EditText etNohp = new EditText(this);
        etNohp.setHint("No HP");
        etNohp.setText(prefManager.getNohp());

        EditText etUsername = new EditText(this);
        etUsername.setHint("Username");
        etUsername.setText(prefManager.getUsername());

        EditText etPassword = new EditText(this);
        etPassword.setHint("Password Baru (kosongkan jika tidak diubah)");
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Add views to layout
        layout.addView(etNama);
        layout.addView(etEmail);
        layout.addView(etNohp);
        layout.addView(etUsername);
        layout.addView(etPassword);

        builder.setView(layout);

        // Set buttons
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String nama = etNama.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String nohp = etNohp.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (nama.isEmpty() || email.isEmpty() || nohp.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi kecuali password", Toast.LENGTH_SHORT).show();
                return;
            }

            updateProfile(nama, email, nohp, username, password);
        });

        builder.setNegativeButton("Batal", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateProfile(String nama, String email, String nohp, String username, String password) {
        showLoading();

        AndroidNetworking.post(URL_UPDATE_PROFILE)
                .addBodyParameter("id_user", String.valueOf(prefManager.getId()))
                .addBodyParameter("nama", nama)
                .addBodyParameter("email", email)
                .addBodyParameter("nohp", nohp)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password) // Akan kosong jika tidak diubah
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        hideLoading();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("code").equals("1")) {
                                // Update berhasil
                                prefManager.setNama(nama);
                                prefManager.setEmail(email);
                                prefManager.setNohp(nohp);
                                prefManager.setUsername(username);

                                // Update UI
                                binding.textNama.setText(nama);

                                Toast.makeText(MainAdminActivity.this, "Profile berhasil diperbarui", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainAdminActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainAdminActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideLoading();
                        Toast.makeText(MainAdminActivity.this, "Terjadi kesalahan: " + anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initLoadingDialog() {
        loadingDialog = PopupDialog.getInstance(MainAdminActivity.this)
                .progressDialogBuilder()
                .createProgressDialog()
                .setTint(R.color.red)
                .setCancelable(false)
                .build().getDialog();
    }

    private void showLoading() {
        if (loadingDialog == null) {
            initLoadingDialog();
        }
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void performLogout() {
        // Kembali ke MainActivity dan clear semua activity stack
        Intent intent = new Intent(MainAdminActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}