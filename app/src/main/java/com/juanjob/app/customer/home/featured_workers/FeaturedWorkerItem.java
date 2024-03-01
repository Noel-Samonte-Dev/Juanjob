package com.juanjob.app.customer.home.featured_workers;

import android.graphics.Bitmap;

public class FeaturedWorkerItem {
    private String name, client_id, category;

    public String getCategory() {
        return category;
    }

    private Bitmap profile_img;

    private double rating;

    public double getRating() {
        return rating;
    }

    public String getName() {
        return name;
    }

    public String getClient_id() {
        return client_id;
    }

    public Bitmap getProfile_img() {
        return profile_img;
    }

    public FeaturedWorkerItem() {

    }

    public FeaturedWorkerItem(String name, String client_id, Bitmap profile_img, double rating, String category) {
        this.name = name;
        this.client_id = client_id;
        this.profile_img = profile_img;
        this.rating = rating;
        this.category = category;
    }
}
