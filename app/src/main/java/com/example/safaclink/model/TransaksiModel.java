package com.example.safaclink.model;

public class TransaksiModel {
    public String id_transaksi;
    public String id_orders;
    public String volume;
    public String status_transaksi;

    public TransaksiModel(String id_transaksi, String id_orders, String volume, String status_transaksi) {
        this.id_transaksi = id_transaksi;
        this.id_orders = id_orders;
        this.volume = volume;
        this.status_transaksi = status_transaksi;
    }

    public String getId_transaksi() {
        return id_transaksi;
    }

    public void setId_transaksi(String id_transaksi) {
        this.id_transaksi = id_transaksi;
    }

    public String getId_orders() {
        return id_orders;
    }

    public void setId_orders(String id_orders) {
        this.id_orders = id_orders;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getStatus_transaksi() {
        return status_transaksi;
    }

    public void setStatus_transaksi(String status_transaksi) {
        this.status_transaksi = status_transaksi;
    }
}
