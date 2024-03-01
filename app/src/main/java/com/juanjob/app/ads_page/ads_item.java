package com.juanjob.app.ads_page;

public class ads_item {
    String msg;
    int image;

    public int getImage() {
        return image;
    }

    public String getMsg() {
        return msg;
    }

    public ads_item(int image, String msg) {
        this.image = image;
        this.msg = msg;
    }
}
