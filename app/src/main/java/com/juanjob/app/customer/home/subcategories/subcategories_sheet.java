package com.juanjob.app.customer.home.subcategories;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.juanjob.app.R;
import com.juanjob.app.customer.home.featured_workers.FeaturedWorkerAdapter;
import com.juanjob.app.customer.home.featured_workers.FeaturedWorkerItem;
import com.juanjob.app.customer.services.ServicesAdapter;
import com.juanjob.app.customer.services.ServicesItems;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;

public class subcategories_sheet extends BottomSheetDialogFragment implements SubCategoriesAdapter.SelectedSubcategory, FeaturedWorkerAdapter.SelectedWorker,
        ServicesAdapter.SelectedService {
    private View mv;
    private String category;

    public String getCategory() {
        return category;
    }

    public subcategories_sheet(String category) {
        this.category = category;
    }

    private AlertDialog loading_screen;
    private TextView select_modal;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loading_screen = new LoadingPage().loadingGif(getActivity());
        mv = inflater.inflate(R.layout.bottom_sheet_modal, container, false);
        select_modal = mv.findViewById(R.id.select_modal);
        select_modal.setText(getCategory());
        ImageView categ_close = mv.findViewById(R.id.modal_close);
        categ_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        rec_view = mv.findViewById(R.id.modal_recycler_view);

        if (getCategory().equals("Electronics")) {
            buildElectronics();
        }

        if (getCategory().equals("Personal Care")) {
            buildSalon();
        }

        if (getCategory().equals("Household Maintenance")) {
            buildHousehold();
        }

        if (getCategory().equals("Mechanical")) {
            buildMechanical();
        }

        if (getCategory().equals("Categories")) {
            buildAllCategories();
        }

        if (getCategory().equals("Services")) {
            getActiveServices();
        }

        return mv;
    }

    private RecyclerView rec_view;
    private List<SubcategoriesItem> items = new ArrayList<>();

    private void buildSubcategory() {
        rec_view.setHasFixedSize(false);
        LinearLayoutManager rec_manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        SubCategoriesAdapter featured_adapter = new SubCategoriesAdapter(getActivity(), items, rec_view, this);
        rec_view.setLayoutManager(rec_manager);
        rec_view.setAdapter(featured_adapter);
    }

    private void buildElectronics() {
        items.add(new SubcategoriesItem("PC/Laptop Repair", R.drawable.ic_laptop));
        items.add(new SubcategoriesItem("Cellphone Repair", R.drawable.ic_cellphone));
        items.add(new SubcategoriesItem("Appliances Repair", R.drawable.ic_appliance));
        buildSubcategory();
    }

    private void buildSalon() {
        items.add(new SubcategoriesItem("Haircut/Salon", R.drawable.ic_haircut));
        items.add(new SubcategoriesItem("Manicure/Pedicure", R.drawable.ic_manicure));
        items.add(new SubcategoriesItem("Massage", R.drawable.ic_massage));
        buildSubcategory();
    }

    private void buildHousehold() {
        items.add(new SubcategoriesItem("Plumbing", R.drawable.ic_plumbing));
        items.add(new SubcategoriesItem("House Wiring", R.drawable.ic_wiring));
        items.add(new SubcategoriesItem("House Cleaning", R.drawable.ic_cleaning));
        items.add(new SubcategoriesItem("Laundry", R.drawable.ic_laundry));
        buildSubcategory();
    }

    private void buildMechanical() {
        items.add(new SubcategoriesItem("Automobile Repair", R.drawable.ic_automobile));
        items.add(new SubcategoriesItem("Motorcycle Repair", R.drawable.ic_motorcycle));
        items.add(new SubcategoriesItem("Bicycle Repair", R.drawable.ic_bike));
        buildSubcategory();
    }

    private List<FeaturedWorkerItem> features_item = new ArrayList<>();

    private void buildWorkers() {
        items = new ArrayList<>();
        buildSubcategory();
        rec_view.setHasFixedSize(false);
        LinearLayoutManager features_rec_manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        FeaturedWorkerAdapter featured_adapter = new FeaturedWorkerAdapter(getActivity(), features_item, rec_view, this);
        rec_view.setLayoutManager(features_rec_manager);
        rec_view.setAdapter(featured_adapter);
    }

    private void getAllWorkers(String sub_category) {
        //loading_screen.show();
        new Repositories().getAllClient(new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject client_id_obj = obj.getJSONObject("client_id");
                        JSONArray client_id_array = client_id_obj.names();

                        if (client_id_array.length() < 1) {
                            if (loading_screen != null) {
                                loading_screen.dismiss();
                            }
                        }

                        for (int a = 0; a < Objects.requireNonNull(client_id_array).length(); a++) {
                            String client_id = String.valueOf(client_id_array.get(a));
                            JSONObject client_info_obj = new JSONObject(String.valueOf(client_id_obj.get(client_id)));
                            String is_available = String.valueOf(client_info_obj.get("is_available"));
                            String profile_img_str = String.valueOf(client_info_obj.get("profile_img"));
                            String firstname = String.valueOf(client_info_obj.get("firstname"));
                            String lastname = String.valueOf(client_info_obj.get("lastname"));
                            String category = String.valueOf(client_info_obj.get("category"));
                            String client_name_str = firstname.toUpperCase() + " " + lastname.toUpperCase();

                            Bitmap profile_img_base64 = BitmapHelper.urlStrToBitmap(profile_img_str);
                            String online_status = client_info_obj.getString("online_status");
                            String rating_str = String.valueOf(client_info_obj.get("rating"));
                            String subcategory_str = String.valueOf(client_info_obj.get("subcategory"));
                            if (online_status.equals("Active") && subcategory_str.equals(sub_category) && is_available.equals("true")) {
                                select_modal.setText(sub_category);
                                features_item.add(new FeaturedWorkerItem(client_name_str, client_id, profile_img_base64, Double.parseDouble(rating_str), category));
                                buildWorkers();
                            }

                            if (a + 1 == Objects.requireNonNull(client_id_array).length()) {
                                if (features_item.isEmpty()) {
                                    Toast.makeText(getContext(), "No available workers.", Toast.LENGTH_SHORT).show();
                                }

                                if (loading_screen != null) {
                                    loading_screen.dismiss();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(getContext(), "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
                    if (loading_screen != null) {
                        loading_screen.dismiss();
                    }
                }
            }
        });
    }

    @Override
    public void onSelect(String subcategory) {
        if (subcategory.equals("Electronics") || subcategory.equals("Personal Care") ||
                subcategory.equals("Household Maintenance") || subcategory.equals("Mechanical")) {
            items = new ArrayList<>();
            buildSubcategory();
            select_modal.setText(subcategory);
            if (subcategory.equals("Electronics")) {
                buildElectronics();
            }

            if (subcategory.equals("Personal Care")) {
                buildSalon();
            }

            if (subcategory.equals("Household Maintenance")) {
                buildHousehold();
            }

            if (subcategory.equals("Mechanical")) {
                buildMechanical();
            }
        } else {
            getAllWorkers(subcategory);
        }
    }

    @Override
    public void onSelectedWorker(String worker) {
        dismiss();
    }

    private void buildAllCategories() {
        items.add(new SubcategoriesItem("Electronics", R.drawable.ic_electronics));
        items.add(new SubcategoriesItem("Personal Care", R.drawable.ic_salon));
        items.add(new SubcategoriesItem("Household Maintenance", R.drawable.ic_household));
        items.add(new SubcategoriesItem("Mechanical", R.drawable.ic_mechanical));
        buildSubcategory();
    }

    //Available Services
    private List<ServicesItems> services_item = new ArrayList<>();

    private void buildServices() {
        rec_view.setHasFixedSize(false);
        GridLayoutManager services_rec_manager = new GridLayoutManager(getContext(), 2);
        ServicesAdapter services_adapter = new ServicesAdapter(getActivity(), services_item, rec_view, this);
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

                        if (client_id_array.length() < 1) {
                            if (loading_screen != null) {
                                loading_screen.dismiss();
                            }
                        }

                        for (int a = 0; a < Objects.requireNonNull(client_id_array).length(); a++) {
                            JSONObject client_id_obj1 = new JSONObject(String.valueOf(client_id_array.get(a)));
                            JSONObject service_id_obj = client_id_obj1.getJSONObject("service_id");
                            JSONArray array = service_id_obj.toJSONArray(service_id_obj.names());

                            if (array.length() < 1) {
                                if (loading_screen != null) {
                                    loading_screen.dismiss();
                                }
                            }

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
                                            if (!result.equals("user_not_found")) {
                                                try {
                                                    JSONObject obj = new JSONObject(result);
                                                    profile_img_str = String.valueOf(obj.get("profile_img"));
                                                    String firstname = String.valueOf(obj.get("firstname"));
                                                    String lastname = String.valueOf(obj.get("lastname"));
                                                    client_name_str = firstname.toUpperCase() + " " + lastname.toUpperCase();
                                                    String is_available = String.valueOf(obj.get("is_available"));

                                                    Bitmap profile_img = BitmapHelper.urlStrToBitmap(profile_img_str);
                                                    if (service_status.equals("Active") && is_available.equals("true")) {
                                                        services_item.add(new ServicesItems(url, service_id, service_name, service_desc, service_location, service_status,
                                                                service_customer, client_id, profile_img, client_name_str, price_range));
                                                        buildServices();
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

    @Override
    public void onSelectedService(String service) {
        dismiss();
    }
}