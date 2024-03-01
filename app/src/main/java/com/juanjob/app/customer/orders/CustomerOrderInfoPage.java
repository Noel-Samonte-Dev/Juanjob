package com.juanjob.app.customer.orders;

import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import java.util.Objects;

import com.juanjob.app.R;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;

public class CustomerOrderInfoPage extends Fragment {
    private AlertDialog loading;
    private String customer_id;
    private ImageView img_view;
    private TextView service_desc, service_name, service_price_range, service_location, service_category,
            worker_name, worker_mobile, worker_email, order_id_tv, service_status, client_id_tv, service_id_tv,
            rating_tv, rating_quantity_tv;
    private Button cancel_button;
    private ConstraintLayout cancel_button_cl, rate_worker_btn_cl;
    private LinearLayoutCompat ratingbar_layout;
    private RatingBar worker_ratingbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customer_order_info_page, container, false);
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
        customer_id = sp.getString("login_id", "");

        ImageView back_btn = v.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        worker_ratingbar = v.findViewById(R.id.worker_ratingbar);
        ratingbar_layout = v.findViewById(R.id.ratingbar_layout);
        rating_tv = v.findViewById(R.id.rating_tv);
        rating_quantity_tv = v.findViewById(R.id.rating_quantity_tv);
        service_id_tv = v.findViewById(R.id.service_id_tv);
        client_id_tv = v.findViewById(R.id.client_id_tv);
        img_view = v.findViewById(R.id.img_view);
        service_desc = v.findViewById(R.id.service_desc);
        service_name = v.findViewById(R.id.service_name);
        service_price_range = v.findViewById(R.id.service_price_range);
        service_location = v.findViewById(R.id.service_location);
        service_category = v.findViewById(R.id.service_category);
        worker_name = v.findViewById(R.id.worker_name);
        worker_mobile = v.findViewById(R.id.worker_mobile);
        worker_email = v.findViewById(R.id.worker_email);
        order_id_tv = v.findViewById(R.id.order_id);
        service_status = v.findViewById(R.id.service_status);

        cancel_button_cl = v.findViewById(R.id.cancel_button_cl);
        cancel_button = v.findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelOrder();
            }
        });

        Button rate_worker_btn = v.findViewById(R.id.rate_worker_btn);
        rate_worker_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateWorker();
            }
        });

        rate_worker_btn_cl = v.findViewById(R.id.rate_worker_btn_cl);

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
        SharedPreferences sp = requireActivity().getSharedPreferences("Customer Orders", MODE_PRIVATE);
        String order_id = sp.getString("order_id", "");
        order_id_tv.setText(order_id);
        new Repositories().getOrder(order_id, customer_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("order_not_found")) {
                    try {
                        JSONObject order_obj = new JSONObject(result);
                        String service_id = String.valueOf(order_obj.get("service_id"));
                        String order_rating = String.valueOf(order_obj.get("rating"));
                        service_id_tv.setText(service_id);
                        String date_ordered = String.valueOf(order_obj.get("date_ordered"));
                        String client_id = String.valueOf(order_obj.get("client_id"));
                        client_id_tv.setText(client_id);
                        String order_status = String.valueOf(order_obj.get("order_status"));
                        service_status.setText(order_status);

                        if (order_status.equals("Pending")) {
                            cancel_button_cl.setVisibility(View.VISIBLE);
                        }

                        if (order_status.equals("Completed") && Double.parseDouble(order_rating) == 0) {
                            rate_worker_btn_cl.setVisibility(View.VISIBLE);
                        } else {
                            rate_worker_btn_cl.setVisibility(View.GONE);
                        }

                        if (Double.parseDouble(order_rating) != 0) {
                            ratingbar_layout.setVisibility(View.VISIBLE);
                            worker_ratingbar.setRating(Float.parseFloat(order_rating));
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
                                        String rating_str = String.valueOf(obj.get("rating"));
                                        rating_tv.setText(rating_str);
                                        String rating_quantity_str = String.valueOf(obj.get("rating_quantity"));
                                        rating_quantity_tv.setText(rating_quantity_str);

                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                loading.dismiss();
                            }
                        });

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

    private void cancelOrder() {
        SharedPreferences sp = requireActivity().getSharedPreferences("Customer Orders", MODE_PRIVATE);
        String order_id = sp.getString("order_id", "");
        loading.show();

        String service_id = service_id_tv.getText().toString();
        String client_id = client_id_tv.getText().toString();
        new Repositories().updateServiceActiveStatus(service_id, client_id, "Cancelled", new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {

            }
        });

        new Repositories().updateOrderStatus(customer_id, order_id, "Cancelled", new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                loading.dismiss();
                if (result.equals("success")) {
                    String msg = "Successfully cancelled order.";
                    Toast.makeText(getContext(),msg, Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    String msg = "Something went wrong, please try again.";
                    Toast.makeText(getContext(),msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void rateWorker() {
        View mview = getLayoutInflater().inflate(R.layout.rate_worker, null);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(requireContext());
        mbuilder.setView(mview);
        mbuilder.setCancelable(false);
        mbuilder.setTitle(null);
        AlertDialog rating_view = mbuilder.create();
        rating_view.show();
        Objects.requireNonNull(rating_view.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView close_btn = mview.findViewById(R.id.close_btn);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating_view.dismiss();
            }
        });

        RatingBar worker_ratingbar = mview.findViewById(R.id.worker_ratingbar);
        Button rate_worker_btn = mview.findViewById(R.id.rate_worker_btn);
        rate_worker_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rating = String.valueOf(worker_ratingbar.getRating());
                if (Double.parseDouble(rating) > 0) {
                    loading.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateRating(rating, rating_view);
                        }
                    }, 2000);
                } else {
                    Toast.makeText(getContext(), "Rating should not be zero.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateRating(String rating, AlertDialog dialog) {
        String client_id = client_id_tv.getText().toString();
        int old_quantity = Integer.parseInt(rating_quantity_tv.getText().toString());
        int quantity = Integer.parseInt(rating_quantity_tv.getText().toString()) + 1;
        double rating_d = Double.parseDouble(rating) + (Double.parseDouble(rating_tv.getText().toString()) * (old_quantity == 0 ? 1 : old_quantity));
        String total_rating = String.valueOf(rating_d / quantity);

        new Repositories().updateOrderRating(customer_id, order_id_tv.getText().toString(), rating, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {

            }
        });

        new Repositories().updateClientRatingQuantity(client_id, String.valueOf(quantity), new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {

            }
        });

        new Repositories().updateClientRating(client_id, total_rating, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                loading.dismiss();
                if (result.equals("success")) {
                    getOrders();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Please Try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
