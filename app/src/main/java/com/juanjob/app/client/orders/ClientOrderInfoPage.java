package com.juanjob.app.client.orders;

import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import org.json.JSONException;
import org.json.JSONObject;
import com.juanjob.app.R;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;

public class ClientOrderInfoPage extends Fragment {
    private AlertDialog loading;
    private String client_id;
    private ImageView img_view;
    private TextView service_desc, service_name, service_price_range, service_location, service_category, service_id_tv,
            worker_name, worker_mobile, worker_email, order_id_tv, service_status, customer_service_location, customer_service_landmark;
    private Button decline_btn, accept_btn, cancelled_btn, completed_btn;
    private LinearLayout button_panel_1;
    private LinearLayoutCompat ratingbar_layout;
    private RatingBar worker_ratingbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_order_info_page, container, false);
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

        ImageView back_btn = v.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        ratingbar_layout = v.findViewById(R.id.ratingbar_layout);
        worker_ratingbar = v.findViewById(R.id.worker_ratingbar);
        customer_service_landmark = v.findViewById(R.id.customer_service_landmark);
        customer_service_location = v.findViewById(R.id.customer_service_location);
        service_id_tv = v.findViewById(R.id.service_id_tv);
        img_view = v.findViewById(R.id.img_view);
        service_desc = v.findViewById(R.id.service_desc);
        service_name = v.findViewById(R.id.service_name);
        service_price_range = v.findViewById(R.id.service_price_range);
        service_location = v.findViewById(R.id.service_location);
        service_category = v.findViewById(R.id.service_category);
        worker_name = v.findViewById(R.id.customer_name);
        worker_mobile = v.findViewById(R.id.customer_mobile);
        worker_email = v.findViewById(R.id.customer_email);
        order_id_tv = v.findViewById(R.id.order_id);
        service_status = v.findViewById(R.id.service_status);
        button_panel_1 = v.findViewById(R.id.button_panel_1);

        decline_btn = v.findViewById(R.id.decline_btn);
        decline_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrder(false);
            }
        });

        accept_btn = v.findViewById(R.id.accept_btn);
        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrder(true);
            }
        });

