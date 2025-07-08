package com.example.safaclink.activity.admin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.safaclink.R;

public class DialogDetailPaketActivity extends AppCompatDialogFragment {
    private Context context;
    private TextView textNamaPaket, textTipePaket, textHarga, textDeskripsi;
    private String nama_paket, tipe_paket, harga, deskripsi;

    @SuppressLint("MissingInflatedId")
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_dialog_detail_paket, null);

        // Inisialisasi TextView
        textNamaPaket = view.findViewById(R.id.textNamaPaket);
        textTipePaket = view.findViewById(R.id.textTipePaket);
        textHarga = view.findViewById(R.id.textHarga);
        textDeskripsi = view.findViewById(R.id.textDeskripsi);

        // Mendapatkan data dari Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            nama_paket = arguments.getString("nama_paket");
            tipe_paket = arguments.getString("tipe_paket");
            harga = arguments.getString("harga");
            deskripsi = arguments.getString("deskripsi");

            // Set data ke TextView
            textNamaPaket.setText(nama_paket != null ? nama_paket : "");
            textTipePaket.setText(tipe_paket != null ? tipe_paket : "");
            textHarga.setText(harga != null ? harga : "");
            textDeskripsi.setText(deskripsi != null ? deskripsi : "");
        }

        builder.setView(view)
                .setTitle("Detail Paket")
                .setPositiveButton("Oke", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}