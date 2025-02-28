package com.example.safaclink.model;

public class PaketModel {
    public String id_paket;
    public String nama_paket;
    public String tipe_paket;
    public String deskripsi;
    public String harga;

    public PaketModel(String id_paket, String nama_paket, String tipe_paket, String deskripsi, String harga) {
        this.id_paket = id_paket;
        this.nama_paket = nama_paket;
        this.tipe_paket = tipe_paket;
        this.deskripsi = deskripsi;
        this.harga = harga;
    }

    public String getId_paket() {
        return id_paket;
    }

    public void setId_paket(String id_paket) {
        this.id_paket = id_paket;
    }

    public String getNama_paket() {
        return nama_paket;
    }

    public void setNama_paket(String nama_paket) {
        this.nama_paket = nama_paket;
    }

    public String getTipe_paket() {
        return tipe_paket;
    }

    public void setTipe_paket(String tipe_paket) {
        this.tipe_paket = tipe_paket;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }
}
