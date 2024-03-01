package com.juanjob.app;

import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.juanjob.app.account.LoginPage;
import com.juanjob.app.customer.CustomerNavigationPage;
import com.juanjob.app.database.Repositories;

public class SelectModulePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.select_module_page);

        Button find_job_btn = findViewById(R.id.find_job_btn);
        find_job_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleSelected(true);
                GotoLoginPage();
            }
        });

        Button find_worker_btn = findViewById(R.id.find_worker_btn);
        find_worker_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleSelected(false);
                GotoLoginPage();
            }
        });
    }

    private void navigationPage() {
        Intent i = new Intent(SelectModulePage.this, CustomerNavigationPage.class);
        startActivity(i);
        finish();
    }

    private void moduleSelected(boolean is_worker) {
        SharedPreferences sp = getSharedPreferences("Module Selected", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("module_selected", is_worker ? "worker" : "employer");
        editor.commit();
    }

    private void GotoLoginPage() {
        Intent i = new Intent(this, LoginPage.class);
        startActivity(i);
    }
}
