package com.juanjob.app.customer.home.categories;

public class CategoriesItem {
    String category;
    int image;

    public String getCategory() {
        return category;
    }

    public int getImage() {
        return image;
    }

    public CategoriesItem(String category, int image) {
        this.category = category;
        this.image = image;
    }
}
