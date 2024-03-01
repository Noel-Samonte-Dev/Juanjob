package com.juanjob.app.ads_page;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;
import com.juanjob.app.R;
import com.juanjob.app.SelectModulePage;
import com.juanjob.app.client.ClientNavigationPage;
import com.juanjob.app.customer.CustomerNavigationPage;

public class ads_adapter extends RecyclerView.Adapter<ads_adapter.ImageSlider1_ViewHolder> {

    private List<ads_item> items;
    private Context context;
    private ViewPager2 view_pager;
    private View v;

    public ads_adapter(List<ads_item> items, ViewPager2 view_pager, Context context) {
        this.items = items;
        this.view_pager = view_pager;
        this.context = context;
    }

    public void setItems(List<ads_item> items) {
        this.items = items;
    }


    @NonNull
    @Override
    public ads_adapter.ImageSlider1_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ads_item, parent, false);
        ads_adapter.ImageSlider1_ViewHolder rvh = new ads_adapter.ImageSlider1_ViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ads_adapter.ImageSlider1_ViewHolder holder, int position) {
        ads_item current_item = items.get(position);
        holder.img_view.setImageResource(current_item.image);
        if (current_item.getMsg().contains("1")) {
            holder.get_started_btn.setVisibility(View.VISIBLE);
        }

        holder.ads_label.setText(current_item.getMsg().replace("1", ""));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ImageSlider1_ViewHolder extends RecyclerView.ViewHolder {

        private ImageView img_view;
        private Button get_started_btn;
        private TextView ads_label;

        ImageSlider1_ViewHolder(@NonNull View item_view) {
            super(item_view);
            ads_label = item_view.findViewById(R.id.ads_label);
            img_view = item_view.findViewById(R.id.img_view);
            get_started_btn = item_view.findViewById(R.id.get_started_btn);
            get_started_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
                    String login_id = sp.getString("login_id", "");
                    if (login_id.trim().isEmpty()) {
                        selectModulePage();
                    } else {
                        navigationPage();
                    }
                }
            });
        }

        public void selectModulePage() {
            Intent i = new Intent(context, SelectModulePage.class);
            context.startActivity(i);
            ((Activity)context).finish();
        }

        private void navigationPage() {
            if (isWorker()) {
                Intent i = new Intent(context, ClientNavigationPage.class);
                context.startActivity(i);
                ((Activity)context).finish();
            } else {
                Intent i = new Intent(context, CustomerNavigationPage.class);
                context.startActivity(i);
                ((Activity)context).finish();
            }
        }

        private boolean isWorker() {
            SharedPreferences sp = context.getSharedPreferences("Module Selected", MODE_PRIVATE);
            String module_selected = sp.getString("module_selected", "");
            return module_selected.equals("worker");
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            items.addAll(items);
            notifyDataSetChanged();
        }
    };
}
