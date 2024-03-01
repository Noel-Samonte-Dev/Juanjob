package com.juanjob.app.helpers;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.juanjob.app.R;

public class LoadingPage {
    public AlertDialog loadingGif(Activity activity) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        View mv = activity.getLayoutInflater().inflate(R.layout.loading_page, null);
        dialog.setView(mv);
        dialog.setCancelable(false);
        dialog.setTitle(null);
        AlertDialog alert = dialog.create();
        int size = loadingGifSize(activity);
        alert.getWindow().setLayout(size, size);
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return alert;
    }

    private static int loadingGifSize(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float width = displayMetrics.widthPixels;
        float widthcanvas = 1500;
        float w = width * (550 / widthcanvas);
        return Math.round(w);
    }
}
