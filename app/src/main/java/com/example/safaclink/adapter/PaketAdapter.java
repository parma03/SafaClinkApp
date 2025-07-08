package com.example.safaclink.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.safaclink.R;
import com.example.safaclink.activity.admin.DialogDetailPaketActivity;
import com.example.safaclink.activity.admin.DialogUpdatePaketActivity;
import com.example.safaclink.apiserver.ApiServer;
import com.example.safaclink.model.PaketModel;
import com.saadahmedev.popupdialog.PopupDialog;
import com.saadahmedev.popupdialog.listener.StandardDialogActionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PaketAdapter extends RecyclerView.Adapter<PaketAdapter.PaketModelViewHolder> implements DialogUpdatePaketActivity.OnPaketUpdatedListener {
    private Context context;
    List<PaketModel> paketModelList;
    private PaketModelViewHolder currentOpenViewHolder;

    public PaketAdapter(Context context, List<PaketModel> paketModelList) {
        this.context = context;
        this.paketModelList = paketModelList;
    }

    @NonNull
    @Override
    public PaketAdapter.PaketModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_paket, parent, false);
        return new PaketAdapter.PaketModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaketAdapter.PaketModelViewHolder holder, int position) {
        PaketModel paketModel = paketModelList.get(position);
        if (holder.tvNamaPaket != null) {
            holder.tvNamaPaket.setText(paketModel.getNama_paket());
        }
        if (holder.tvKodePaket != null) {
            holder.tvKodePaket.setText(paketModel.getId_paket());
        }
        if (holder.tvHarga != null) {
            holder.tvHarga.setText(paketModel.getHarga());
        }
        if (holder.tvTipe != null) {
            holder.tvTipe.setText(paketModel.getTipe_paket());
        }
        if (holder.tvDeskripsi != null) {
            holder.tvDeskripsi.setText(paketModel.getDeskripsi());
        }

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPaketDetailDialog(paketModel);
            }
        });

        // Perbaikan: Menggunakan layoutUpdate dan layoutDelete (bukan cardUpdate dan cardDelete)
        holder.layoutUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateDialog(paketModel);
            }
        });

        holder.layoutDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupDialog.getInstance(context)
                        .standardDialogBuilder()
                        .createAlertDialog()
                        .setHeading("Konfigurasi Hapus")
                        .setDescription("Data Paket akan dihapus, lanjutkan ?")
                        .build(new StandardDialogActionListener() {
                            @Override
                            public void onPositiveButtonClicked(Dialog dialog) {
                                hapusData(paketModel.id_paket);
                                paketModelList.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                                dialog.dismiss();
                            }

                            @Override
                            public void onNegativeButtonClicked(Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    private void showUpdateDialog(PaketModel paketModel) {
        DialogUpdatePaketActivity dialog = new DialogUpdatePaketActivity();
        Bundle args = new Bundle();
        args.putString("id_paket", paketModel.getId_paket());
        args.putString("nama_paket", paketModel.getNama_paket());
        args.putString("tipe_paket", paketModel.getTipe_paket());
        args.putString("harga", paketModel.getHarga());
        args.putString("deskripsi", paketModel.getDeskripsi());
        dialog.setArguments(args);

        AppCompatActivity activity = (AppCompatActivity) context;
        dialog.setOnPaketUpdatedListener(this);
        dialog.showNow(activity.getSupportFragmentManager(), "update_paket");
    }

    private void showPaketDetailDialog(PaketModel paketModel) {
        DialogDetailPaketActivity dialog = new DialogDetailPaketActivity();
        Bundle args = new Bundle();
        args.putString("nama_paket", paketModel.getNama_paket());
        args.putString("tipe_paket", paketModel.getTipe_paket());
        args.putString("harga", paketModel.getHarga());
        args.putString("deskripsi", paketModel.getDeskripsi());
        dialog.setArguments(args);

        AppCompatActivity activity = (AppCompatActivity) context;
        dialog.showNow(activity.getSupportFragmentManager(), "detail_paket");
    }

    @Override
    public void onPaketAdded(String message) {
        showSuccessNotification(message);
    }

    @Override
    public void onPaketError(String errorMessage) {
        showErrorNotification(errorMessage);
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

    public void hapusData(String id_paket) {
        if (paketModelList.isEmpty()) {
            Toast.makeText(context, "Data Paket kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        AndroidNetworking.post(ApiServer.site_url_admin + "deletePaket.php")
                .addBodyParameter("id_paket", id_paket)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            if ("Paket berhasil dihapus".equals(message)) {
                                PopupDialog.getInstance(context)
                                        .statusDialogBuilder()
                                        .createSuccessDialog()
                                        .setHeading("BERHASIL !!!")
                                        .setDescription("Hapus Data Paket Berhasil")
                                        .setCancelable(false)
                                        .build(Dialog::dismiss)
                                        .show();
                            } else {
                                PopupDialog.getInstance(context)
                                        .statusDialogBuilder()
                                        .createSuccessDialog()
                                        .setHeading("GAGAL !!!")
                                        .setDescription("Gagal Hapus Data Paket")
                                        .setCancelable(false)
                                        .build(Dialog::dismiss)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            PopupDialog.getInstance(context)
                                    .statusDialogBuilder()
                                    .createSuccessDialog()
                                    .setHeading("GAGAL !!!")
                                    .setDescription("Gagal Hapus Data Paket")
                                    .setCancelable(false)
                                    .build(Dialog::dismiss)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        PopupDialog.getInstance(context)
                                .statusDialogBuilder()
                                .createSuccessDialog()
                                .setHeading("GAGAL !!!")
                                .setDescription("Gagal Hapus : "+ anError.getErrorBody())
                                .setCancelable(false)
                                .build(Dialog::dismiss)
                                .show();
                        Toast.makeText(context, "Hapus gagal: " + anError.getErrorBody(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public class PaketModelViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaPaket, tvKodePaket, tvHarga, tvTipe, tvDeskripsi;
        CardView card;
        // Perbaikan: Menggunakan View (bukan CardView) untuk konsistensi dengan UserAdapter
        View layoutUpdate, layoutDelete;

        public PaketModelViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            tvNamaPaket = itemView.findViewById(R.id.tvNamaPaket);
            tvKodePaket = itemView.findViewById(R.id.tvKodePaket);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvTipe = itemView.findViewById(R.id.tvTipe);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
            layoutUpdate = itemView.findViewById(R.id.cardUpdate);
            layoutDelete = itemView.findViewById(R.id.cardDelete);
        }
    }
}