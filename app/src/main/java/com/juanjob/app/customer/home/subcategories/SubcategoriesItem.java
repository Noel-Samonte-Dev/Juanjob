package com.juanjob.app.customer.home.subcategories;

public class SubcategoriesItem {
    String category;

    public String getCategory() {
        return category;
    }

    public int getImage() {
        return image;
    }

    int image;

    public SubcategoriesItem(String category, int image) {
        this.category = category;
        this.image = image;
    }
}
