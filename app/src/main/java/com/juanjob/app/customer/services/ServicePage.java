package com.juanjob.app.customer.services;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.juanjob.app.R;
import com.juanjob.app.database.OrderTable;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;

import de.hdodenhof.circleimageview.CircleImageView;

public class ServicePage extends Fragment {
    private ImageView back_btn;
    private TextView service_desc, service_name, service_location, service_category, service_status, service_price_range,
            customer_brgy_tv, order_img_tv;
    private Button order_btn;
    private String service_id, customer_id, client_id;
    private AlertDialog loading_screen;
    private CircleImageView img_view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customer_service_page, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading_screen = new LoadingPage().loadingGif(getActivity());
        loading_screen.show();

        SharedPreferences sp = getActivity().getSharedPreferences("Service", MODE_PRIVATE);
        service_id = sp.getString("service_id", "");
        client_id = sp.getString("client_id", "");

        SharedPreferences login_sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        customer_id = login_sp.getString("login_id", "");

        customer_brgy_tv = v.findViewById(R.id.customer_brgy_tv);

        order_img_tv = v.findViewById(R.id.order_img_tv);
        img_view = v.findViewById(R.id.img_view);
        service_desc = v.findViewById(R.id.service_desc);
        service_name = v.findViewById(R.id.service_name);
        service_price_range = v.findViewById(R.id.service_price_range);
        service_location = v.findViewById(R.id.service_location);
        service_category = v.findViewById(R.id.service_category);
        service_status = v.findViewById(R.id.service_status);
        getKeyboardEventStatus(service_status);

        back_btn = v.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        order_btn = v.findViewById(R.id.order_btn);
        order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceInfoPage();
            }
        });

        getServiceInfo();

        SwipeRefreshLayout swipe_refresh = v.findViewById(R.id.swipe_refresh);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setRefreshing(false);
                getServiceInfo();
            }
        });
    }

    private void createOrder(String cus_brgy, String cus_landmark, String cus_desc) {
        loading_screen.show();
        String service_price_range_str = service_price_range.getText().toString();
        String img_str = order_img_tv.getText().toString();
        OrderTable oder_table = new OrderTable(dateCreated(), "Pending", service_id, client_id, "0", cus_brgy, cus_landmark, cus_desc, service_price_range_str, img_str);
        new Repositories().createOrder(oder_table, customer_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                String msg = "Please Try Again.";
                if (result.equals("success")) {
                    msg = "Successfully Created Order!";
                    getServiceInfo();
                }

                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                loading_screen.dismiss();
            }
        });
    }

    private void updateService(String cus_brgy, String cus_landmark, String cus_desc) {
        new Repositories().updateServiceActiveStatus(service_id, client_id, "Pending", new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (result.equals("success")) {
                    new Repositories().updateServiceCustomer(service_id, client_id, customer_id, new Repositories.RepoCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if (result.equals("success")) {
                                createOrder(cus_brgy, cus_landmark, cus_desc);
                            } else {
                                Toast.makeText(getContext(), "Please Try Again", Toast.LENGTH_SHORT).show();
                                loading_screen.dismiss();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Please Try Again", Toast.LENGTH_SHORT).show();
                    loading_screen.dismiss();
                }
            }
        });
    }

    private String dateCreated() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss-a");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 8);
        return dateFormat.format(calendar.getTime());
    }

    private void getServiceInfo() {
        new Repositories().getService(service_id, client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loading_screen.dismiss();
                    }
                }, 1000);

                try {
                    JSONObject sku_obj = new JSONObject(result);
                    Bitmap url = BitmapHelper.urlStrToBitmap(String.valueOf(sku_obj.get("img")));
                    setImageView(url);

                    order_img_tv.setText(sku_obj.getString("img"));

                    String service_name_str = String.valueOf(sku_obj.get("name"));
                    service_name.setText(service_name_str);

                    String service_desc_str = String.valueOf(sku_obj.get("description"));
                    service_desc.setText(service_desc_str);

                    String service_location_str = String.valueOf(sku_obj.get("location"));
                    service_location.setText(service_location_str);

                    String service_status_str = String.valueOf(sku_obj.get("status"));
                    service_status.setText(service_status_str);

                    if (!service_status_str.equals("Active")) {
                        order_btn.setEnabled(false);
                        order_btn.setBackgroundResource(R.drawable.custom_btn_bg_disabled);
                    }

                    String category_str = String.valueOf(sku_obj.get("category"));
                    service_category.setText(category_str);

                    String price_range = String.valueOf(sku_obj.get("price_range"));
                    service_price_range.setText(price_range);

                    if (prompt_dialog != null) {
                        prompt_dialog.dismiss();
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
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

    private void getKeyboardEventStatus(TextView textView) {
        textView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = textView.getText().toString().trim().toLowerCase();
                textView.setTextColor(Color.parseColor(text.equals("active") ? "#008000" : "#ADABAB"));
            }
        });
    }

    private TextInputEditText landmark_text_field, description_text_field;
    private AlertDialog prompt_dialog;

    private void serviceInfoPage() {
        View mview = View.inflate(requireContext(), R.layout.customer_order_now_prompt, null);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(requireContext());
        mbuilder.setView(mview);
        mbuilder.setCancelable(false);
        mbuilder.setTitle(null);
        prompt_dialog = mbuilder.create();
        prompt_dialog.show();

        landmark_text_field = mview.findViewById(R.id.landmark_text_field);
        description_text_field = mview.findViewById(R.id.description_text_field);

        brgyPicker(mview);

        Button cancel_btn = mview.findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prompt_dialog.dismiss();
            }
        });

        Button save_btn = mview.findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String brgy = customer_brgy_tv.getText().toString();
                String landmark = String.valueOf(landmark_text_field.getText());
                String desc = String.valueOf(description_text_field.getText());
                if (!brgy.trim().isEmpty() && !landmark.trim().isEmpty() && !desc.trim().isEmpty()) {
                    loading_screen.show();
                    updateService(brgy, landmark, desc);
                } else {
                    Toast.makeText(getContext(), "Please provide all required fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void brgyPicker(View view) {
        List<String> brgy_list = new ArrayList<>();
        brgy_list.add("");
        brgy_list.add("Bancal");
        brgy_list.add("Barangay 1");
        brgy_list.add("Barangay 2");
        brgy_list.add("Barangay 3");
        brgy_list.add("Barangay 4");
        brgy_list.add("Barangay 5");
        brgy_list.add("Barangay 6");
        brgy_list.add("Barangay 7");
        brgy_list.add("Barangay 8");
        brgy_list.add("Cabilang Baybay");
        brgy_list.add("Lantic");
        brgy_list.add("Mabuhay");
        brgy_list.add("Maduya");
        brgy_list.add("Milagrosa");

        Spinner brgy_dropdown = view.findViewById(R.id.brgy_dropdown);
        ConstraintLayout brgy_box = view.findViewById(R.id.brgy_box);
        TextView brgy_textview = view.findViewById(R.id.brgy_textview);
        brgy_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brgy_dropdown.performClick();
            }
        });

        brgy_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brgy_dropdown.performClick();
            }
        });

        ArrayAdapter<String> brgy_adapter = new ArrayAdapter<String>(requireContext(),
                R.layout.spinner_item, brgy_list);
        brgy_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brgy_dropdown.setAdapter(brgy_adapter);
        brgy_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index = brgy_dropdown.getSelectedItemPosition();
                if (index > 0) {
                    brgy_textview.setText(brgy_list.get(index));
                    brgy_textview.setTextColor(Color.parseColor("#000000"));
                    customer_brgy_tv.setText(brgy_list.get(index));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
