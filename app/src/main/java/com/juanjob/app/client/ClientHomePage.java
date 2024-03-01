package com.juanjob.app.client;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.juanjob.app.R;
import com.juanjob.app.client.orders.ClientOrderItem;
import com.juanjob.app.client.orders.ClientOrdersAdapter;
import com.juanjob.app.client.orders.ClientOrdersPage;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class ClientHomePage extends Fragment {
    private TextView text_1, text_2, nothing_found_tv;
    private RatingBar worker_rating;
    private AlertDialog loading;
    private String client_id;
    private CircleImageView profile_img;
    private Button toggle;
    private Context page_context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        page_context = container.getContext();
        return inflater.inflate(R.layout.client_home_page, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading = new LoadingPage().loadingGif(getActivity());

        toggle = v.findViewById(R.id.toggle);
        profile_img = v.findViewById(R.id.profile_img);
        rec_view = v.findViewById(R.id.orders_rv);

        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        client_id = sp.getString("login_id", "");

        nothing_found_tv = v.findViewById(R.id.nothing_found_tv);
        text_1 = v.findViewById(R.id.text_1);
        text_2 = v.findViewById(R.id.text_2);
        worker_rating = v.findViewById(R.id.worker_rating);

        getOrders();

        SwipeRefreshLayout swipe_refresh = v.findViewById(R.id.swipe_refresh);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setRefreshing(false);
                getOrders();
            }
        });

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_active = !is_active;
                changeOnlineStatus();
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

    private void getProfileInfo() {
        new Repositories().getClient(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String category = String.valueOf(obj.get("category"));
                        String subcategory = String.valueOf(obj.get("subcategory"));
                        String other_category = String.valueOf(obj.get("other_category"));
                        String category_str = category.equals("Others") ? "Others: " + other_category : category;
                        String service_location_str = String.valueOf(obj.get("service_location"));
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
                        String full_name = firstname + " " + lastname;
                        String rating = obj.getString("rating");
                        int completed_orders = obj.getInt("completed_orders");

                        text_1.setText("Welcome back, " + full_name + "!");
                        text_2.setText("Total Orders Completed: " + String.valueOf(completed_orders));

                        worker_rating.setRating(Float.parseFloat(rating));
                        setImageView(profile_img_str);

                        if (online_status_str.equals("Active")) {
                            profile_img.setBorderColor(Color.parseColor("#008000"));
                            toggle.setText("Online");
                            toggle.setBackgroundResource(R.drawable.online_btn);
                            is_active = true;
                        } else {
                            profile_img.setBorderColor(Color.parseColor("#ADABAB"));
                            toggle.setText("Offline");
                            toggle.setBackgroundResource(R.drawable.offline_btn);
                            is_active = false;
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                loading.dismiss();
            }
        });
    }

    private void setImageView(String img_str) {
        Bitmap bitmap = BitmapHelper.urlStrToBitmap(img_str);
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .priority(Priority.HIGH)
                .format(DecodeFormat.PREFER_RGB_565);

        Glide.with(page_context)
                .applyDefaultRequestOptions(options)
                .asBitmap()
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.default_img)
                .into(profile_img);
    }

    private boolean is_active = false;
    private void changeOnlineStatus() {
        loading.show();
        String online_status = is_active ? "Active" : "Inactive";
        new Repositories().updateClientOnlineStatus(client_id, online_status, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                updateServiceStatus(online_status);
            }
        });
    }

    private void updateServiceStatus(String status) {
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
                            int finalI = i;
                            new Repositories().updateServiceActiveStatus(service_id, client_id, status, new Repositories.RepoCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    if (finalI + 1 == array.length()) {
                                        if (status.equals("Active")) {
                                            profile_img.setBorderColor(Color.parseColor("#008000"));
                                            toggle.setText("Online");
                                            toggle.setBackgroundResource(R.drawable.online_btn);
                                            is_active = true;
                                        } else {
                                            profile_img.setBorderColor(Color.parseColor("#ADABAB"));
                                            toggle.setText("Offline");
                                            toggle.setBackgroundResource(R.drawable.offline_btn);
                                            is_active = false;
                                        }
                                        loading.dismiss();
                                    }
                                }
                            });
                        }

                        if (array.length() == 0) {
                            loading.dismiss();
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    loading.dismiss();
                }
            }
        });
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
                                                                            String price_str = String.valueOf(order_obj.get("price"));
                                                                            String service_id = String.valueOf(order_obj.get("service_id"));
                                                                            String cus_brgy = String.valueOf(order_obj.get("cus_brgy"));
                                                                            String date_ordered = String.valueOf(order_obj.get("date_ordered"));
                                                                            String order_image = String.valueOf(order_obj.get("order_image"));
                                                                            String client_id_str = String.valueOf(order_obj.get("client_id"));
                                                                            String order_status = String.valueOf(order_obj.get("order_status"));
                                                                            String order_id = String.valueOf(order_id_obj.names().get(a));

                                                                            Calendar cal = Calendar.getInstance();
                                                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                                                            String date_now = dateFormat.format(cal.getTime());

                                                                            if (client_id.equals(client_id_str) && date_ordered.contains(date_now) && order_status.equals("Completed")) {
                                                                                Date date = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss-a").parse(date_ordered);
                                                                                int date_int = (int)date.getTime();
                                                                                order_item.add(new ClientOrderItem(category, cus_brgy, order_id, customer_id_str, price_str, order_image, "Completed", date_int, date.getTime()));
                                                                                buildOrders();
                                                                            }

                                                                            if (a + 1 == order_id_array.length()) {
                                                                                if (order_item.isEmpty()) {
                                                                                    nothing_found_tv.setVisibility(View.VISIBLE);
                                                                                    nothing_found_tv.setText("No orders today.");
                                                                                }

                                                                                getProfileInfo();
                                                                            }
                                                                        }
                                                                    } catch (JSONException | ParseException e) {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        nothing_found_tv.setVisibility(View.VISIBLE);
                                                        nothing_found_tv.setText("No orders today.");
                                                        getProfileInfo();
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
}
