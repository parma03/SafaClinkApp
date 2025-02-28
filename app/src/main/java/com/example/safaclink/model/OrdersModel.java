package com.example.safaclink.model;

public class OrdersModel {
    public String id_order;
    public String id_konsumen;
    public String id_paket;
    public String tanggal_orders;
    public String jam_booking;
    public String alamat;
    public String catatan;
    public String foto_barang;
    public String status_orders;

    public OrdersModel(String id_order, String id_konsumen, String id_paket, String tanggal_orders, String jam_booking, String alamat, String catatan, String foto_barang, String status_orders) {
        this.id_order = id_order;
        this.id_konsumen = id_konsumen;
        this.id_paket = id_paket;
        this.tanggal_orders = tanggal_orders;
        this.jam_booking = jam_booking;
        this.alamat = alamat;
        this.catatan = catatan;
        this.foto_barang = foto_barang;
        this.status_orders = status_orders;
    }

    public String getId_order() {
        return id_order;
    }

    public void setId_order(String id_order) {
        this.id_order = id_order;
    }

    public String getId_konsumen() {
        return id_konsumen;
    }

    public void setId_konsumen(String id_konsumen) {
        this.id_konsumen = id_konsumen;
    }

    public String getId_paket() {
        return id_paket;
    }

    public void setId_paket(String id_paket) {
        this.id_paket = id_paket;
    }

    public String getTanggal_orders() {
        return tanggal_orders;
    }

    public void setTanggal_orders(String tanggal_orders) {
        this.tanggal_orders = tanggal_orders;
    }

    public String getJam_booking() {
        return jam_booking;
    }

    public void setJam_booking(String jam_booking) {
        this.jam_booking = jam_booking;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public String getFoto_barang() {
        return foto_barang;
    }

    public void setFoto_barang(String foto_barang) {
        this.foto_barang = foto_barang;
    }

    public String getStatus_orders() {
        return status_orders;
    }

    public void setStatus_orders(String status_orders) {
        this.status_orders = status_orders;
    }
}
