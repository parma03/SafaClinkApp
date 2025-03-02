package com.example.safaclink;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.databinding.ActivityRegisterBinding;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private Dialog loadingDialog;
    private static final String URL_REGISTER = ApiServer.site_url_admin + "register.php";

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
        AndroidNetworking.initialize(this);

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

        initLoadingDialog();
        hideLoading();

        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    private void register() {
        showLoading();

        String nama = Objects.requireNonNull(binding.txtNama.getText()).toString().trim();
        String nohp = Objects.requireNonNull(binding.txtNohp.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.txtEmail.getText()).toString().trim();
        String username = Objects.requireNonNull(binding.txtUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.txtPassword.getText()).toString().trim();

        if (nama.isEmpty() || nohp.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showEmptyFieldsDialog();
            hideLoading();
            return;
        }

        AndroidNetworking.post(URL_REGISTER)
                .addBodyParameter("nama", nama)
                .addBodyParameter("nohp", nohp)
                .addBodyParameter("email", email)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hideLoading();
                            if (response.getString("code").equals("1")) {
                                showSuccessRegister();
                            } else if (response.getString("code").equals("2")) {
                                registerGagalPopUp();
                            } else {
                                registerErrorPopUp();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideLoading();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        registerErrorPopUp();
                        hideLoading();
                    }
                });

    }

    private void initLoadingDialog() {
        loadingDialog = PopupDialog.getInstance(RegisterActivity.this)
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

    public void registerGagalPopUp() {
        PopupDialog.getInstance(RegisterActivity.this)
                .statusDialogBuilder()
                .createErrorDialog()
                .setHeading("WARNING !!!")
                .setDescription("Username sudah terdaftar")
                .setCancelable(false)
                .build(Dialog::dismiss)
                .show();
        hideLoading();
    }

    public void registerErrorPopUp() {
        PopupDialog.getInstance(RegisterActivity.this)
                .statusDialogBuilder()
                .createErrorDialog()
                .setHeading("WARNING !!!")
                .setDescription("Gagal Melakukan Pendaftaran")
                .setCancelable(false)
                .build(Dialog::dismiss)
                .show();
        hideLoading();
    }

    private void showEmptyFieldsDialog() {
        PopupDialog.getInstance(RegisterActivity.this)
                .statusDialogBuilder()
                .createWarningDialog()
                .setHeading("WARNING !!!")
                .setDescription("Form Wajib Diisi Semua!.")
                .setCancelable(false)
                .build(Dialog::dismiss)
                .show();
    }

    private void showSuccessRegister() {
        PopupDialog.getInstance(RegisterActivity.this)
                .statusDialogBuilder()
                .createSuccessDialog()
                .setCancelable(false)
                .setHeading("Well Done")
                .setDescription("Berhasil Registrasi, Silahkan Login")
                .build(dialog -> {
                    dialog.dismiss();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}