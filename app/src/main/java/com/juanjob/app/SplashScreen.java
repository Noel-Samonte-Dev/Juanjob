package com.juanjob.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.juanjob.app.ads_page.AdsPage;
import com.juanjob.app.database.Repositories;

public class SplashScreen extends AppCompatActivity {
    private String login_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);

//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setApplicationId("1:878776686292:android:7b0307596d3c072e5e0d41") // Required for Analytics.
//                .setApiKey("AIzaSyCqgY65SPiMuR60st_yq3BWnDogl7sjTJ0") // Required for Auth.
//                .build();
//        FirebaseApp.initializeApp(this, options, "primary");

        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        login_id = sp.getString("login_id", "");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSecure();
            }
        }, 3000);
    }

    private void goToAdsPage() {
        Intent i = new Intent(this, AdsPage.class);
        startActivity(i);
        finish();
    }

    private void getSecure() {
        new Repositories().getSecure(new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (result.equals("true")) {
                    goToAdsPage();
                } else {
                    Intent i = new Intent(SplashScreen.this, SecurePage.class);
                    startActivity(i);
                    finish();
                }
            }
        });
    }
}
