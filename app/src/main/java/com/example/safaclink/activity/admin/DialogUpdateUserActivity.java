package com.example.safaclink.activity.admin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.safaclink.R;
import com.example.safaclink.RegisterActivity;
import com.example.safaclink.apiserver.ApiServer;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class DialogUpdateUserActivity extends AppCompatDialogFragment {
    private Context context;
    private EditText editNama, editNohp, editEmail, editUsername, editPassword;
    private String id_user, nama, nohp, email, username, password, role;
    private Spinner editSpinnerRole;
    private static final String URL_UPDATE_USER =
            ApiServer.site_url_admin + "updateUser.php";

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_dialog_update_user, null);

        editNama = view.findViewById(R.id.editNama);
        editSpinnerRole = view.findViewById(R.id.editSpinnerRole);
        editNohp = view.findViewById(R.id.editNohp);
        editEmail = view.findViewById(R.id.editEmail);
        editUsername = view.findViewById(R.id.editUsername);
        editPassword = view.findViewById(R.id.editPassword);

        Bundle arguments = getArguments();
        if (arguments != null) {
            id_user = arguments.getString("id_user");
            nama = arguments.getString("nama");
            nohp = arguments.getString("nohp");
            email = arguments.getString("email");
            username = arguments.getString("username");
            password = arguments.getString("password");
            role = arguments.getString("role");

            editNama.setText(nama);
            editNohp.setText(nohp);
            int statusPosition = getIndexByValue(editSpinnerRole, role);
            editSpinnerRole.setSelection(statusPosition);
            editEmail.setText(email);
            editUsername.setText(username);
            editPassword.setText(password);
        }

        builder.setView(view)
                .setTitle("Update User")
                .setNegativeButton("Batal", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("Oke", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UpdateUser();
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public interface OnUserUpdatedListener {
        void onUserAdded(String message);
        void onUserError(String errorMessage);
    }

    private OnUserUpdatedListener onUserUpdatedListener;

    public void setOnUserUpdatedListener(OnUserUpdatedListener listener) {
        this.onUserUpdatedListener = listener;
    }

    private void UpdateUser() {
        String updatedName = editNama.getText().toString();
        String updatedNohp = editNohp.getText().toString();
        String updatedRole = editSpinnerRole.getSelectedItem().toString();
        String updatedEmail = editEmail.getText().toString();
        String updatedUsername = editUsername.getText().toString();
        String updatedPassword = editPassword.getText().toString();

        if (updatedName.isEmpty()) {
            editNama.setError("Nama tidak boleh kosong");
            showEmptyFieldsDialog("Nama");
            return;
        } else {
            editNama.setError(null);
        }

        if (updatedEmail.isEmpty()) {
            editEmail.setError("Email tidak boleh kosong");
            showEmptyFieldsDialog("Email");
            return;
        } else {
            editEmail.setError(null);
        }

        if (updatedNohp.isEmpty()) {
            editNohp.setError("Nomor HP tidak boleh kosong");
            showEmptyFieldsDialog("Nomor HP");
            return;
        } else {
            editNohp.setError(null);
        }

        if (updatedUsername.isEmpty()) {
            editUsername.setError("Username tidak boleh kosong");
            showEmptyFieldsDialog("Username");
            return;
        } else {
            editUsername.setError(null);
        }

        if (updatedPassword.isEmpty()) {
            editPassword.setError("Password tidak boleh kosong");
            showEmptyFieldsDialog("Password");
            return;
        } else {
            editPassword.setError(null);
        }

        editNama.setError(null);
        editEmail.setError(null);
        editUsername.setError(null);
        editNohp.setError(null);
        editPassword.setError(null);

        AndroidNetworking.post(URL_UPDATE_USER)
                .addBodyParameter("id_user", id_user)
                .addBodyParameter("nama", updatedName)
                .addBodyParameter("nohp", updatedNohp)
                .addBodyParameter("role", updatedRole)
                .addBodyParameter("email", updatedEmail)
                .addBodyParameter("username", updatedUsername)
                .addBodyParameter("password", updatedPassword)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("code").equals("1")) {
                                if (onUserUpdatedListener != null) {
                                    onUserUpdatedListener.onUserAdded("Update Data User Berhasil");
                                }
                            } else if (response.getString("code").equals("2")) {
                                if (onUserUpdatedListener != null) {
                                    onUserUpdatedListener.onUserError("Username telah Digunakan");
                                }
                            } else {
                                if (onUserUpdatedListener != null) {
                                    onUserUpdatedListener.onUserError("Gagal Update Data User");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        if (getActivity() != null) {
                            if (onUserUpdatedListener != null) {
                                onUserUpdatedListener.onUserError("Gagal Update Data User");
                            }
                        }
                    }
                });
    }

    private void showEmptyFieldsDialog(String fieldName) {
        PopupDialog.getInstance(context)
                .statusDialogBuilder()
                .createWarningDialog()
                .setHeading("WARNING !!!")
                .setDescription(fieldName + " tidak boleh kosong.")
                .setCancelable(false)
                .build(Dialog::dismiss)
                .show();
    }

    private int getIndexByValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                return i;
            }
        }
        return 0;
    }
}