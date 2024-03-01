package com.juanjob.app.customer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import com.juanjob.app.R;
import com.juanjob.app.SelectModulePage;
import com.juanjob.app.client.ClientHomePage;
import com.juanjob.app.customer.home.CustomerHomePage;
import com.juanjob.app.customer.home.CustomerHomePage_v2;
import com.juanjob.app.customer.orders.CustomerOrdersPage;
import com.juanjob.app.customer.profile.CustomerProfilePage;

public class CustomerNavigationPage extends AppCompatActivity {
    private BottomNavigationView nav_bottom;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() < 2) {
            exitApp();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_navigation_page);

        SharedPreferences sp = getSharedPreferences("Module Selected", MODE_PRIVATE);
        String module_selected = sp.getString("module_selected", "");
        String set_module = module_selected.equals("worker") ? "" : "Find Workers";
        TextView as_what_tv = findViewById(R.id.as_what_tv);
        as_what_tv.setText(set_module);

        nav_bottom = findViewById(R.id.customer_nav_bottom);
        nav_bottom.setOnItemSelectedListener(nav_listener);

        FragmentContainerView fr = findViewById(R.id.customer_fragmentContainerView);
        fr.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (fr.getFragment().toString().contains("home_page") || fr.getFragment().toString().contains("NavHostFragment")) {
                    nav_bottom.getMenu().getItem(0).setChecked(true);
                }

                if (fr.getFragment().toString().contains("orders_page")) {
                    nav_bottom.getMenu().getItem(1).setChecked(true);
                }

                if (fr.getFragment().toString().contains("profile_page")) {
                    nav_bottom.getMenu().getItem(2).setChecked(true);
                }

                if (fr.getFragment().toString().contains("settings_page")) {
                    nav_bottom.getMenu().getItem(3).setChecked(true);
                }
            }
        });

        reloadHomePage();
    }

    private final NavigationBarView.OnItemSelectedListener nav_listener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.home_page:
                    setFragment(new CustomerHomePage_v2(), "customer_home_page");
                    return true;
                case R.id.profile_page:
                    setFragment(new CustomerProfilePage(), "customer_profile_page");
                    return false;
                case R.id.settings_page:
                    logout();
                    return false;
                case R.id.orders_page:
                    setFragment(new CustomerOrdersPage(), "customer_orders_page");
                    return false;
                default:
                    setFragment(new CustomerHomePage_v2(), "customer_home_page");
            }
            return false;
        }
    };

    private void setFragment(Fragment fragment, String name) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.customer_fragmentContainerView, fragment, name).commit();
    }

    private void GotoLoginPage() {
        Intent i = new Intent(this, SelectModulePage.class);
        startActivity(i);
        finish();
    }

    private void logout() {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(this);
        View mview = getLayoutInflater().inflate(R.layout.prompt_dialog, null);
        mbuilder.setView(mview);
        AlertDialog dialog = mbuilder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button close = mview.findViewById(R.id.btn_cancel_logout);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button proceed = mview.findViewById(R.id.btn_proceed_logout);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("login_id", "");
                editor.commit();

                GotoLoginPage();
            }
        });
    }

    private void exitApp() {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(this);
        View mview = getLayoutInflater().inflate(R.layout.prompt_dialog, null);
        mbuilder.setView(mview);
        AlertDialog dialog = mbuilder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button close = mview.findViewById(R.id.btn_cancel_logout);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button proceed = mview.findViewById(R.id.btn_proceed_logout);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
            }
        });

        TextView logout_text = mview.findViewById(R.id.prompt_text);
        logout_text.setText("Are you sure you want to exit Juanjob?");
    }

    private void reloadHomePage() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.customer_fragmentContainerView, new CustomerHomePage_v2(), "customer_home_page").commit();
    }
}
