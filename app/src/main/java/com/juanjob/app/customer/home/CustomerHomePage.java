package com.juanjob.app.customer.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.juanjob.app.R;
import com.juanjob.app.banner.HomeBannerAdapter;
import com.juanjob.app.banner.HomeBannerItem;
import com.juanjob.app.customer.home.categories.CategoriesAdapter;
import com.juanjob.app.customer.home.categories.CategoriesItem;
import com.juanjob.app.customer.home.featured_workers.FeaturedWorkerAdapter;
import com.juanjob.app.customer.home.featured_workers.FeaturedWorkerItem;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.customer.services.ServicesAdapter;
import com.juanjob.app.customer.services.ServicesItems;

public class CustomerHomePage extends Fragment implements FeaturedWorkerAdapter.SelectedWorker, ServicesAdapter.SelectedService {
    private ViewPager2 banner_view_pager;
    private Handler slider_handler = new Handler();
    private List<HomeBannerItem> banner_item = new ArrayList<>();
    private HomeBannerAdapter banner_adapter;
    private TabLayout banner_dot_indicator;
    private boolean is_worker;
    private AlertDialog loading_screen;
    private SwipeRefreshLayout swipe_refresh;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customer_home_page, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading_screen = new LoadingPage().loadingGif(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences("Module Selected", MODE_PRIVATE);
        String module_selected = sp.getString("module_selected", "");
        is_worker = module_selected.equals("worker");

        //Banner
        banner_dot_indicator = v.findViewById(R.id.dot_indicator);
        banner_view_pager = v.findViewById(R.id.view_pager);
        banner_view_pager.setClipToPadding(false);
        banner_view_pager.setClipChildren(false);
        banner_view_pager.setOffscreenPageLimit(3);
        banner_view_pager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(20));
        transformer.addTransformer((new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float v = 1 - Math.abs(position);
                page.setScaleY(0.8f + v * 0.2f);
            }
        }));

        banner_view_pager.setPageTransformer(transformer);
        banner_view_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                slider_handler.removeCallbacks(slider_run);
                slider_handler.postDelayed(slider_run, 3000);

            }
        });

        banner_adapter = new HomeBannerAdapter(banner_item, banner_view_pager, getContext());
        banner_view_pager.setAdapter(banner_adapter);

        banner_item.add(new HomeBannerItem(R.drawable.banner_1));
        banner_item.add(new HomeBannerItem(R.drawable.banner_2));
        banner_item.add(new HomeBannerItem(R.drawable.benner_3));
        banner_item.add(new HomeBannerItem(R.drawable.banner_4));

        dotIndicator(banner_dot_indicator, banner_view_pager, banner_item.size());

        Button search_bar_box = v.findViewById(R.id.search_bar_box);
        search_bar_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Under Development", Toast.LENGTH_SHORT).show();
            }
        });

        //Featured
        features_rec_view = v.findViewById(R.id.features_rv);
        buildFeatured();

        TextView view_all_features = v.findViewById(R.id.view_all_features);
        view_all_features.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Under Development", Toast.LENGTH_SHORT).show();
            }
        });

        //Categories
        categories_rec_view = v.findViewById(R.id.categories_rv);

        //Services
        TextView view_all_services = v.findViewById(R.id.view_all_services);
        view_all_services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Under Development", Toast.LENGTH_SHORT).show();
            }
        });

        services_rec_view = v.findViewById(R.id.services_rv);
        buildServices();
        buildCategories();
        getActiveServices();

        swipe_refresh = v.findViewById(R.id.swipe_refresh);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                features_item = new ArrayList<>();
                buildFeatured();
                services_item = new ArrayList<>();
                buildServices();
                getActiveServices();
                swipe_refresh.setRefreshing(false);
            }
        });

    }

    private Runnable slider_run = new Runnable() {
        @Override
        public void run() {
            int current_pos = banner_view_pager.getCurrentItem();
            if (current_pos == banner_item.size() - 1) {
                current_pos = 0;
                banner_view_pager.setCurrentItem(current_pos);
            } else {
                banner_view_pager.setCurrentItem(current_pos + 1);
            }
        }
    };

    private void dotIndicator(TabLayout dot, ViewPager2 slider, int item_size) {
        for (int a = 0; a < item_size; a++) {
            dot.addTab(dot.newTab().setText(""));
        }

        dot.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                slider.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

        });

        slider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                dot.selectTab(dot.getTabAt(position));
                if (position >= item_size) {
                    dot.selectTab(dot.getTabAt(0));
                } else {
                    dot.selectTab(dot.getTabAt(position));
                }
            }
        });
    }

    //Featured Workers
    private RecyclerView features_rec_view;
    private List<FeaturedWorkerItem> features_item = new ArrayList<>();
    private void buildFeatured() {
        features_rec_view.setHasFixedSize(false);
        LinearLayoutManager features_rec_manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        FeaturedWorkerAdapter featured_adapter = new FeaturedWorkerAdapter(getActivity(), features_item, features_rec_view, this);
        features_rec_view.setLayoutManager(features_rec_manager);
        features_rec_view.setAdapter(featured_adapter);
    }

    //Categories
    private RecyclerView categories_rec_view;
    private final List<CategoriesItem> categories_item = new ArrayList<>();
    private void buildCategories() {
        categories_item.add(new CategoriesItem("Electronics", R.drawable.ic_electronics));
        categories_item.add(new CategoriesItem("Personal Care", R.drawable.ic_salon));
        categories_item.add(new CategoriesItem("Household Maintenance", R.drawable.ic_household));
        categories_item.add(new CategoriesItem("Mechanical", R.drawable.ic_mechanical));

        categories_rec_view.setHasFixedSize(false);
        LinearLayoutManager categories_rec_manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        CategoriesAdapter categories_adapter = new CategoriesAdapter(getActivity(), categories_item, categories_rec_view);
        categories_rec_view.setLayoutManager(categories_rec_manager);
        categories_rec_view.setAdapter(categories_adapter);
    }

    //Available Services
    private RecyclerView services_rec_view;
    private List<ServicesItems> services_item = new ArrayList<>();
    private void buildServices() {
        services_rec_view.setHasFixedSize(false);
        GridLayoutManager services_rec_manager = new GridLayoutManager(getContext(), 2);
        ServicesAdapter services_adapter = new ServicesAdapter(getActivity(), services_item, services_rec_view, this);
        services_rec_view.setLayoutManager(services_rec_manager);
        services_rec_view.setAdapter(services_adapter);
    }
    private void getActiveServices() {
        loading_screen.show();
        features_item = new ArrayList<>();
        buildFeatured();
        services_item = new ArrayList<>();
        buildServices();
        new Repositories().getAllServices(new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("services_not_found")) {
                    try {
                        JSONObject obj= new JSONObject(result);
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

                                            Bitmap profile_img = BitmapHelper.urlStrToBitmap(profile_img_str);
                                            if (service_status.equals("Active")) {
                                                services_item.add(new ServicesItems(url, service_id, service_name, service_desc, service_location, service_status,
                                                        service_customer, client_id, profile_img, client_name_str, price_range));
                                                buildServices();
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

                    services_item = new ArrayList<>();
                    features_item = new ArrayList<>();
                    buildServices();
                    buildFeatured();
                }
            }
        });
    }

    @Override
    public void onSelectedWorker(String worker) {

    }

    @Override
    public void onSelectedService(String service) {

    }
}

class RatingComparator implements Comparator<FeaturedWorkerItem> {

    @Override
    public int compare(FeaturedWorkerItem a, FeaturedWorkerItem b) {
        return (int) (b.getRating() - a.getRating());
    }
}

