package com.juanjob.app.client.orders;

public class ClientOrderItem {
    String name, location, order_id, customer_id, price, order_image, order_status;
    int date_ordered_int;
    long date_ordered_long;

    public String getCustomer_id() {
        return customer_id;
    }

    public String getOrder_image() {
        return order_image;
    }

    public long getDate_ordered_long() {
        return date_ordered_long;
    }

    public String getOrder_status() {
        return order_status;
    }

    public String getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public int getDate_ordered_int() {
        return date_ordered_int;
    }

    public String getLocation() {
        return location;
    }

    public String getOrder_id() {
        return order_id;
    }

    public ClientOrderItem(String name, String location, String order_id, String customer_id, String price, String order_image, String order_status, int date_ordered_int, long date_ordered_long) {
        this.name = name;
        this.location = location;
        this.order_id = order_id;
        this.customer_id = customer_id;
        this.price = price;
        this.order_image = order_image;
        this.order_status = order_status;
        this.date_ordered_int = date_ordered_int;
        this.date_ordered_long = date_ordered_long;
    }
}
