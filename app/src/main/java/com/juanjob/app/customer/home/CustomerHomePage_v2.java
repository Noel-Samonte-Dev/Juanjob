package com.juanjob.app.customer.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.juanjob.app.R;
import com.juanjob.app.customer.search.SearchModalSheet;
import com.juanjob.app.customer.home.featured_workers.FeaturedWorkerAdapter;
import com.juanjob.app.customer.home.featured_workers.FeaturedWorkerItem;
import com.juanjob.app.customer.home.subcategories.subcategories_sheet;
import com.juanjob.app.customer.profile.CustomerProfilePage;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerHomePage_v2 extends Fragment implements FeaturedWorkerAdapter.SelectedWorker {
    private AlertDialog loading_screen;
    private SwipeRefreshLayout swipe_refresh;
    private CircleImageView profile_img;
    private TextView text_1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customer_home_page_v2, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading_screen = new LoadingPage().loadingGif(getActivity());

        //Featured
        features_rec_view = v.findViewById(R.id.features_rv);
        buildFeatured();
        getActiveServices();

        text_1 = v.findViewById(R.id.text_1);
        profile_img = v.findViewById(R.id.profile_img);
        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfilePage();
            }
        });

        swipe_refresh = v.findViewById(R.id.swipe_refresh);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setRefreshing(false);
                getCustomerInfo();
                features_item = new ArrayList<>();
                buildFeatured();
                getActiveServices();
            }
        });

        getCustomerInfo();

        Button categories_btn = v.findViewById(R.id.categories_btn);
        categories_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subcategories_sheet subcategories_sheet = new subcategories_sheet("Categories");
                subcategories_sheet.show(getParentFragmentManager(), "null");
            }
        });

        Button services_btn = v.findViewById(R.id.services_btn);
        services_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subcategories_sheet subcategories_sheet = new subcategories_sheet("Services");
                subcategories_sheet.show(getParentFragmentManager(), "null");
            }
        });

        Button search_bar_box = v.findViewById(R.id.search_bar_box);
        search_bar_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchModalSheet searchModalSheet = new SearchModalSheet();
                searchModalSheet.show(getParentFragmentManager(),"null");
            }
        });

    }

    //Featured Workers
    private RecyclerView features_rec_view;
    private List<FeaturedWorkerItem> features_item = new ArrayList<>();
    private void buildFeatured() {
        features_rec_view.setHasFixedSize(false);
        GridLayoutManager features_rec_manager = new GridLayoutManager(getContext(), 2);
        FeaturedWorkerAdapter featured_adapter = new FeaturedWorkerAdapter(getActivity(), features_item, features_rec_view, this);
        features_rec_view.setLayoutManager(features_rec_manager);
        features_rec_view.setAdapter(featured_adapter);
    }
    private void getActiveServices() {
        loading_screen.show();
        features_item = new ArrayList<>();
        features_item.clear();
        buildFeatured();
        refreshActiveServices();
    }

    private void refreshActiveServices() {
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                features_item = new ArrayList<>();
                features_item.clear();
                buildFeatured();
                if (snapshot.getValue() != null) {
                    Gson gson = new Gson();
                    Type typeObject = new TypeToken<HashMap>() {}.getType();
                    String result = gson.toJson(snapshot.getValue(), typeObject);
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
                                String service_status = String.valueOf(sku_obj.get("status"));
                                String client_id = String.valueOf(sku_obj.get("client_id"));

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
                                                    String is_available = String.valueOf(obj.get("is_available"));
                                                    profile_img_str = String.valueOf(obj.get("profile_img"));
                                                    String firstname = String.valueOf(obj.get("firstname"));
                                                    String lastname = String.valueOf(obj.get("lastname"));
                                                    String category = String.valueOf(obj.get("category"));
                                                    client_name_str = firstname.toUpperCase() + " " + lastname.toUpperCase();

                                                    Bitmap profile_img_base64 = BitmapHelper.urlStrToBitmap(profile_img_str);
                                                    String online_status = obj.getString("online_status");
                                                    String rating_str = String.valueOf(obj.get("rating"));
                                                    if (online_status.equals("Active") && is_available.equals("true") && Double.parseDouble(rating_str) > 3.99) {
                                                        features_item.add(new FeaturedWorkerItem(client_name_str, client_id, profile_img_base64, Double.parseDouble(rating_str), category));
                                                    }

                                                } catch (JSONException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }

                                            if (finalI + 1 == Objects.requireNonNull(client_id_array).length()) {
                                                features_item.sort(new RatingComparator());
                                                if (features_item.size() > 5) {
                                                    features_item.subList(5, features_item.size()).clear();
                                                }
                                                buildFeatured();
                                                loading_screen.dismiss();
                                            }
                                        }
                                    });
                                } else {
                                    if (client_id_array.length() == a + 1) {
                                        features_item.sort(new RatingComparator());
                                        if (features_item.size() > 5) {
                                            features_item.subList(5, features_item.size()).clear();
                                        }
                                        buildFeatured();
                                        loading_screen.dismiss();
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    loading_screen.dismiss();
                    features_item = new ArrayList<>();
                    buildFeatured();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        DatabaseReference database = FirebaseDatabase.getInstance(Repositories.firebase_db_url).getReference();
        database.child("service_table")
                .addValueEventListener(vel);
    }

    @Override
    public void onSelectedWorker(String worker) {

    }

    private void goToProfilePage() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.customer_fragmentContainerView, new CustomerProfilePage(), "customer_profile_page").commit();
    }

    private void getCustomerInfo() {
        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        String customer_id = sp.getString("login_id", "");
        new Repositories().getCustomer(customer_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String profile_img_str = String.valueOf(obj.get("profile_img"));
                        setImageView(profile_img_str);
                        String firstname = String.valueOf(obj.get("firstname"));
                        String lastname = String.valueOf(obj.get("lastname"));
                        String full_name = firstname + " " + lastname;
                        String text = "Welcome back, " + full_name;
                        text_1.setText(text);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
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
}

