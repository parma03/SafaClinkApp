package com.example.safaclink.model;

// Model gabungan untuk menampung data dari transaksi dan order
public class CombinedOrderModel {
    // From transaction
    private String id_transaksi;
    private String id_pelanggan;
    private String id_paket;
    private String alamat;
    private String lokasi;
    private String item;
    private String foto_barang;
    private String status_transaksi;
    private String total_harga;
    private String snap_token;
    private String order_id;
    private String created_at;
    private String updated_at;

    // From order
    private String id_orders;
    private String status_order;
    private String order_created_at;
    private String order_updated_at;

    // From paket
    private String nama_paket;
    private String tipe_paket;
    private String paket_deskripsi;
    private String harga;

    // From user
    private String nama_pelanggan;
    private String nohp_pelanggan;
    private String email_pelanggan;

    // For determining data source
    private String dataSource; // "transaction" or "order"

    public CombinedOrderModel() {}

    // Static method to create from TransaksiModel
    public static CombinedOrderModel fromTransaksi(TransaksiModel transaksi) {
        CombinedOrderModel model = new CombinedOrderModel();
        model.id_transaksi = transaksi.getId_transaksi();
        model.id_pelanggan = transaksi.getId_pelanggan();
        model.id_paket = transaksi.getId_paket();
        model.alamat = transaksi.getAlamat();
        model.lokasi = transaksi.getLokasi();
        model.item = transaksi.getItem();
        model.foto_barang = transaksi.getFoto_barang();
        model.status_transaksi = transaksi.getStatus_transaksi();
        model.total_harga = transaksi.getTotal_harga();
        model.snap_token = transaksi.getSnap_token();
        model.order_id = transaksi.getOrder_id();
        model.created_at = transaksi.getCreated_at();
        model.updated_at = transaksi.getUpdated_at();
        model.dataSource = "transaction";
        return model;
    }

    // Static method to create from OrderModel
    public static CombinedOrderModel fromOrder(OrderModel order) {
        CombinedOrderModel model = new CombinedOrderModel();
        model.id_orders = order.getId_orders();
        model.order_id = order.getOrder_id();
        model.status_order = order.getStatus_order();
        model.order_created_at = order.getCreated_at();
        model.order_updated_at = order.getUpdated_at();
        model.dataSource = "order";
        return model;
    }

    // Getters and setters
    public String getId_transaksi() { return id_transaksi; }
    public void setId_transaksi(String id_transaksi) { this.id_transaksi = id_transaksi; }

    public String getId_pelanggan() { return id_pelanggan; }
    public void setId_pelanggan(String id_pelanggan) { this.id_pelanggan = id_pelanggan; }

    public String getId_paket() { return id_paket; }
    public void setId_paket(String id_paket) { this.id_paket = id_paket; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public String getFoto_barang() { return foto_barang; }
    public void setFoto_barang(String foto_barang) { this.foto_barang = foto_barang; }

    public String getStatus_transaksi() { return status_transaksi; }
    public void setStatus_transaksi(String status_transaksi) { this.status_transaksi = status_transaksi; }

    public String getTotal_harga() { return total_harga; }
    public void setTotal_harga(String total_harga) { this.total_harga = total_harga; }

    public String getSnap_token() { return snap_token; }
    public void setSnap_token(String snap_token) { this.snap_token = snap_token; }

    public String getOrder_id() { return order_id; }
    public void setOrder_id(String order_id) { this.order_id = order_id; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }

    public String getId_orders() { return id_orders; }
    public void setId_orders(String id_orders) { this.id_orders = id_orders; }

    public String getStatus_order() { return status_order; }
    public void setStatus_order(String status_order) { this.status_order = status_order; }

    public String getOrder_created_at() { return order_created_at; }
    public void setOrder_created_at(String order_created_at) { this.order_created_at = order_created_at; }

    public String getOrder_updated_at() { return order_updated_at; }
    public void setOrder_updated_at(String order_updated_at) { this.order_updated_at = order_updated_at; }

    public String getNama_paket() { return nama_paket; }
    public void setNama_paket(String nama_paket) { this.nama_paket = nama_paket; }

    public String getTipe_paket() { return tipe_paket; }
    public void setTipe_paket(String tipe_paket) { this.tipe_paket = tipe_paket; }

    public String getPaket_deskripsi() { return paket_deskripsi; }
    public void setPaket_deskripsi(String paket_deskripsi) { this.paket_deskripsi = paket_deskripsi; }

    public String getHarga() { return harga; }
    public void setHarga(String harga) { this.harga = harga; }

    public String getNama_pelanggan() { return nama_pelanggan; }
    public void setNama_pelanggan(String nama_pelanggan) { this.nama_pelanggan = nama_pelanggan; }

    public String getNohp_pelanggan() { return nohp_pelanggan; }
    public void setNohp_pelanggan(String nohp_pelanggan) { this.nohp_pelanggan = nohp_pelanggan; }

    public String getEmail_pelanggan() { return email_pelanggan; }
    public void setEmail_pelanggan(String email_pelanggan) { this.email_pelanggan = email_pelanggan; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    // Helper method to get display status
    public String getDisplayStatus() {
        if ("order".equals(dataSource)) {
            return status_order != null ? status_order : "Unknown";
        } else {
            return status_transaksi != null ? status_transaksi : "Unknown";
        }
    }

    // Helper method to get primary created date
    public String getPrimaryCreatedAt() {
        if ("order".equals(dataSource)) {
            return order_created_at != null ? order_created_at : created_at;
        } else {
            return created_at != null ? created_at : order_created_at;
        }
    }
}