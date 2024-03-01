package com.juanjob.app.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import com.juanjob.app.R;
import com.juanjob.app.SelectModulePage;
import com.juanjob.app.client.orders.ClientOrdersPage;
import com.juanjob.app.client.profile.ClientProfilePage;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.LoadingPage;

public class ClientNavigationPage extends AppCompatActivity {
    private BottomNavigationView nav_bottom;
    private AlertDialog loading, notif_dialog;
    private View notif_view;

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
        setContentView(R.layout.client_navigation_page);
        loading = new LoadingPage().loadingGif(this);

        AlertDialog.Builder mbuilder = new AlertDialog.Builder(ClientNavigationPage.this);
        notif_view = getLayoutInflater().inflate(R.layout.order_notifcation, null);
        mbuilder.setView(notif_view);
        notif_dialog = mbuilder.create();

        SharedPreferences sp = getSharedPreferences("Module Selected", MODE_PRIVATE);
        String module_selected = sp.getString("module_selected", "");
        String set_module = module_selected.equals("worker") ? "" : "Find Workers";
        TextView as_what_tv = findViewById(R.id.as_what_tv);
        as_what_tv.setText(set_module);

        nav_bottom = findViewById(R.id.nav_bottom);
        nav_bottom.setOnItemSelectedListener(nav_listener);

        FragmentContainerView fr = findViewById(R.id.client_fragmentContainerView);
        fr.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
//                if (fr.getFragment().toString().contains("services_page") || fr.getFragment().toString().contains("NavHostFragment")) {
//                    nav_bottom.getMenu().getItem(0).setChecked(true);
//                }

                if (fr.getFragment().toString().contains("home_page") || fr.getFragment().toString().contains("NavHostFragment")) {
                    nav_bottom.getMenu().getItem(0).setChecked(true);
                    getOrders();
                }

                if (fr.getFragment().toString().contains("profile_page")) {
                    nav_bottom.getMenu().getItem(1).setChecked(true);
                    getOrders();
                }

                if (fr.getFragment().toString().contains("orders_page")) {
                    nav_bottom.getMenu().getItem(2).setChecked(true);
                    getOrders();
                }
            }
        });

        reloadHomePage();
    }

    private final NavigationBarView.OnItemSelectedListener nav_listener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.client_home_page:
                    setFragment(new ClientHomePage(), "home_page");
                    return false;
                case R.id.client_profile_page:
                    setFragment(new ClientProfilePage(), "profile_page");
                    return false;
                case R.id.client_orders_page:
                    setFragment(new ClientOrdersPage(), "orders_page");
                    return false;
                case R.id.client_logout_page:
                    logout();
                    return true;
                default:
                    setFragment(new ClientHomePage(), "home_page");
            }
            return false;
        }
    };

    private void setFragment(Fragment fragment, String name) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.client_fragmentContainerView, fragment, name).commit();
    }

    private void GotoLoginPage() {
        Intent i = new Intent(this, SelectModulePage.class);
        startActivity(i);
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

    private void reloadHomePage() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.client_fragmentContainerView, new ClientHomePage(), "home_page").commit();
    }

    private void getOrders() {
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        String client_id = sp.getString("login_id", "");
        new Repositories().getAllClientServices(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("services_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject service_id_obj = obj.getJSONObject("service_id");
                        JSONArray array = service_id_obj.toJSONArray(service_id_obj.names());
                        for (int i = 0; i < Objects.requireNonNull(array).length(); i++) {
                            String service_id = Objects.requireNonNull(service_id_obj.names()).get(i).toString();
                            JSONObject service_obj = new JSONObject(String.valueOf(array.get(i)));
                            String category = String.valueOf(service_obj.get("category"));
                            String location = String.valueOf(service_obj.get("location"));
                            new Repositories().getService(service_id, client_id, new Repositories.RepoCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    try {
                                        JSONObject sku_obj = new JSONObject(result);
                                        String customer_id_str = String.valueOf(sku_obj.get("customer_id"));
                                        if (!customer_id_str.trim().isEmpty()) {
                                            new Repositories().getCustomerOrders(customer_id_str, new Repositories.RepoCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    if (!result.equals("order_not_found")) {
                                                        try {
                                                            JSONObject obj= new JSONObject(result);
                                                            JSONObject order_id_obj = obj.getJSONObject("order_id");
                                                            JSONArray order_id_array = order_id_obj.toJSONArray(order_id_obj.names());
                                                            for (int a = 0; a < Objects.requireNonNull(order_id_array).length(); a++) {
                                                                String sku = String.valueOf(order_id_array.get(a));
                                                                JSONObject order_obj = new JSONObject(sku);
                                                                String service_id = String.valueOf(order_obj.get("service_id"));
                                                                String date_ordered = String.valueOf(order_obj.get("date_ordered"));
                                                                String cus_brgy = String.valueOf(order_obj.get("cus_brgy"));
                                                                String client_id_str = String.valueOf(order_obj.get("client_id"));
                                                                String order_status = String.valueOf(order_obj.get("order_status"));
                                                                String order_id = String.valueOf(order_id_obj.names().get(a));
                                                                if (client_id.equals(client_id_str) && order_status.toLowerCase().trim().equals("pending")) {
                                                                    new Repositories().getCustomer(customer_id_str, new Repositories.RepoCallback() {
                                                                        @Override
                                                                        public void onSuccess(String result) {
                                                                            if (!result.equals("user_not_found")) {
                                                                                try {
                                                                                    JSONObject obj = new JSONObject(result);
                                                                                    String account_status_str = String.valueOf(obj.get("account_status"));
                                                                                    String online_status_str = String.valueOf(obj.get("online_status"));
                                                                                    String address_str = String.valueOf(obj.get("address"));
                                                                                    String gender_str = String.valueOf(obj.get("gender"));
                                                                                    String email_str = String.valueOf(obj.get("email"));
                                                                                    String mobile_str = String.valueOf(obj.get("mobile"));
                                                                                    String profile_img_str = String.valueOf(obj.get("profile_img"));
                                                                                    String firstname = String.valueOf(obj.get("firstname"));
                                                                                    String lastname = String.valueOf(obj.get("lastname"));
                                                                                    String middle_name = String.valueOf(obj.get("middle_name"));
                                                                                    String full_name = firstname + " " + middle_name + " " + lastname;

                                                                                    if (!notif_dialog.isShowing()) {
                                                                                        notif_dialog.show();
                                                                                        notif_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                                                                        TextView cus_name = notif_view.findViewById(R.id.cus_name);
                                                                                        cus_name.setText(full_name);
                                                                                        TextView cus_mobile = notif_view.findViewById(R.id.cus_mobile);
                                                                                        cus_mobile.setText(mobile_str);
                                                                                        TextView cus_location = notif_view.findViewById(R.id.cus_location);
                                                                                        cus_location.setText(cus_brgy);
                                                                                        TextView confirm_btn = notif_view.findViewById(R.id.confirm_btn);
                                                                                        confirm_btn.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View v) {
                                                                                                updateOrder(customer_id_str, order_id);
                                                                                            }
                                                                                        });
                                                                                    }

                                                                                } catch (JSONException e) {
                                                                                    throw new RuntimeException(e);
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        } catch (JSONException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void updateOrder(String customer_id, String order_id) {
        loading.show();
        new Repositories().updateOrderStatus(customer_id, order_id, "Accepted", new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                loading.dismiss();
                if (result.equals("success")) {
                    String msg = "Successfully updated order status";
                    Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
                    notif_dialog.dismiss();
                } else {
                    String msg = "Something went wrong, please try again.";
                    Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
