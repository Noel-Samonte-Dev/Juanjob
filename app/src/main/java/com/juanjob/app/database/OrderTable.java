package com.juanjob.app.database;

public class OrderTable {

    String date_ordered, order_status, service_id, client_id, rating, cus_brgy, cus_landmark, cus_desc, price, order_image;

    public String getDate_ordered() {
        return date_ordered;
    }

    public String getOrder_status() {
        return order_status;
    }

    public String getRating() {
        return rating;
    }

    public String getOrder_image() {
        return order_image;
    }

    public String getCus_brgy() {
        return cus_brgy;
    }

    public String getCus_landmark() {
        return cus_landmark;
    }

    public String getPrice() {
        return price;
    }

    public String getCus_desc() {
        return cus_desc;
    }

    public String getService_id() {
        return service_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public OrderTable() {

    }

    public OrderTable(String date_ordered, String order_status, String service_id, String client_id,
                      String rating, String cus_brgy, String cus_landmark, String cus_desc, String price, String order_image) {
        this.date_ordered = date_ordered;
        this.order_status = order_status;
        this.service_id = service_id;
        this.client_id = client_id;
        this.rating = rating;
        this.cus_brgy = cus_brgy;
        this.cus_landmark = cus_landmark;
        this.cus_desc = cus_desc;
        this.price = price;
        this.order_image = order_image;
    }
}
