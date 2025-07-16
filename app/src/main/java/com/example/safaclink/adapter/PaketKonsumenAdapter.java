package com.example.safaclink.adapter;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safaclink.R;
import com.example.safaclink.activity.konsumen.DialogPesanActivity;
import com.example.safaclink.model.PaketModel;
import com.saadahmedev.popupdialog.PopupDialog;
import com.saadahmedev.popupdialog.listener.StandardDialogActionListener;

import java.text.DecimalFormat;
import java.util.List;

public class PaketKonsumenAdapter extends RecyclerView.Adapter<PaketKonsumenAdapter.PaketModelViewHolder> implements DialogPesanActivity.OnPacketPesanListener {
    private Context context;
    List<PaketModel> paketModelList;

    public PaketKonsumenAdapter(Context context, List<PaketModel> paketModelList) {
        this.context = context;
        this.paketModelList = paketModelList;
    }

    @NonNull
    @Override
    public PaketKonsumenAdapter.PaketModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_paket_konsumen, parent, false);
        return new PaketKonsumenAdapter.PaketModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaketKonsumenAdapter.PaketModelViewHolder holder, int position) {
        PaketModel paketModel = paketModelList.get(position);

        if (holder.tvNamaPaket != null) {
            holder.tvNamaPaket.setText(paketModel.getNama_paket());
        }
        if (holder.tvKodePaket != null) {
            holder.tvKodePaket.setText(paketModel.getId_paket());
        }
        if (holder.tvHarga != null) {
            // Format harga dengan rupiah
            try {
                double harga = Double.parseDouble(paketModel.getHarga());
                holder.tvHarga.setText(formatRupiah(harga));
            } catch (NumberFormatException e) {
                holder.tvHarga.setText(paketModel.getHarga());
            }
        }
        if (holder.tvTipe != null) {
            holder.tvTipe.setText(paketModel.getTipe_paket());
        }
        if (holder.tvDeskripsi != null) {
            holder.tvDeskripsi.setText(paketModel.getDeskripsi());
        }

        // Click listeners
        holder.card.setOnClickListener(v -> showPesanDialog(paketModel));
        holder.btnPesan.setOnClickListener(v -> showPesanDialog(paketModel));

        if (holder.layoutUpdate != null) {
            holder.layoutUpdate.setOnClickListener(v -> showPesanDialog(paketModel));
        }
    }

    // Tambahkan method untuk format rupiah
    private String formatRupiah(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return "Rp " + formatter.format(amount);
    }

    private void showPesanDialog(PaketModel paketModel) {
        DialogPesanActivity dialog = new DialogPesanActivity();
        Bundle args = new Bundle();
        args.putString("id_paket", paketModel.getId_paket());
        args.putString("nama_paket", paketModel.getNama_paket());
        args.putString("tipe_paket", paketModel.getTipe_paket());
        args.putString("harga", paketModel.getHarga());
        args.putString("deskripsi", paketModel.getDeskripsi());
        dialog.setArguments(args);

        AppCompatActivity activity = (AppCompatActivity) context;
        dialog.setOnPacketPesanListener(this);
        dialog.showNow(activity.getSupportFragmentManager(), "pesan_paket");
    }

    @Override
    public void onPesanAdded(String message) {
        showSuccessNotification(message);
    }

    @Override
    public void onPesanError(String message) {
        showErrorNotification(message);
    }

    private void UpdatePaketList() {
        if (listener != null) {
            listener.onPaketListChanged();
        }
    }

    public interface OnPaketListChangedListener {
        void onPaketListChanged();
    }

    private OnPaketListChangedListener listener;

    public void setOnPaketListChangedListener(OnPaketListChangedListener listener) {
        this.listener = listener;
    }

    private void showSuccessNotification(String message) {
        PopupDialog.getInstance(context)
                .statusDialogBuilder()
                .createSuccessDialog()
                .setCancelable(false)
                .setHeading("Berhasil")
                .setDescription(message)
                .build(dialog -> {
                    UpdatePaketList();
                    dialog.dismiss();
                })
                .show();
    }

    private void showErrorNotification(String message) {
        PopupDialog.getInstance(context)
                .statusDialogBuilder()
                .createErrorDialog()
                .setHeading("GAGAL !!!")
                .setDescription(message)
                .setCancelable(true)
                .build(dialog -> {
                    UpdatePaketList();
                    dialog.dismiss();
                })
                .show();
    }

    public int getItemCount() {
        return paketModelList.size();
    }

    public class PaketModelViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaPaket, tvKodePaket, tvHarga, tvTipe, tvDeskripsi;
        CardView card;
        Button btnPesan;
        View layoutUpdate, layoutDelete;

        public PaketModelViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            tvNamaPaket = itemView.findViewById(R.id.tvNamaPaket);
            tvKodePaket = itemView.findViewById(R.id.tvKodePaket);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvTipe = itemView.findViewById(R.id.tvTipe);
            btnPesan = itemView.findViewById(R.id.btnPesan);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
            layoutUpdate = itemView.findViewById(R.id.cardUpdate);
        }
    }

}