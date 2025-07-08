package com.example.safaclink.activity.admin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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
import com.example.safaclink.apiserver.ApiServer;
import com.saadahmedev.popupdialog.PopupDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class DialogUpdatePaketActivity extends AppCompatDialogFragment {
    private Context context;
    private EditText et_nama_paket, et_tipe_paket, et_deskripsi, et_harga;
    private String id_paket, nama_paket, tipe_paket, deskripsi, harga;
    private static final String URL_UPDATE =
            ApiServer.site_url_admin + "updatePaket.php";

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_dialog_update_paket, null);

        et_nama_paket = view.findViewById(R.id.et_nama_paket);
        et_tipe_paket = view.findViewById(R.id.et_tipe_paket);
        et_deskripsi = view.findViewById(R.id.et_deskripsi);
        et_harga = view.findViewById(R.id.et_harga);

        Bundle arguments = getArguments();
        if (arguments != null) {
            id_paket = arguments.getString("id_paket");
            nama_paket = arguments.getString("nama_paket");
            tipe_paket = arguments.getString("tipe_paket");
            deskripsi = arguments.getString("deskripsi");
            harga = arguments.getString("harga");

            et_nama_paket.setText(nama_paket);
            et_tipe_paket.setText(tipe_paket);
            et_deskripsi.setText(deskripsi);
            et_harga.setText(harga);
        }

        builder.setView(view)
                .setTitle("Updated Paket")
                .setNegativeButton("Batal", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("Oke", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UpdatePaket();
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public interface OnPaketUpdatedListener {
        void onPaketAdded(String message);
        void onPaketError(String errorMessage);
    }

    private OnPaketUpdatedListener onPaketUpdatedListener;

    public void setOnPaketUpdatedListener(OnPaketUpdatedListener listener) {
        this.onPaketUpdatedListener = listener;
    }

    private void UpdatePaket() {
        String updatedNamePaket = et_nama_paket.getText().toString();
        String updatedTiperPaket = et_tipe_paket.getText().toString();
        String updatedDeskripsiPaket = et_deskripsi.getText().toString();
        String updatedHargaPaket = et_harga.getText().toString();

        if (updatedNamePaket.isEmpty()) {
            et_nama_paket.setError("Nama Paket tidak boleh kosong");
            showEmptyFieldsDialog("Nama Paket");
            return;
        } else {
            et_nama_paket.setError(null);
        }

        if (updatedTiperPaket.isEmpty()) {
            et_tipe_paket.setError("Tipe Paket tidak boleh kosong");
            showEmptyFieldsDialog("Tipe Paket");
            return;
        } else {
            et_tipe_paket.setError(null);
        }

        if (updatedDeskripsiPaket.isEmpty()) {
            et_deskripsi.setError("Deskripsi tidak boleh kosong");
            showEmptyFieldsDialog("Deskripsi");
            return;
        } else {
            et_deskripsi.setError(null);
        }

        if (updatedHargaPaket.isEmpty()) {
            et_harga.setError("Harga tidak boleh kosong");
            showEmptyFieldsDialog("Harga");
            return;
        } else {
            et_harga.setError(null);
        }

        et_nama_paket.setError(null);
        et_harga.setError(null);
        et_tipe_paket.setError(null);
        et_deskripsi.setError(null);

        AndroidNetworking.post(URL_UPDATE)
                .addBodyParameter("id_paket", id_paket)
                .addBodyParameter("nama_paket", updatedNamePaket)
                .addBodyParameter("tipe_paket", updatedTiperPaket)
                .addBodyParameter("deskripsi", updatedDeskripsiPaket)
                .addBodyParameter("harga", updatedHargaPaket)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("code").equals("1")) {
                                if (onPaketUpdatedListener != null) {
                                    onPaketUpdatedListener.onPaketAdded("Update Data Paket Berhasil");
                                }
                            } else if (response.getString("code").equals("2")) {
                                if (onPaketUpdatedListener != null) {
                                    onPaketUpdatedListener.onPaketError("Paket Sudah Ada");
                                }
                            } else {
                                if (onPaketUpdatedListener != null) {
                                    onPaketUpdatedListener.onPaketError("Gagal Update Data Paket");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        if (getActivity() != null) {
                            if (onPaketUpdatedListener != null) {
                                onPaketUpdatedListener.onPaketError("Gagal Update Data Paket");
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
}