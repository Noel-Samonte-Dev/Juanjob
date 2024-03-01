package com.juanjob.app.customer.profile;

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
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONException;
import org.json.JSONObject;

import com.juanjob.app.R;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;
import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerProfilePage extends Fragment {
    private CircleImageView profile_img;
    private TextView profile_name, toggle_status, home_address, contact_number, email, gender, account_status;
    private SwitchMaterial toggle;
    private AlertDialog loading;
    private boolean is_verified, is_active;
    private String customer_id;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customer_profile_page, container, false);
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

        profile_img = v.findViewById(R.id.profile_img);
        profile_name = v.findViewById(R.id.profile_name);
        toggle_status = v.findViewById(R.id.toggle_status);
        toggle = v.findViewById(R.id.toggle);
        home_address = v.findViewById(R.id.home_address);
        contact_number = v.findViewById(R.id.contact_number);
        email = v.findViewById(R.id.email);
        gender = v.findViewById(R.id.gender);
        account_status = v.findViewById(R.id.account_status);

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!is_verified) {
                    toggle.setChecked(false);
                    is_active = false;
                    String msg = "Accounts should be verified first before going online. Please contact the Juanjob HR department at juanjob@gmail.com for your account verification request status.";
                    ToastMsg.error_toast(getContext(), msg);
                } else if (toggle.isChecked()) {
                    is_active = true;
                    changeOnlineStatus();
                } else {
                    is_active = false;
                    changeOnlineStatus();
                }
            }
        });

        Button edit_btn = v.findViewById(R.id.edit_btn);
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEditProfilePage();
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

    private void getCustomerInfo() {
        loading.show();
        new Repositories().getCustomer(customer_id, new Repositories.RepoCallback() {
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

                        profile_name.setText(full_name);
                        account_status.setText(account_status_str);
                        home_address.setText(address_str);
                        gender.setText(gender_str);
                        email.setText(email_str);
                        contact_number.setText(mobile_str);

                        is_verified = !account_status_str.equals("Unverified");
                        is_active = online_status_str.equals("Active");

                        if (is_active) {
                            toggle.setChecked(true);
                            toggle.getThumbDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.green), PorterDuff.Mode.SRC_IN);
                            toggle.getTrackDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.green), PorterDuff.Mode.SRC_IN);
                            profile_img.setBorderColor(Color.parseColor("#008000"));
                            toggle_status.setText("Online");
                        }

                        setImageView(profile_img_str);

                        is_active = true;
                        changeOnlineStatus();

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

        Glide.with(requireContext())
                .applyDefaultRequestOptions(options)
                .asBitmap()
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.default_img)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        profile_img.setImageBitmap(resource);
                    }
                });
    }

    private void changeOnlineStatus() {
        loading.show();
        String online_status = is_active ? "Active" : "Inactive";
        new Repositories().updateCustomerOnlineStatus(customer_id, online_status, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                String msg = "Failed to update status, please try again.";
                if (result.equals("success")) {
                    msg = "Online Status Successfully Updated!";
                    if (is_active) {
                        toggle.getThumbDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.green), PorterDuff.Mode.SRC_IN);
                        toggle.getTrackDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.green), PorterDuff.Mode.SRC_IN);
                        profile_img.setBorderColor(Color.parseColor("#008000"));
                        toggle_status.setText("Online");
                    } else {
                        toggle.getThumbDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
                        toggle.getTrackDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
                        profile_img.setBorderColor(Color.parseColor("#ADABAB"));
                        toggle_status.setText("Offline");
                    }
                }

                //Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                loading.dismiss();
            }
        });
    }

    private void goToEditProfilePage() {
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.customer_fragmentContainerView, new CustomerEditProfilePage())
                .addToBackStack(null).commit();
    }
}
