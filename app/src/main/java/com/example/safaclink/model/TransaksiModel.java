package com.example.safaclink.model;

public class TransaksiModel {
    public String id_transaksi;
    public String id_pelanggan;
    public String id_paket;
    public String alamat;
    public String lokasi;
    public String item;
    public String foto_barang;
    public String status_transaksi;
    public String total_harga;
    public String snap_token;
    public String order_id;
    public String created_at;
    public String updated_at;

    public TransaksiModel(String id_transaksi, String id_pelanggan, String id_paket, String alamat, String lokasi, String item, String foto_barang, String status_transaksi, String total_harga, String snap_token, String order_id, String created_at, String updated_at) {
        this.id_transaksi = id_transaksi;
        this.id_pelanggan = id_pelanggan;
        this.id_paket = id_paket;
        this.alamat = alamat;
        this.lokasi = lokasi;
        this.item = item;
        this.foto_barang = foto_barang;
        this.status_transaksi = status_transaksi;
        this.total_harga = total_harga;
        this.snap_token = snap_token;
        this.order_id = order_id;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public String getId_transaksi() {
        return id_transaksi;
    }

    public void setId_transaksi(String id_transaksi) {
        this.id_transaksi = id_transaksi;
    }

    public String getId_pelanggan() {
        return id_pelanggan;
    }

    public void setId_pelanggan(String id_pelanggan) {
        this.id_pelanggan = id_pelanggan;
    }

    public String getId_paket() {
        return id_paket;
    }

    public void setId_paket(String id_paket) {
        this.id_paket = id_paket;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getFoto_barang() {
        return foto_barang;
    }

    public void setFoto_barang(String foto_barang) {
        this.foto_barang = foto_barang;
    }

    public String getStatus_transaksi() {
        return status_transaksi;
    }

    public void setStatus_transaksi(String status_transaksi) {
        this.status_transaksi = status_transaksi;
    }

    public String getTotal_harga() {
        return total_harga;
    }

    public void setTotal_harga(String total_harga) {
        this.total_harga = total_harga;
    }

    public String getSnap_token() {
        return snap_token;
    }

    public void setSnap_token(String snap_token) {
        this.snap_token = snap_token;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
