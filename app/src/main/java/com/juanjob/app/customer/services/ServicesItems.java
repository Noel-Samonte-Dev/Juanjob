package com.juanjob.app.customer.services;

import android.graphics.Bitmap;

public class ServicesItems {
    private String service_id, service_name, service_desc, service_location, service_status, service_customer, client_id, client_name, price_range;
    private Bitmap url, profile_img;

    public String getService_id() {
        return service_id;
    }

    public String getService_name() {
        return service_name;
    }

    public String getService_desc() {
        return service_desc;
    }

    public String getService_location() {
        return service_location;
    }

    public String getService_status() {
        return service_status;
    }

    public String getService_customer() {
        return service_customer;
    }

    public String getClient_id() {
        return client_id;
    }

    public Bitmap getUrl() {
        return url;
    }

    public String getClient_name() {
        return client_name;
    }

    public Bitmap getProfile_img() {
        return profile_img;
    }

    public String getPrice_range() {
        return price_range;
    }

    public ServicesItems() {

    }

    public ServicesItems(Bitmap url, String service_id, String service_name, String service_desc, String service_location,
                         String service_status, String service_customer, String client_id, Bitmap profile_img, String client_name, String price_range) {
        this.url = url;
        this.service_id = service_id;
        this.service_name = service_name;
        this.service_desc = service_desc;
        this.service_location = service_location;
        this.service_status = service_status;
        this.service_customer = service_customer;
        this.client_id = client_id;
        this.profile_img = profile_img;
        this.client_name = client_name;
        this.price_range = price_range;
    }
}
