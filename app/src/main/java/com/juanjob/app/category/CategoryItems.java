package com.juanjob.app.category;

public class CategoryItems {
    private String url, label;

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public CategoryItems(String url, String label) {
        this.url = url;
        this.label = label;
    }
}