//        cancelled_btn = v.findViewById(R.id.cancelled_btn);
//        cancelled_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateOrderStatus("Cancelled");
//            }
//        });

        completed_btn = v.findViewById(R.id.completed_btn);
        completed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus("Completed");
            }
        });

        getOrders();

        SwipeRefreshLayout swipe_refresh = v.findViewById(R.id.swipe_refresh);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setRefreshing(false);
                getOrders();
            }
        });
    }

    private void getOrders() {
        loading.show();
        SharedPreferences sp = requireActivity().getSharedPreferences("Client Orders", MODE_PRIVATE);
        String order_id = sp.getString("order_id", "");
        String customer_id = sp.getString("customer_id", "");
        order_id_tv.setText(order_id);
        new Repositories().getOrder(order_id, customer_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                System.out.println(order_id);
                if (!result.equals("order_not_found")) {
                    try {
                        JSONObject order_obj = new JSONObject(result);
                        String service_id = String.valueOf(order_obj.get("service_id"));
                        service_id_tv.setText(service_id);
                        String date_ordered = String.valueOf(order_obj.get("date_ordered"));
                        String client_id_str = String.valueOf(order_obj.get("client_id"));
                        if (client_id_str.equals(client_id)) {
                            String order_status = String.valueOf(order_obj.get("order_status"));
                            service_status.setText(order_status);
//                            if (order_status.equals("Pending")) {
//                                button_panel.setVisibility(View.VISIBLE);
//                            }

                            String cus_brgy = String.valueOf(order_obj.get("cus_brgy"));
                            String cus_landmark = String.valueOf(order_obj.get("cus_landmark"));
                            String cus_desc = String.valueOf(order_obj.get("cus_desc"));
                            String landmark = cus_landmark + "\n" + cus_desc;
                            customer_service_location.setText(cus_brgy);
                            customer_service_landmark.setText(landmark);

                            String order_rating = String.valueOf(order_obj.get("rating"));
                            if (Double.parseDouble(order_rating) != 0) {
                                ratingbar_layout.setVisibility(View.VISIBLE);
                                worker_ratingbar.setRating(Float.parseFloat(order_rating));
                            }

                            if (order_status.equals("Accepted")) {
                                button_panel_1.setVisibility(View.VISIBLE);
                            }

                            new Repositories().getService(service_id, client_id, new Repositories.RepoCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    if (!result.equals("service_not_found")) {
                                        try {
                                            JSONObject sku_obj = new JSONObject(result);
                                            Bitmap url = BitmapHelper.urlStrToBitmap(String.valueOf(sku_obj.get("img")));
                                            setImageView(url);
                                            String category = String.valueOf(sku_obj.get("category"));
                                            service_category.setText(category);
                                            String location = String.valueOf(sku_obj.get("location"));
                                            service_location.setText(location);
                                            String service = String.valueOf(sku_obj.get("name"));
                                            service_name.setText(service);
                                            String price_range = String.valueOf(sku_obj.get("price_range"));
                                            service_price_range.setText(price_range);
                                            String description = String.valueOf(sku_obj.get("description"));
                                            service_desc.setText(description);
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            });

                            new Repositories().getClient(client_id, new Repositories.RepoCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    if (!result.equals("user_not_found")) {
                                        try {
                                            JSONObject obj = new JSONObject(result);
                                            String email_str = String.valueOf(obj.get("email"));
                                            worker_email.setText(email_str);
                                            String mobile_str = String.valueOf(obj.get("mobile"));
                                            worker_mobile.setText(mobile_str);
                                            String firstname = String.valueOf(obj.get("firstname"));
                                            String lastname = String.valueOf(obj.get("lastname"));
                                            String full_name = firstname + " " + lastname;
                                            worker_name.setText(full_name);
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                    loading.dismiss();
                                }
                            });
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

    private void setImageView(Bitmap bitmap) {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .priority(Priority.HIGH)
                .format(DecodeFormat.PREFER_RGB_565);

        Glide.with(requireActivity())
                .applyDefaultRequestOptions(options)
                .asBitmap()
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.app_logo)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        img_view.setImageBitmap(resource);
                    }
                });
    }

    private void updateOrder(boolean is_accepted) {
        SharedPreferences sp = requireActivity().getSharedPreferences("Client Orders", MODE_PRIVATE);
        String order_id = sp.getString("order_id", "");
        String customer_id = sp.getString("customer_id", "");
        String order_status = is_accepted ? "Accepted" : "Declined";
        loading.show();

        new Repositories().updateOrderStatus(customer_id, order_id, order_status, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                loading.dismiss();
                if (result.equals("success")) {
                    String msg = "Successfully updated order status";
                    Toast.makeText(getContext(),msg, Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    String msg = "Something went wrong, please try again.";
                    Toast.makeText(getContext(),msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateOrderStatus(String order_status) {
        SharedPreferences sp = requireActivity().getSharedPreferences("Client Orders", MODE_PRIVATE);
        String order_id = sp.getString("order_id", "");
        String customer_id = sp.getString("customer_id", "");
        loading.show();

        if (order_status.equals("Completed")) {
            new Repositories().getClient(client_id, new Repositories.RepoCallback() {
                @Override
                public void onSuccess(String result) {
                    if (!result.equals("user_not_found")) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            int completed_orders = obj.getInt("completed_orders");
                            new Repositories().updateClientCompletedOrders(client_id, completed_orders + 1, new Repositories.RepoCallback() {
                                @Override
                                public void onSuccess(String result) {

                                }
                            });

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }

        if (order_status.equals("Cancelled") || order_status.equals("Completed")) {
            String service_status = order_status.equals("Cancelled") ? "Cancelled" : "Completed";
            String service_id = service_id_tv.getText().toString();
            new Repositories().updateServiceActiveStatus(service_id, client_id, "Active", new Repositories.RepoCallback() {
                @Override
                public void onSuccess(String result) {

                }
            });
        }

        new Repositories().updateOrderStatus(customer_id, order_id, order_status, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                loading.dismiss();
                if (result.equals("success")) {
                    String msg_cancelled = "Successfully cancelled order.";
                    String msg_completed = "Successfully completed order.";
                    String msg = order_status.equals("Cancelled") ? msg_cancelled : msg_completed;
                    Toast.makeText(getContext(),msg, Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    String msg = "Something went wrong, please try again.";
                    Toast.makeText(getContext(),msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
