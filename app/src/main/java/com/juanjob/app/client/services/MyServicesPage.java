package com.juanjob.app.client.services;

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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.juanjob.app.R;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.customer.services.ServicesItems;

public class MyServicesPage extends Fragment {
    private String client_id;
    private TextView no_services_found;
    private AlertDialog loading_screen;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_services_page, container, false);
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

        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        client_id = sp.getString("login_id", "");

        no_services_found = v.findViewById(R.id.no_services_found);
        services_rec_view = v.findViewById(R.id.services_rv);

        Button add_service = v.findViewById(R.id.add_service);
        add_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOnlineStatus();
            }
        });

        Button clear_all_btn = v.findViewById(R.id.clear_all_btn);
        clear_all_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (services_item.isEmpty()) {
                    Toast.makeText(getContext(), "There is nothing to clear.", Toast.LENGTH_SHORT).show();
                } else {
                    deleteAllServicesPrompt();
                }
            }
        });

        getMyServices();
    }

    private AlertDialog clear_all_prompt_dialog;
    private void deleteAllServicesPrompt() {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(getContext());
        View mview = getLayoutInflater().inflate(R.layout.prompt_dialog, null);
        mbuilder.setView(mview);
        clear_all_prompt_dialog = mbuilder.create();
        clear_all_prompt_dialog.show();
        clear_all_prompt_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button close = mview.findViewById(R.id.btn_cancel_logout);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear_all_prompt_dialog.dismiss();
            }
        });

        Button proceed = mview.findViewById(R.id.btn_proceed_logout);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllServices();
            }
        });

        TextView logout_text = mview.findViewById(R.id.prompt_text);
        logout_text.setText("Are you sure you want to clear all services?");
    }

    private void deleteAllServices() {
        new Repositories().deleteAllService(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (result.equals("success")) {
                    Toast.makeText(getContext(), "All Cleared!", Toast.LENGTH_SHORT).show();
                    services_item.clear();
                    services_item = new ArrayList<>();
                    buildServices();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            clear_all_prompt_dialog.dismiss();
                        }
                    }, 1000);
                } else {
                    Toast.makeText(getContext(), "Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToCreateServicePage() {
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.client_fragmentContainerView, new CreateServicePage())
                .addToBackStack(null).commit();
    }

    private RecyclerView services_rec_view;
    private LinearLayoutManager services_rec_manager;
    private List<ServicesItems> services_item = new ArrayList<>();
    private MyServicesAdapter services_adapter;
    private void buildServices() {
        no_services_found.setVisibility(services_item.isEmpty() ? View.VISIBLE : View.GONE);
        services_rec_view.setHasFixedSize(false);
        services_rec_manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        services_adapter = new MyServicesAdapter(getActivity(), services_item, services_rec_view);
        services_rec_view.setLayoutManager(services_rec_manager);
        services_rec_view.setAdapter(services_adapter);
    }

    private void getMyServices() {
        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
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
                            String sku = String.valueOf(array.get(i));
                            JSONObject sku_obj = new JSONObject(sku);
                            Bitmap url = BitmapHelper.urlStrToBitmap(String.valueOf(sku_obj.get("img")));
                            String service_id = Objects.requireNonNull(service_id_obj.names()).get(i).toString();
                            String service_name = String.valueOf(sku_obj.get("name"));
                            String service_desc = String.valueOf(sku_obj.get("description"));
                            String service_location = String.valueOf(sku_obj.get("location"));
                            String service_status = String.valueOf(sku_obj.get("status"));
                            String service_customer = String.valueOf(sku_obj.get("customer_id"));
                            String client_id = String.valueOf(sku_obj.get("client_id"));
                            String category = String.valueOf(sku_obj.get("category"));

                            if (!service_status.equals("Cancelled") && !service_status.equals("Completed")) {
                                services_item.add(new ServicesItems(url, service_id, service_name, service_desc, service_location,
                                        service_status, service_customer,client_id, null, "", ""));
                            }

                            if (i + 1 == Objects.requireNonNull(array).length()) {
                                buildServices();
                                loading_screen.dismiss();
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    loading_screen.dismiss();
                    buildServices();
                }
            }
        });
    }

    private void checkOnlineStatus() {
        loading_screen.show();
        new Repositories().getClientStatus(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (result.equals("\"Active\"") && services_item.size() == 0) {
                    goToCreateServicePage();
                } else if (services_item.size() > 0) {
                    Toast.makeText(getContext(), "Only one service per client is available at the moment.", Toast.LENGTH_LONG).show();
                }
                else if (result.equals("\"Inactive\"")){
                    Toast.makeText(getContext(), "Please go online to add a service.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), "Please Try Again.", Toast.LENGTH_SHORT).show();
                }
                loading_screen.dismiss();
            }
        });
    }
}
