package com.juanjob.app.customer.profile;

import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.juanjob.app.R;
import com.juanjob.app.customer.services.ServicePage;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerClientProfilePage extends Fragment {
    private TextView profile_name, home_address, contact_number, email, gender, service_location, service_category, account_status,
            completed_orders_tv;
    private String client_id;
    private AlertDialog loading;
    private RatingBar worker_rating;
    private CircleImageView profile_img;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customer_client_profile_page, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading = new LoadingPage().loadingGif(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences("Client", MODE_PRIVATE);
        client_id = sp.getString("client_id", "");

        ImageView back_btn = v.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        profile_img = v.findViewById(R.id.profile_img);
        completed_orders_tv = v.findViewById(R.id.completed_orders_tv);
        worker_rating = v.findViewById(R.id.worker_rating);
        profile_name = v.findViewById(R.id.profile_name);
        home_address = v.findViewById(R.id.home_address);
        contact_number = v.findViewById(R.id.contact_number);
        email = v.findViewById(R.id.email);
        gender = v.findViewById(R.id.gender);
        service_location = v.findViewById(R.id.service_location);
        service_category = v.findViewById(R.id.service_category);
        account_status = v.findViewById(R.id.account_status);
        Button service_btn = v.findViewById(R.id.service_btn);
        service_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotToServicePage();
            }
        });

        getCustomerInfo();

        SwipeRefreshLayout swipe_refresh = v.findViewById(R.id.swipe_refresh);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setRefreshing(false);
                getCustomerInfo();
            }
        });
    }

    private void gotToServicePage() {
        loading.show();
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
                            if (i == 0) {
                                storeServiceInfo(service_id, client_id);

                                FragmentManager manager = getParentFragmentManager();
                                manager.beginTransaction()
                                        .add(R.id.customer_fragmentContainerView, new ServicePage())
                                        .addToBackStack(null).commit();
                            }
                        }

                        loading.dismiss();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    loading.dismiss();
                    Toast.makeText(getContext(), "Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storeServiceInfo(String service_id, String client_id) {
        SharedPreferences sp = getActivity().getSharedPreferences("Service", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("service_id", service_id);
        editor.putString("client_id", client_id);
        editor.commit();
    }

    private void getCustomerInfo() {
        loading.show();
        new Repositories().getClient(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String category = String.valueOf(obj.get("category"));
                        String rating_str = String.valueOf(obj.get("rating"));
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
                        String full_name = firstname + " " + middle_name + " " + lastname;
                        int completed_orders = obj.getInt("completed_orders");

                        completed_orders_tv.setText(String.valueOf(completed_orders));

                        worker_rating.setRating(Float.parseFloat(rating_str));

                        profile_name.setText(full_name);
                        account_status.setText(account_status_str);
                        service_category.setText(category_str);
                        service_location.setText(service_location_str);
                        home_address.setText(address_str);
                        gender.setText(gender_str);
                        email.setText(email_str);
                        contact_number.setText(mobile_str);

                        setProfileImage(profile_img_str);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                loading.dismiss();
            }
        });
    }



    private void setProfileImage(String img_str) {
        Bitmap bitmap = BitmapHelper.urlStrToBitmap(img_str);
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .priority(Priority.HIGH)
                .format(DecodeFormat.PREFER_RGB_565);

        Glide.with(requireContext())
                .applyDefaultRequestOptions(options)
                .asBitmap()
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.default_img)
                .into(profile_img);
    }
}
