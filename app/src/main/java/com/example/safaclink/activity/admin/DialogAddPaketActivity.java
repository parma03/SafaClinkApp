package com.example.safaclink.activity.admin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
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

public class DialogAddPaketActivity extends AppCompatDialogFragment {
    private Context context;
    private EditText et_nama_paket, et_tipe_paket, et_deskripsi, et_harga;
    private static final String URL_ADD = ApiServer.site_url_admin + "addPaket.php";

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_dialog_add_paket, null);

        et_nama_paket = view.findViewById(R.id.et_nama_paket);
        et_tipe_paket = view.findViewById(R.id.et_tipe_paket);
        et_deskripsi = view.findViewById(R.id.et_deskripsi);
        et_harga = view.findViewById(R.id.et_harga);

        builder.setView(view)
                .setTitle("Add Paket")
                .setNegativeButton("Batal", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("Oke", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddPaket();
                    }
                });

        return builder.create();
    }

    public interface OnPaketAddedListener {
        void onPaketAdded(String message);
        void onPaketError(String errorMessage);
    }

    private OnPaketAddedListener onPaketAddedListener;

    public void setOnPaketAddedListener(OnPaketAddedListener listener) {
        this.onPaketAddedListener = listener;
    }

    private void AddPaket() {
        String addNamaPaket = et_nama_paket.getText().toString();
        String addTipePaket = et_tipe_paket.getText().toString();
        String addDeskripsi = et_deskripsi.getText().toString();
        String addHarga = et_harga.getText().toString();

        if (addNamaPaket.isEmpty()) {
            et_nama_paket.setError("Nama Paket tidak boleh kosong");
            showEmptyFieldsDialog("Nama Paket");
            return;
        } else {
            et_nama_paket.setError(null);
        }

        if (addTipePaket.isEmpty()) {
            et_tipe_paket.setError("Tipe Paket tidak boleh kosong");
            showEmptyFieldsDialog("Tipe Paket");
            return;
        } else {
            et_tipe_paket.setError(null);
        }

        if (addDeskripsi.isEmpty()) {
            et_deskripsi.setError("Deskripsi tidak boleh kosong");
            showEmptyFieldsDialog("Deskripsi");
            return;
        } else {
            et_deskripsi.setError(null);
        }

        if (addHarga.isEmpty()) {
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

        AndroidNetworking.post(URL_ADD)
                .addBodyParameter("nama_paket", addNamaPaket)
                .addBodyParameter("tipe_paket", addTipePaket)
                .addBodyParameter("deskripsi", addDeskripsi)
                .addBodyParameter("harga", addHarga)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("code").equals("1")) {
                                if (onPaketAddedListener != null) {
                                    onPaketAddedListener.onPaketAdded("Add Data Paket Berhasil");
                                }
                            } else if (response.getString("code").equals("2")) {
                                if (onPaketAddedListener != null) {
                                    onPaketAddedListener.onPaketError("Paket Sudah Ada");
                                }
                            } else {
                                if (onPaketAddedListener != null) {
                                    onPaketAddedListener.onPaketError("Gagal Add Data Paket");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        if (getActivity() != null) {
                            if (onPaketAddedListener != null) {
                                onPaketAddedListener.onPaketError("Gagal Add Data Paket");
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