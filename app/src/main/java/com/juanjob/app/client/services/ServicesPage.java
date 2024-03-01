package com.juanjob.app.client.services;

import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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

public class ServicesPage extends Fragment {
    private ImageView img_view, back_btn;
    private TextView service_desc, service_name, service_location, service_category, service_status, service_price_range;
    private Button delete_btn, edit_btn, add_btn;
    private String service_id, client_id;
    private AlertDialog loading_screen, delete_service_dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_service_page, container, false);
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

        SharedPreferences login_sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        client_id = login_sp.getString("login_id", "");

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

        delete_btn = v.findViewById(R.id.delete_btn);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteServicePrompt();
            }
        });

        add_btn = v.findViewById(R.id.add_btn);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkOnlineStatus();
                Toast.makeText(getContext(), "Only one service per client is available at the moment.", Toast.LENGTH_LONG).show();
            }
        });

        edit_btn = v.findViewById(R.id.edit_btn);
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeServiceId(service_id);
                goToEditServicePage();
            }
        });

        getServiceInfo();

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

                    String service_name_str = String.valueOf(sku_obj.get("name"));
                    service_name.setText(service_name_str);

                    String service_desc_str = String.valueOf(sku_obj.get("description"));
                    service_desc.setText(service_desc_str);

                    String service_location_str = String.valueOf(sku_obj.get("location"));
                    service_location.setText(service_location_str);

                    String service_status_str = String.valueOf(sku_obj.get("status"));
                    service_status.setText(service_status_str);

                    String category_str = String.valueOf(sku_obj.get("category"));
                    service_category.setText(category_str);

                    String price_range = String.valueOf(sku_obj.get("price_range"));
                    service_price_range.setText(price_range);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    void setImageView(Bitmap bitmap) {
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

    private void deleteServicePrompt() {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(getContext());
        View mview = getLayoutInflater().inflate(R.layout.prompt_dialog, null);
        mbuilder.setView(mview);
        delete_service_dialog = mbuilder.create();
        delete_service_dialog.show();
        delete_service_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button close = mview.findViewById(R.id.btn_cancel_logout);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_service_dialog.dismiss();
            }
        });

        Button proceed = mview.findViewById(R.id.btn_proceed_logout);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteService();
            }
        });

        TextView logout_text = mview.findViewById(R.id.prompt_text);
        logout_text.setText("Are you sure you want to delete this service?");
    }

    private void deleteService() {
        loading_screen.show();
        new Repositories().deleteService(service_id, client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (result.equals("success")) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loading_screen.dismiss();
                            Toast.makeText(getContext(), "Service Deleted!", Toast.LENGTH_SHORT).show();
                            reloadMyServicesPage();
                        }
                    }, 1000);
                } else {
                    loading_screen.dismiss();
                    Toast.makeText(getContext(), "Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void reloadMyServicesPage() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.client_fragmentContainerView, new MyServicesPage(), "services_page").commit();
    }

    private void goToCreateServicePage() {
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.client_fragmentContainerView, new CreateServicePage())
                .addToBackStack(null).commit();
    }

    private void checkOnlineStatus() {
        loading_screen.show();
        new Repositories().getClientStatus(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (result.equals("\"Active\"")) {
                    goToCreateServicePage();
                } else if (result.equals("\"Inactive\"")){
                    Toast.makeText(getContext(), "Please go online to add a service.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Please Try Again.", Toast.LENGTH_SHORT).show();
                }
                loading_screen.dismiss();
            }
        });
    }

    private void goToEditServicePage() {
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.client_fragmentContainerView, new EditServicePage())
                .addToBackStack(null).commit();
    }

    private void storeServiceId(String service_id) {
        SharedPreferences sp = requireActivity().getSharedPreferences("Service", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("service_id", service_id);
        editor.commit();
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
}
