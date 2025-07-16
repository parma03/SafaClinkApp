package com.example.safaclink.model;

public class OrderModel {
    public String id_orders;
    public String order_id;
    public String status_order;
    public String created_at;
    public String updated_at;

    public OrderModel(String id_orders, String order_id, String status_order, String created_at, String updated_at) {
        this.id_orders = id_orders;
        this.order_id = order_id;
        this.status_order = status_order;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public String getId_orders() {
        return id_orders;
    }

    public void setId_orders(String id_orders) {
        this.id_orders = id_orders;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getStatus_order() {
        return status_order;
    }

    public void setStatus_order(String status_order) {
        this.status_order = status_order;
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
