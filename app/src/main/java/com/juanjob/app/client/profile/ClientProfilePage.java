package com.juanjob.app.client.profile;

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
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import com.juanjob.app.R;
import com.juanjob.app.client.services.EditServicePage;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;
import de.hdodenhof.circleimageview.CircleImageView;

public class ClientProfilePage extends Fragment {
    private TextView home_address, contact_number, email, gender, service_location, service_category,
            account_status, profile_name, toggle_status, service_subcategory, price_range, completed_orders_tv, service_id_tv;
    private String client_id;
    private AlertDialog loading;
    private CircleImageView profile_img;
    private SwitchMaterial toggle;
    private boolean is_verified, is_active;
    private Button edit_btn;
    private RatingBar worker_rating;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_profile_page, container, false);
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

        service_id_tv = v.findViewById(R.id.service_id_tv);
        completed_orders_tv = v.findViewById(R.id.completed_orders_tv);
        worker_rating = v.findViewById(R.id.worker_rating);
        price_range = v.findViewById(R.id.price_range);
        service_subcategory = v.findViewById(R.id.service_subcategory);
        profile_name = v.findViewById(R.id.profile_name);
        profile_img = v.findViewById(R.id.profile_img);
        account_status = v.findViewById(R.id.account_status);
        service_category = v.findViewById(R.id.service_category);
        service_location = v.findViewById(R.id.service_location);
        gender = v.findViewById(R.id.gender);
        email = v.findViewById(R.id.email);
        home_address = v.findViewById(R.id.home_address);
        contact_number = v.findViewById(R.id.contact_number);

        toggle_status = v.findViewById(R.id.toggle_status);
        toggle = v.findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!is_verified) {
                    toggle.setChecked(false);
                    is_active = false;
                    String msg = "Accounts should be verified first before going online. Please contact the Juanjob HR department at hr@juanjob.com for your account verification request status.";
                    ToastMsg.error_toast(getContext(), msg);
                } else if (toggle.isChecked()) {
                    toggle.getThumbDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.green), PorterDuff.Mode.SRC_IN);
                    toggle.getTrackDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.green), PorterDuff.Mode.SRC_IN);
                    profile_img.setBorderColor(Color.parseColor("#008000"));
                    toggle_status.setText("Online");
                    is_active = true;
                } else {
                    toggle.getThumbDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
                    toggle.getTrackDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
                    profile_img.setBorderColor(Color.parseColor("#ADABAB"));
                    toggle_status.setText("Offline");
                    is_active = false;
                }

                changeOnlineStatus();
            }
        });

        getProfileInfo();

        edit_btn = v.findViewById(R.id.edit_btn);
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEditProfilePage();
            }
        });

        Button edit_service_btn = v.findViewById(R.id.edit_service_btn);
        edit_service_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeServiceId();
                goToEditService();
            }
        });

        getServiceInfo();

        SwipeRefreshLayout swipe_refresh = v.findViewById(R.id.swipe_refresh);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setRefreshing(false);
                getProfileInfo();
                getServiceInfo();
            }
        });
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
                        String full_name = firstname + " " + middle_name + " " + lastname;
                        String rating = obj.getString("rating");
                        int completed_orders = obj.getInt("completed_orders");

                        worker_rating.setRating(Float.parseFloat(rating));
                        completed_orders_tv.setText(String.valueOf(completed_orders));
                        profile_name.setText(full_name);
                        account_status.setText(account_status_str);
                        service_category.setText(category_str);
                        service_subcategory.setText(subcategory);
                        service_location.setText(service_location_str);
                        home_address.setText(address_str);
                        gender.setText(gender_str);
                        email.setText(email_str);
                        contact_number.setText(mobile_str);

                        is_verified = !account_status_str.equals("Unverified");
                        is_active = online_status_str.equals("Active");

                        if (is_active) {
                            toggle.setChecked(true);
                            toggle.getThumbDrawable().setColorFilter(ContextCompat.getColor(requireContext(), R.color.green), PorterDuff.Mode.SRC_IN);
                            toggle.getTrackDrawable().setColorFilter(ContextCompat.getColor(requireContext(), R.color.green), PorterDuff.Mode.SRC_IN);
                            profile_img.setBorderColor(Color.parseColor("#008000"));
                            toggle_status.setText("Online");
                        }

                        setImageView(profile_img_str);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                loading.dismiss();
            }
        });
    }

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

    private void setImageView(String img_str) {
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

    private void goToEditProfilePage() {
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.client_fragmentContainerView, new ClientEditProfilePage())
                .addToBackStack(null).commit();
    }

    private void goToEditService() {
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.client_fragmentContainerView, new EditServicePage())
                .addToBackStack(null).commit();
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
                            String price_range_str = String.valueOf(new JSONObject(String.valueOf(service_id_obj.get(service_id))).get("price_range"));
                            price_range.setText(price_range_str);
                            int finalI = i;
                            new Repositories().updateServiceActiveStatus(service_id, client_id, status, new Repositories.RepoCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    if (finalI + 1 == array.length()) {
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

    private void getServiceInfo() {
        loading.show();
        new Repositories().getAllClientServices(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("services_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject service_id_obj = obj.getJSONObject("service_id");
                        JSONArray array = service_id_obj.toJSONArray(service_id_obj.names());
                        if (Objects.requireNonNull(array).length() > 0) {
                            String service_id_str = Objects.requireNonNull(service_id_obj.names()).get(0).toString();
                            String service_id = String.valueOf(service_id_obj.get(service_id_str));
                            String price_range_str = String.valueOf(new JSONObject(service_id).get("price_range"));
                            price_range.setText(price_range_str);
                            service_id_tv.setText(service_id_str);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                loading.dismiss();
            }
        });
    }

    private void storeServiceId() {
        String service_id = service_id_tv.getText().toString().trim();
        SharedPreferences sp = getActivity().getSharedPreferences("Service", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("service_id", service_id);
        editor.commit();
    }
}
