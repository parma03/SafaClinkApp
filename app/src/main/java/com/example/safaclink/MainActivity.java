package com.example.safaclink;

import android.app.ActivityOptions;
import android.app.AlertDialog;
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
import com.example.safaclink.activity.admin.MainAdminActivity;
import com.example.safaclink.activity.konsumen.MainKonsumenActivity;
import com.example.safaclink.activity.owner.MainOwnerActivity;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.apiserver.PrefManager;
import com.example.safaclink.databinding.ActivityMainBinding;
import com.example.safaclink.databinding.ActivityRegisterBinding;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PrefManager prefManager;
    private Dialog loadingDialog, loadingMigration, successMigration, errorMigration;
    private static final String URL_CHECK_LOGIN_ADMIN = ApiServer.site_url_admin + "checkLogin.php";
    private static final String URL_CHECK_LOGIN_PELANGGAN = ApiServer.site_url_konsumen + "checkLogin.php";
    private static final String URL_CHECK_LOGIN_OWNER = ApiServer.site_url_owner + "checkLogin.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AndroidNetworking.initialize(this);
        prefManager = new PrefManager(this);

        checkDatabase();

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);

                Pair [] pairs = new Pair[7];
                pairs[0] = new Pair<View, String>(binding.logoImage,"logo_image");
                pairs[1] = new Pair<View, String>(binding.logoName1,"logo_text");
                pairs[2] = new Pair<View, String>(binding.username,"username_trans");
                pairs[3] = new Pair<View, String>(binding.password,"password_trans");
                pairs[4] = new Pair<View, String>(binding.buttonLogin,"buttonLogin_trans");
                pairs[5] = new Pair<View, String>(binding.textView1,"textView1_trans");
                pairs[6] = new Pair<View, String>(binding.btnRegister,"btnRegister_trans");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void checkDatabase() {
        loadingMigration().show();

        // Define URL for checking database existence
        String URL_CHECK_DB = ApiServer.db_set + "checkdb.php";

        AndroidNetworking.get(URL_CHECK_DB)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingMigration.dismiss();
                        try {
                            // If response contains success code, database exists
                            if (response.has("success") && response.getBoolean("success")) {
                                // Database exists, continue with normal app flow
                                // No need to show successMigration dialog here
                            } else {
                                // Database doesn't exist, perform migration
                                migrateDatabase();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorMigration().show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loadingMigration.dismiss();
                        errorMigration().show();
                    }
                });
    }

    private void migrateDatabase() {
        loadingMigration().show();

        // Define URL for database migration
        String URL_MIGRATE_DB = ApiServer.db_set + "migratedb.php";

        AndroidNetworking.get(URL_MIGRATE_DB)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingMigration.dismiss();
                        try {
                            if (response.has("success") && response.getBoolean("success")) {
                                // Migration successful - ONLY show success dialog here
                                successMigration().show();
                            } else {
                                // Migration failed
                                errorMigration().show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorMigration().show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loadingMigration.dismiss();
                        errorMigration().show();
                    }
                });
    }

    private void login() {
        loadingPopUp().show();

        String username = Objects.requireNonNull(binding.txtUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.txtPassword.getText()).toString();

        if (username.isEmpty() || password.isEmpty()) {
            showEmptyFieldsDialog();
            loadingPopUp().dismiss();
        }

        AndroidNetworking.post(URL_CHECK_LOGIN_ADMIN)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loadingDialog.dismiss();
                            if (response.getString("code").equals("1")) {
                                JSONArray array = response.getJSONArray("data");
                                JSONObject object = array.getJSONObject(0);
                                String id = object.getString("id_user");
                                prefManager.setId(id);
                                prefManager.setNama(object.getString("nama"));
                                prefManager.setUsername(object.getString("email"));
                                prefManager.setPassword(object.getString("password"));
                                prefManager.setNohp(object.getString("nohp"));
                                prefManager.setEmail(object.getString("email"));
                                prefManager.setImg(object.getString("profile"));
                                prefManager.setLoginStatus(true);
                                prefManager.setTipe("Administrator");
                                Intent intent = new Intent(MainActivity.this, MainAdminActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                loginKonsumen();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loadingDialog.dismiss();
                    }
                });
    }

    private void loginKonsumen() {
        loadingPopUp().show();

        String username = Objects.requireNonNull(binding.txtUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.txtPassword.getText()).toString();

        if (username.isEmpty() || password.isEmpty()) {
            showEmptyFieldsDialog();
            loadingPopUp().dismiss();
        }

        AndroidNetworking.post(URL_CHECK_LOGIN_ADMIN)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loadingDialog.dismiss();
                            if (response.getString("code").equals("1")) {
                                JSONArray array = response.getJSONArray("data");
                                JSONObject object = array.getJSONObject(0);
                                String id = object.getString("id_user");
                                prefManager.setId(id);
                                prefManager.setNama(object.getString("nama"));
                                prefManager.setUsername(object.getString("email"));
                                prefManager.setPassword(object.getString("password"));
                                prefManager.setNohp(object.getString("nohp"));
                                prefManager.setEmail(object.getString("email"));
                                prefManager.setImg(object.getString("profile"));
                                prefManager.setLoginStatus(true);
                                prefManager.setTipe("Konsumen");
                                Intent intent = new Intent(MainActivity.this, MainKonsumenActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                loginOwner();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loadingDialog.dismiss();
                    }
                });
    }

    private void loginOwner() {
        loadingPopUp().show();

        String username = Objects.requireNonNull(binding.txtUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.txtPassword.getText()).toString();

        if (username.isEmpty() || password.isEmpty()) {
            showEmptyFieldsDialog();
            loadingPopUp().dismiss();
        }

        AndroidNetworking.post(URL_CHECK_LOGIN_ADMIN)
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loadingDialog.dismiss();
                            if (response.getString("code").equals("1")) {
                                JSONArray array = response.getJSONArray("data");
                                JSONObject object = array.getJSONObject(0);
                                String id = object.getString("id_user");
                                prefManager.setId(id);
                                prefManager.setNama(object.getString("nama"));
                                prefManager.setUsername(object.getString("email"));
                                prefManager.setPassword(object.getString("password"));
                                prefManager.setNohp(object.getString("nohp"));
                                prefManager.setEmail(object.getString("email"));
                                prefManager.setImg(object.getString("profile"));
                                prefManager.setLoginStatus(true);
                                prefManager.setTipe("Owner");
                                Intent intent = new Intent(MainActivity.this, MainOwnerActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                loginGagalPopUp();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loadingDialog.dismiss();
                    }
                });
    }

    private Dialog loadingMigration() {
        loadingMigration = PopupDialog.getInstance(MainActivity.this)
                .progressDialogBuilder()
                .createLottieDialog()
                .setRawRes(R.raw.success)
                .setCancelable(false)
                .build().getDialog();
        return loadingMigration;
    }

    private Dialog successMigration() {
        successMigration = PopupDialog.getInstance(MainActivity.this)
                .statusDialogBuilder()
                .createSuccessDialog()
                .setCancelable(false)
                .setHeading("Well Done")
                .setDescription("Migration Completed")
                .build(Dialog::dismiss).getDialog();
        return successMigration;
    }

    private Dialog errorMigration() {
        errorMigration = PopupDialog.getInstance(MainActivity.this)
                .progressDialogBuilder()
                .createLottieDialog()
                .setCancelable(false)
                .setRawRes(R.raw.success)
                .build().getDialog();
        return errorMigration;
    }

    private Dialog loadingPopUp() {
        loadingDialog = PopupDialog.getInstance(MainActivity.this)
                .progressDialogBuilder()
                .createProgressDialog()
                .setTint(R.color.red)
                .setCancelable(false)
                .build().getDialog();
        return loadingDialog;
    }

    public void loginGagalPopUp() {
        PopupDialog.getInstance(MainActivity.this)
                .statusDialogBuilder()
                .createErrorDialog()
                .setHeading("WARNING !!!")
                .setDescription("Username atau Password Salah")
                .setCancelable(false)
                .build(Dialog::dismiss)
                .show();
    }

    private void showEmptyFieldsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Isi Username Dan Password.");
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}