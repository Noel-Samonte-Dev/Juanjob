package com.juanjob.app.customer.search;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.juanjob.app.R;
import com.juanjob.app.customer.services.ServicesAdapter;
import com.juanjob.app.customer.services.ServicesItems;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SearchModalSheet extends BottomSheetDialogFragment implements SearchAdapter.SelectedService {
    private AlertDialog loading_screen;
    private View mv;
    private BottomSheetDialog dialog;
    private BottomSheetBehavior bottomSheetBehavior;
    private Button search_button_icon;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        ConstraintLayout layout = dialog.findViewById(R.id.sheet_layout);
        assert layout != null;
        layout.setMinHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loading_screen = new LoadingPage().loadingGif(getActivity());
        mv = inflater.inflate(R.layout.bottom_sheet_search, container, false);
        rec_view = mv.findViewById(R.id.modal_recycler_view);

        TextInputEditText searchbar_suggestions_txt = mv.findViewById(R.id.searchbar_suggestions_txt);
        search_button_icon = mv.findViewById(R.id.search_button_icon);
        search_button_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchbar_suggestions_txt.setText("");
            }
        });

        ImageView modal_close = mv.findViewById(R.id.modal_close);
        modal_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getActiveServices();
        textOnKeyListener(searchbar_suggestions_txt);
        return mv;
    }

    //Available Services
    private List<ServicesItems> services_item = new ArrayList<>();
    private List<ServicesItems> services_item1 = new ArrayList<>();
    private RecyclerView rec_view;

    private void buildServices() {
        rec_view.setHasFixedSize(false);
        LinearLayoutManager services_rec_manager = new LinearLayoutManager(getContext());
        SearchAdapter services_adapter = new SearchAdapter(getActivity(), services_item, rec_view, this);
        rec_view.setLayoutManager(services_rec_manager);
        rec_view.setAdapter(services_adapter);
    }

    private void getActiveServices() {
        //loading_screen.show();
        services_item = new ArrayList<>();
        buildServices();
        new Repositories().getAllServices(new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("services_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject client_id_obj = obj.getJSONObject("client_id");
                        JSONArray client_id_array = client_id_obj.toJSONArray(client_id_obj.names());
                        for (int a = 0; a < Objects.requireNonNull(client_id_array).length(); a++) {
                            JSONObject client_id_obj1 = new JSONObject(String.valueOf(client_id_array.get(a)));
                            JSONObject service_id_obj = client_id_obj1.getJSONObject("service_id");
                            JSONArray array = service_id_obj.toJSONArray(service_id_obj.names());
                            for (int i = 0; i < Objects.requireNonNull(array).length(); i++) {
                                String sku = String.valueOf(array.get(i));
                                JSONObject sku_obj = new JSONObject(sku);
                                Bitmap url = BitmapHelper.urlStrToBitmap(String.valueOf(sku_obj.get("img")));
                                String service_id = String.valueOf(service_id_obj.names().get(i));
                                String service_name = String.valueOf(sku_obj.get("name"));
                                String service_desc = String.valueOf(sku_obj.get("description"));
                                String service_location = String.valueOf(sku_obj.get("location"));
                                String service_status = String.valueOf(sku_obj.get("status"));
                                String service_customer = String.valueOf(sku_obj.get("customer_id"));
                                String client_id = String.valueOf(sku_obj.get("client_id"));
                                String category = String.valueOf(sku_obj.get("category"));
                                String price_range = String.valueOf(sku_obj.get("price_range"));

                                if (!service_status.equalsIgnoreCase("cancelled") && !service_status.equalsIgnoreCase("completed")) {
                                    int finalI = a;
                                    new Repositories().getClient(client_id, new Repositories.RepoCallback() {
                                        @Override
                                        public void onSuccess(String result) {
                                            String profile_img_str = "";
                                            String client_name_str = "";
                                            String rating_str = "";
                                            if (!result.equals("user_not_found")) {
                                                try {
                                                    JSONObject obj = new JSONObject(result);
                                                    profile_img_str = String.valueOf(obj.get("profile_img"));
                                                    String firstname = String.valueOf(obj.get("firstname"));
                                                    String lastname = String.valueOf(obj.get("lastname"));
                                                    client_name_str = firstname.toUpperCase() + " " + lastname.toUpperCase();
                                                    rating_str = String.valueOf(obj.get("rating"));

                                                    Bitmap profile_img = BitmapHelper.urlStrToBitmap(profile_img_str);
                                                    if (service_status.equals("Active")) {
                                                        services_item1.add(new ServicesItems(url, service_id, category, rating_str, service_location, service_status,
                                                                service_customer, client_id, profile_img, client_name_str, price_range));
                                                    }

                                                    if (finalI + 1 == Objects.requireNonNull(client_id_array).length()) {
                                                        if (loading_screen != null) {
                                                            loading_screen.dismiss();
                                                        }
                                                    }

                                                } catch (JSONException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    if (client_id_array.length() == a + 1) {
                                        if (loading_screen != null) {
                                            loading_screen.dismiss();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    if (loading_screen != null) {
                        loading_screen.dismiss();
                    }
                    services_item = new ArrayList<>();
                    buildServices();
                }
            }
        });
    }

    private void textOnKeyListener(TextInputEditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = editText.getText().toString().toLowerCase().trim();
                if (text.trim().isEmpty()) {
                    services_item.clear();
                    buildServices();
                    search_button_icon.setBackgroundResource(R.drawable.ic_search);
                } else {
                    services_item.clear();
                    search_button_icon.setBackgroundResource(R.drawable.ic_clear_black);
                    services_item = services_item1.stream()
                            .filter(p -> p.getClient_name().toLowerCase().trim().contains(text)).collect(Collectors.toList());
                    buildServices();
                }
            }
        });
    }

    @Override
    public void onSelectedSearch(String service) {

    }
}
