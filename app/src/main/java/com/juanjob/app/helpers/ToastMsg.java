package com.juanjob.app.helpers;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.juanjob.app.R;

public class ToastMsg {
    public static void error_toast(Context context, String message) {
        Toast toast = new Toast(context);
        View toast_view = LayoutInflater.from(context).inflate(R.layout.custom_toast_msg, null);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 200);
        toast.show();

        TextView toast_msg = toast_view.findViewById(R.id.toast_msg);
        toast_msg.setText(message);

    }
}
