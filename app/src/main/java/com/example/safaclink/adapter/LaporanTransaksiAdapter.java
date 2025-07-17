package com.example.safaclink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safaclink.R;
import com.example.safaclink.model.CombinedOrderModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LaporanTransaksiAdapter extends RecyclerView.Adapter<LaporanTransaksiAdapter.ViewHolder> {
    private List<CombinedOrderModel> dataList;
    private NumberFormat rupiah;
    private SimpleDateFormat dateFormat;

    public LaporanTransaksiAdapter(List<CombinedOrderModel> dataList) {
        this.dataList = dataList;
        this.rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_laporan_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CombinedOrderModel item = dataList.get(position);

        holder.tvOrderId.setText(item.getOrder_id());
        holder.tvNamaPelanggan.setText(item.getNama_pelanggan());
        holder.tvNamaPaket.setText(item.getNama_paket());
        holder.tvTipePaket.setText(item.getTipe_paket());
        holder.tvJumlahItem.setText(item.getItem() + " item");
        holder.tvTotalHarga.setText(rupiah.format(Long.parseLong(item.getTotal_harga())));

        // Format tanggal
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(item.getCreated_at());
            if (date != null) {
                holder.tvTanggal.setText(dateFormat.format(date));
            }
        } catch (Exception e) {
            holder.tvTanggal.setText(item.getCreated_at());
        }

        // Status order
        String statusOrder = item.getStatus_order();
        if (statusOrder != null) {
            holder.tvStatusOrder.setText(statusOrder.toUpperCase());
            holder.tvStatusOrder.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatusOrder.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvNamaPelanggan, tvNamaPaket, tvTipePaket,
                tvJumlahItem, tvTotalHarga, tvTanggal, tvStatusOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvNamaPelanggan = itemView.findViewById(R.id.tvNamaPelanggan);
            tvNamaPaket = itemView.findViewById(R.id.tvNamaPaket);
            tvTipePaket = itemView.findViewById(R.id.tvTipePaket);
            tvJumlahItem = itemView.findViewById(R.id.tvJumlahItem);
            tvTotalHarga = itemView.findViewById(R.id.tvTotalHarga);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvStatusOrder = itemView.findViewById(R.id.tvStatusOrder);
        }
    }
}