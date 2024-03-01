package com.juanjob.app.client.orders;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.juanjob.app.R;
import com.juanjob.app.customer.orders.CustomerOrderItem;
import com.juanjob.app.customer.orders.CustomerOrdersPage;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.LoadingPage;

public class ClientOrdersPage extends Fragment {
    private String client_id;
    private AlertDialog loading;
    private TextView nothing_found_tv;
    private boolean is_pending = true;
    private boolean is_cancelled = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_orders_page, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading = new LoadingPage().loadingGif(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        client_id = sp.getString("login_id", "");

        nothing_found_tv = v.findViewById(R.id.nothing_found_tv);
        rec_view = v.findViewById(R.id.orders_rv);
        getOrders();

        SwipeRefreshLayout swipe_refresh = v.findViewById(R.id.swipe_refresh);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setRefreshing(false);
                getOrders();
            }
        });

        Button pending_btn = v.findViewById(R.id.pending_btn1);
        Button completed_btn = v.findViewById(R.id.completed_btn1);
        Button cancelled_btn = v.findViewById(R.id.cancelled_btn1);

        pending_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_pending = true;
                is_cancelled = false;
                getOrders();
                completed_btn.setBackgroundResource(R.drawable.black_border);
                cancelled_btn.setBackgroundResource(R.drawable.black_border);
                pending_btn.setBackgroundResource(R.drawable.blue_border);
            }
        });

        completed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_pending = false;
                is_cancelled = false;
                getOrders();
                completed_btn.setBackgroundResource(R.drawable.blue_border);
                pending_btn.setBackgroundResource(R.drawable.black_border);
                cancelled_btn.setBackgroundResource(R.drawable.black_border);
            }
        });

        cancelled_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_pending = false;
                is_cancelled = true;
                getOrders();
                completed_btn.setBackgroundResource(R.drawable.black_border);
                pending_btn.setBackgroundResource(R.drawable.black_border);
                cancelled_btn.setBackgroundResource(R.drawable.blue_border);
            }
        });
    }

    private void getOrders() {
        order_item = new ArrayList<>();
        buildOrders();
        nothing_found_tv.setVisibility(View.GONE);
        nothing_found_tv.setText("");
        loading.show();
        refreshOrders();
    }

    private RecyclerView rec_view;
    private LinearLayoutManager rec_manager;
    private List<ClientOrderItem> order_item = new ArrayList<>();
    private ClientOrdersAdapter order_adapter;
    private void buildOrders() {
        rec_view.setHasFixedSize(false);
        rec_manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        order_adapter = new ClientOrdersAdapter(getActivity(), order_item, rec_view);
        rec_view.setLayoutManager(rec_manager);
        rec_view.setAdapter(order_adapter);
    }
    private void refreshOrders() {
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                order_item = new ArrayList<>();
                buildOrders();
                if (snapshot.getValue() != null) {
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

                                        new Repositories().getAllOrders(new Repositories.RepoCallback() {
                                            @Override
                                            public void onSuccess(String result_orders) {
                                                if (!result_orders.equals("order_not_found")) {
                                                    try {
                                                        JSONObject order_obj = new JSONObject(result_orders);
                                                        JSONArray order_array = order_obj.names();
                                                        for (int n = 0; n < order_array.length(); n++) {
                                                            String customer_id_str = String.valueOf(order_array.get(n));
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
                                                                                    String price_str = String.valueOf(order_obj.get("price"));
                                                                                    String service_id = String.valueOf(order_obj.get("service_id"));
                                                                                    String date_ordered = String.valueOf(order_obj.get("date_ordered"));
                                                                                    String order_image = String.valueOf(order_obj.get("order_image"));
                                                                                    String client_id_str = String.valueOf(order_obj.get("client_id"));
                                                                                    String order_status = String.valueOf(order_obj.get("order_status"));
                                                                                    String order_id = String.valueOf(order_id_obj.names().get(a));
                                                                                    Date date = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss-a").parse(date_ordered);
                                                                                    int date_int = (int)date.getTime();

                                                                                    if (client_id.equals(client_id_str) && !is_pending && !is_cancelled && order_status.equals("Completed")) {
                                                                                        order_item.add(new ClientOrderItem(category, location, order_id, customer_id_str, price_str, order_image, "Completed", date_int, date.getTime()));
                                                                                    }

                                                                                    if (client_id.equals(client_id_str) && is_cancelled && order_status.equals("Cancelled")) {
                                                                                        order_item.add(new ClientOrderItem(category, location, order_id, customer_id_str, price_str, order_image, "Cancelled", date_int, date.getTime()));
                                                                                    }

                                                                                    if (client_id.equals(client_id_str) && is_pending && !is_cancelled && !order_status.equals("Completed") && !order_status.equals("Cancelled")) {
                                                                                        order_item.add(new ClientOrderItem(category, location, order_id, customer_id_str, price_str, order_image, "Pending", date_int, date.getTime()));
                                                                                    }

                                                                                    if (a + 1 == order_id_array.length()) {
                                                                                        if (!order_item.isEmpty()) {
                                                                                            order_item.sort(new DateComparator());
                                                                                            buildOrders();
                                                                                        }

                                                                                        if (order_item.isEmpty()) {
                                                                                            nothing_found_tv.setVisibility(View.VISIBLE);
                                                                                            nothing_found_tv.setText(is_pending ? "No pending order." : "No completed order.");
                                                                                        }

                                                                                        loading.dismiss();
                                                                                    }
                                                                                }
                                                                            } catch (JSONException | ParseException e) {
                                                                                throw new RuntimeException(e);
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                Toast.makeText(getContext(), "Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
                                                                loading.dismiss();
                                                            }
                                                        }
                                                    } catch (JSONException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                } else {
                                                    nothing_found_tv.setVisibility(View.VISIBLE);
                                                    nothing_found_tv.setText(is_pending ? "No pending order." : "No completed order.");
                                                    loading.dismiss();
                                                }
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                Toast.makeText(getContext(), "Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        DatabaseReference database = FirebaseDatabase.getInstance(Repositories.firebase_db_url).getReference();
        database.child("order_table")
                .addValueEventListener(vel);
    }

    class DateComparator implements Comparator<ClientOrderItem> {

        @Override
        public int compare(ClientOrderItem a, ClientOrderItem b) {
            return (int) (b.getDate_ordered_int() - a.getDate_ordered_int());
        }
    }
}

