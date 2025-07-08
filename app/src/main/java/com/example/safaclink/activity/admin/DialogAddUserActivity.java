package com.example.safaclink.activity.admin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.safaclink.R;
import com.example.safaclink.apiserver.ApiServer;

import org.json.JSONException;
import org.json.JSONObject;

public class DialogAddUserActivity extends AppCompatDialogFragment {
    private EditText editNama, editNohp, editEmail, editUsername, editPassword;
    private static final String URL_ADD_ADMIN = ApiServer.site_url_admin + "addAdmin.php";
    private static final String URL_ADD_KONSUMEN = ApiServer.site_url_admin + "addKonsumen.php";
    private static final String URL_ADD_OWNER = ApiServer.site_url_admin + "addOwner.php";
    private String userType;

    public static DialogAddUserActivity newInstance(String userType) {
        DialogAddUserActivity dialog = new DialogAddUserActivity();
        Bundle args = new Bundle();
        args.putString("user_type", userType);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userType = getArguments().getString("user_type", "admin");
        } else {
            userType = "admin"; // default
        }
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_dialog_add_user, null);

        editNama = view.findViewById(R.id.editNama);
        editNohp = view.findViewById(R.id.editNohp);
        editEmail = view.findViewById(R.id.editEmail);
        editUsername = view.findViewById(R.id.editUsername);
        editPassword = view.findViewById(R.id.editPassword);

        String dialogTitle = "Add " + userType.substring(0, 1).toUpperCase() + userType.substring(1);

        builder.setView(view)
                .setTitle(dialogTitle)
                .setNegativeButton("Batal", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("Oke", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddUser();
                    }
                });

        return builder.create();
    }

    public interface OnUserAddedListener {
        void onUserAdded(String message);
        void onUserError(String errorMessage);
    }

    private OnUserAddedListener onUserAddedListener;

    public void setOnUserAddedListener(OnUserAddedListener listener) {
        this.onUserAddedListener = listener;
    }

    private String getAddUserURL() {
        switch (userType.toLowerCase()) {
            case "konsumen":
                return URL_ADD_KONSUMEN;
            case "owner":
                return URL_ADD_OWNER;
            case "admin":
            default:
                return URL_ADD_ADMIN;
        }
    }

    private void AddUser(){
        String addName = editNama.getText().toString();
        String addNohp = editNohp.getText().toString();
        String addEmail = editEmail.getText().toString();
        String addUsername = editUsername.getText().toString();
        String addPassword = editPassword.getText().toString();

        if (addName.isEmpty()) {
            editNama.setError("Nama tidak boleh kosong");
            return;
        } else {
            editNama.setError(null);
        }

        if (addEmail.isEmpty()) {
            editEmail.setError("Email tidak boleh kosong");
            return;
        } else {
            editEmail.setError(null);
        }

        if (addNohp.isEmpty()) {
            editNohp.setError("Nomor HP tidak boleh kosong");
            return;
        } else {
            editNohp.setError(null);
        }

        if (addUsername.isEmpty()) {
            editUsername.setError("Username tidak boleh kosong");
            return;
        } else {
            editUsername.setError(null);
        }

        if (addPassword.isEmpty()) {
            editPassword.setError("Password tidak boleh kosong");
            return;
        } else {
            editPassword.setError(null);
        }

        editNama.setError(null);
        editEmail.setError(null);
        editUsername.setError(null);
        editNohp.setError(null);
        editPassword.setError(null);

        // Gunakan URL yang sesuai berdasarkan tipe user
        String urlToUse = getAddUserURL();
        Log.d("AddUser", "Using URL: " + urlToUse + " for user type: " + userType);

        AndroidNetworking.post(urlToUse)
                .addBodyParameter("nama", addName)
                .addBodyParameter("nohp", addNohp)
                .addBodyParameter("email", addEmail)
                .addBodyParameter("username", addUsername)
                .addBodyParameter("password", addPassword)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("response", "response::" + response);
                            if (response.getString("code").equals("1")) {
                                if (onUserAddedListener != null) {
                                    onUserAddedListener.onUserAdded("Add " + userType + " Berhasil");
                                }
                            } else if (response.getString("code").equals("2")) {
                                if (onUserAddedListener != null) {
                                    onUserAddedListener.onUserError("Username Sudah Terpakai");
                                }
                            } else {
                                if (onUserAddedListener != null) {
                                    onUserAddedListener.onUserError("Add " + userType + " Gagal");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (onUserAddedListener != null) {
                                onUserAddedListener.onUserError("Add " + userType + " Gagal");
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("response", "eror::" + anError);
                        if (onUserAddedListener != null) {
                            onUserAddedListener.onUserError("Tambah Data " + userType + " GAGAL");
                        }
                    }
                });
    }
}