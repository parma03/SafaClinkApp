package com.example.safaclink.model;

public class UserModel {
    public String id_user;
    public String nama;
    public String nohp;
    public String email;
    public String username;
    public String password;
    public String profile;

    public UserModel(String id_user, String nama, String nohp, String email, String username, String password, String profile) {
        this.id_user = id_user;
        this.nama = nama;
        this.nohp = nohp;
        this.email = email;
        this.username = username;
        this.password = password;
        this.profile = profile;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNohp() {
        return nohp;
    }

    public void setNohp(String nohp) {
        this.nohp = nohp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
