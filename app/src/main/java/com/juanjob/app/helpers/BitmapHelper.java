package com.juanjob.app.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class BitmapHelper {
    public static String bitmap_str(Context context, String img_filePath) {
        Uri filePath = Uri.fromFile(new File(img_filePath));
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        int width = 200;
//        int height = 200;
//
//        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.WEBP, 60, baos);
        }

        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static Bitmap urlStrToBitmap(String url) {
        byte[] decoded_str = Base64.decode(url, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded_str, 0, decoded_str.length);
        return bitmap;
    }
}
