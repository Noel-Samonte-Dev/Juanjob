package com.juanjob.app.ads_page;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import com.juanjob.app.R;
import com.juanjob.app.customer.home.subcategories.SubcategoriesItem;

public class AdsPage extends AppCompatActivity {
    private ViewPager2 ads_slider;
    private Handler slider_handler = new Handler();
    private List<ads_item> ads_list;
    private ads_adapter ads_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ads_page);

        getAds();
    }

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
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        slider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                dot.selectTab(dot.getTabAt(position));
            }
        });
    }

    private Runnable slider_run = new Runnable() {
        @Override
        public void run() {
            if (ads_slider.getCurrentItem() + 1 == ads_list.size()) {}

            ads_slider.setCurrentItem(ads_slider.getCurrentItem() + 1);
        }
    };

    private void getAds() {
        ads_slider = findViewById(R.id.ads_slider);
        ads_slider.setClipToPadding(false);
        ads_slider.setClipChildren(false);
        ads_slider.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(0));
        transformer.addTransformer((new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float v = 1 - Math.abs(position);
                page.setScaleY(0.8f + v * 0.2f);
            }
        }));

        ads_slider.setPageTransformer(transformer);
        ads_list = new ArrayList<>();
        ads_list.add(new ads_item(R.drawable.ads_1, "JuanJob Electronics Repair"));
        ads_list.add(new ads_item(R.drawable.ads_2, "JuanJob Personal Care"));
        ads_list.add(new ads_item(R.drawable.ads_3, "JuanJob Household Maintenance"));
        ads_list.add(new ads_item(R.drawable.ads_4, "JuanJob Mechanical Maintenance1"));
        ads_adapter = new ads_adapter(ads_list, ads_slider, this);
        ads_slider.setAdapter(ads_adapter);
        TabLayout ads_indicator = findViewById(R.id.ads_indicator);
        dotIndicator(ads_indicator, ads_slider, ads_list.size());

        ads_slider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                slider_handler.removeCallbacks(slider_run);
                //slider_handler.postDelayed(slider_run, 3000);
            }
        });
    }
}
