package com.example.safaclink.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safaclink.R;
import com.example.safaclink.model.CombinedOrderModel;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderKonsumenAdapter extends RecyclerView.Adapter<OrderKonsumenAdapter.OrdersModelViewHolder> {
    private static final String TAG = "OrderKonsumenAdapter";
    private Context context;
    private List<CombinedOrderModel> combinedOrderModels;
    private OnPrintInvoiceListener printInvoiceListener;
    private OnKonfirmasiListener konfirmasiListener;
    private String userRole; // "admin" atau "konsumen"

    public OrderKonsumenAdapter(Context context, List<CombinedOrderModel> combinedOrderModels) {
        this.context = context;
        this.combinedOrderModels = combinedOrderModels;
        this.userRole = "konsumen"; // default
    }

    public OrderKonsumenAdapter(Context context, List<CombinedOrderModel> combinedOrderModels, String userRole) {
        this.context = context;
        this.combinedOrderModels = combinedOrderModels;
        this.userRole = userRole;
    }

    // Interface untuk handle print invoice
    public interface OnPrintInvoiceListener {
        void onPrintInvoice(CombinedOrderModel orderModel);
    }

    // Interface untuk handle konfirmasi
    public interface OnKonfirmasiListener {
        void onKonfirmasi(CombinedOrderModel orderModel);
    }

    public void setOnPrintInvoiceListener(OnPrintInvoiceListener listener) {
        this.printInvoiceListener = listener;
    }

    public void setOnKonfirmasiListener(OnKonfirmasiListener listener) {
        this.konfirmasiListener = listener;
    }

    @NonNull
    @Override
    public OrderKonsumenAdapter.OrdersModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_orders_konsumen, parent, false);
        return new OrderKonsumenAdapter.OrdersModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersModelViewHolder holder, int position) {
        CombinedOrderModel combinedOrderModel = combinedOrderModels.get(position);

        // Set nama paket
        if (holder.tvNamaPaket != null && combinedOrderModel.getNama_paket() != null) {
            holder.tvNamaPaket.setText(combinedOrderModel.getNama_paket());
        }

        // Set kode paket (order_id)
        if (holder.tvKodePaket != null && combinedOrderModel.getOrder_id() != null) {
            holder.tvKodePaket.setText(combinedOrderModel.getOrder_id());
        }

        // Set harga dengan format rupiah
        if (holder.tvHarga != null && combinedOrderModel.getTotal_harga() != null) {
            String harga = formatRupiah(combinedOrderModel.getTotal_harga());
            holder.tvHarga.setText(harga);
        }

        // Set tipe paket
        if (holder.tvTipe != null && combinedOrderModel.getTipe_paket() != null) {
            holder.tvTipe.setText(combinedOrderModel.getTipe_paket());
        }

        // Set deskripsi
        if (holder.tvDeskripsi != null && combinedOrderModel.getPaket_deskripsi() != null) {
            holder.tvDeskripsi.setText(combinedOrderModel.getPaket_deskripsi());
        }

        // Set status order
        if (holder.tvStatus != null) {
            String status = getStatusText(combinedOrderModel.getStatus_order());
            holder.tvStatus.setText(status);
        }

        // Set foto barang dari Base64
        if (combinedOrderModel.getFoto_barang() != null &&
                !combinedOrderModel.getFoto_barang().equals("null") &&
                !combinedOrderModel.getFoto_barang().isEmpty()) {

            try {
                // Decode base64 string to bitmap
                byte[] decodedString = Base64.decode(combinedOrderModel.getFoto_barang(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgFoto.setImageBitmap(decodedByte);
            } catch (Exception e) {
                // Jika gagal decode, gunakan placeholder
                holder.imgFoto.setImageResource(R.mipmap.icon_package_foreground);
            }
        } else {
            // Gunakan placeholder jika tidak ada foto
            holder.imgFoto.setImageResource(R.mipmap.icon_package_foreground);
        }

        // Handle button cetak invoice
        if (holder.btnCetakInvoice != null) {
            holder.btnCetakInvoice.setOnClickListener(v -> {
                if (printInvoiceListener != null) {
                    printInvoiceListener.onPrintInvoice(combinedOrderModel);
                }
            });
        }

        // Handle button konfirmasi berdasarkan role dan status
        setupKonfirmasiButton(holder, combinedOrderModel, position);

        // Hide swipe actions for konsumen view
        if (holder.layoutDelete != null) {
            holder.layoutDelete.setVisibility(View.GONE);
        }
        if (holder.layoutUpdate != null) {
            holder.layoutUpdate.setVisibility(View.GONE);
        }
    }

    private void setupKonfirmasiButton(OrdersModelViewHolder holder, CombinedOrderModel combinedOrderModel, int position) {
        if (holder.btnKonfirmasi == null) {
            Log.w(TAG, "btnKonfirmasi is null at position " + position);
            return;
        }

        String statusOrder = combinedOrderModel.getStatus_order();
        String statusTransaksi = combinedOrderModel.getStatus_transaksi();

        // Log untuk debugging
        Log.d(TAG, "Position: " + position +
                ", Order ID: " + combinedOrderModel.getOrder_id() +
                ", Status Order: " + statusOrder +
                ", Status Transaksi: " + statusTransaksi +
                ", User Role: " + userRole);

        // Normalize null values
        String normalizedStatusOrder = (statusOrder == null || statusOrder.equals("null") || statusOrder.isEmpty()) ? null : statusOrder;
        String normalizedStatusTransaksi = (statusTransaksi == null || statusTransaksi.equals("null") || statusTransaksi.isEmpty()) ? null : statusTransaksi;

        if ("admin".equals(userRole)) {
            // Logic untuk Admin
            if (normalizedStatusOrder == null && "paid".equals(normalizedStatusTransaksi)) {
                // Pesanan baru dibayar, belum ada di tb_orders
                holder.btnKonfirmasi.setVisibility(View.VISIBLE);
                holder.btnKonfirmasi.setText("Jemput Pesanan");
                holder.btnKonfirmasi.setOnClickListener(v -> {
                    Log.d(TAG, "Button Jemput Pesanan clicked for order: " + combinedOrderModel.getOrder_id());
                    if (konfirmasiListener != null) {
                        konfirmasiListener.onKonfirmasi(combinedOrderModel);
                    } else {
                        Log.w(TAG, "konfirmasiListener is null");
                    }
                });
                Log.d(TAG, "Button set to: Jemput Pesanan");

            } else if ("dijemput".equals(normalizedStatusOrder)) {
                // Admin bisa mengerjakan pesanan
                holder.btnKonfirmasi.setVisibility(View.VISIBLE);
                holder.btnKonfirmasi.setText("Kerjakan Pesanan");
                holder.btnKonfirmasi.setOnClickListener(v -> {
                    Log.d(TAG, "Button Kerjakan Pesanan clicked for order: " + combinedOrderModel.getOrder_id());
                    if (konfirmasiListener != null) {
                        konfirmasiListener.onKonfirmasi(combinedOrderModel);
                    }
                });
                Log.d(TAG, "Button set to: Kerjakan Pesanan");

            } else if ("dikerjakan".equals(normalizedStatusOrder)) {
                // Admin bisa mengantar pesanan
                holder.btnKonfirmasi.setVisibility(View.VISIBLE);
                holder.btnKonfirmasi.setText("Antar Pesanan");
                holder.btnKonfirmasi.setOnClickListener(v -> {
                    Log.d(TAG, "Button Antar Pesanan clicked for order: " + combinedOrderModel.getOrder_id());
                    if (konfirmasiListener != null) {
                        konfirmasiListener.onKonfirmasi(combinedOrderModel);
                    }
                });
                Log.d(TAG, "Button set to: Antar Pesanan");

            } else if ("dikonfirmasi".equals(normalizedStatusOrder)) {
                // Admin bisa menyelesaikan pesanan
                holder.btnKonfirmasi.setVisibility(View.VISIBLE);
                holder.btnKonfirmasi.setText("Selesaikan Pesanan");
                holder.btnKonfirmasi.setOnClickListener(v -> {
                    Log.d(TAG, "Button Selesaikan Pesanan clicked for order: " + combinedOrderModel.getOrder_id());
                    if (konfirmasiListener != null) {
                        konfirmasiListener.onKonfirmasi(combinedOrderModel);
                    }
                });
                Log.d(TAG, "Button set to: Selesaikan Pesanan");

            } else {
                // Status lainnya (diantar, selesai) - hide button
                holder.btnKonfirmasi.setVisibility(View.GONE);
                Log.d(TAG, "Button hidden for status: " + normalizedStatusOrder);
            }
        } else {
            // Logic untuk Konsumen
            if ("diantar".equals(normalizedStatusOrder)) {
                // Konsumen bisa konfirmasi penerimaan
                holder.btnKonfirmasi.setVisibility(View.VISIBLE);
                holder.btnKonfirmasi.setText("Konfirmasi Diterima");
                holder.btnKonfirmasi.setOnClickListener(v -> {
                    Log.d(TAG, "Button Konfirmasi Diterima clicked for order: " + combinedOrderModel.getOrder_id());
                    if (konfirmasiListener != null) {
                        konfirmasiListener.onKonfirmasi(combinedOrderModel);
                    }
                });
                Log.d(TAG, "Button set to: Konfirmasi Diterima");
            } else {
                // Status lainnya - hide button
                holder.btnKonfirmasi.setVisibility(View.GONE);
                Log.d(TAG, "Button hidden for konsumen, status: " + normalizedStatusOrder);
            }
        }
    }

    private String formatRupiah(String amount) {
        try {
            double value = Double.parseDouble(amount);
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            return formatter.format(value);
        } catch (NumberFormatException e) {
            return "Rp " + amount;
        }
    }

    private String getStatusText(String status) {
        if (status == null || status.equals("null") || status.isEmpty()) {
            return "Menunggu Konfirmasi";
        }

        switch (status) {
            case "dijemput":
                return "Sedang Dijemput";
            case "dikerjakan":
                return "Sedang Dikerjakan";
            case "diantar":
                return "Sedang Diantar";
            case "dikonfirmasi":
                return "Dikonfirmasi";
            case "selesai":
                return "Selesai";
            default:
                return "Menunggu Konfirmasi";
        }
    }

    public interface OnOrdersListChangedListener {
        void onOrdersListChangedListener();
    }

    private OnOrdersListChangedListener listener;

    public void setOnOrdersListChangedListener(OnOrdersListChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return combinedOrderModels.size();
    }

    public class OrdersModelViewHolder extends RecyclerView.ViewHolder {
        View layoutDelete, layoutUpdate;
        CardView card;
        ImageView imgFoto;
        TextView tvNamaPaket, tvKodePaket, tvHarga, tvTipe, tvDeskripsi, tvStatus;
        Button btnCetakInvoice, btnKonfirmasi;

        public OrdersModelViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            tvNamaPaket = itemView.findViewById(R.id.tvNamaPaket);
            tvKodePaket = itemView.findViewById(R.id.tvKodePaket);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvTipe = itemView.findViewById(R.id.tvTipe);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
            imgFoto = itemView.findViewById(R.id.imgFoto);
            layoutDelete = itemView.findViewById(R.id.layout_detail);
            layoutUpdate = itemView.findViewById(R.id.cardUpdate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCetakInvoice = itemView.findViewById(R.id.btnCetakInvoice);
            btnKonfirmasi = itemView.findViewById(R.id.btnKonfirmasi);
        }
    }
}