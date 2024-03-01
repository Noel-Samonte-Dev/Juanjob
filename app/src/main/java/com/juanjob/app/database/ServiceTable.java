package com.juanjob.app.database;

public class ServiceTable {
    String name, description, date_created, category, location, status, client_id, customer_id, img, price_range;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public String getDate_created() {
        return date_created;
    }

    public String getImg() {
        return img;
    }

    public String getCategory() {
        return category;
    }

    public String getClient_id() {
        return client_id;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public String getPrice_range() {
        return price_range;
    }

    public ServiceTable() {

    }

    public ServiceTable(String name, String description, String date_created, String category, String location,
                        String status, String client_id, String customer_id, String img, String price_range) {
        this.name = name;
        this.description = description;
        this.date_created = date_created;
        this.category = category;
        this.location = location;
        this.status = status;
        this.client_id = client_id;
        this.customer_id = customer_id;
        this.img = img;
        this.price_range = price_range;
    }
}
