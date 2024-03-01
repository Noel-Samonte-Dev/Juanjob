package com.juanjob.app.banner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;

import com.juanjob.app.R;

public class HomeBannerAdapter extends RecyclerView.Adapter<HomeBannerAdapter.ImageSlider1_ViewHolder> {

    private List<HomeBannerItem> items;
    private Context context;
    private ViewPager2 view_pager;
    private View v;

    public HomeBannerAdapter(List<HomeBannerItem> items, ViewPager2 view_pager, Context context) {
        this.items = items;
        this.view_pager = view_pager;
        this.context = context;
    }

    public void setItems(List<HomeBannerItem> items) {
        this.items = items;
    }


    @NonNull
    @Override
    public ImageSlider1_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_banner_item, parent, false);
        HomeBannerAdapter.ImageSlider1_ViewHolder rvh = new HomeBannerAdapter.ImageSlider1_ViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSlider1_ViewHolder holder, int position) {
        holder.setImageView(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ImageSlider1_ViewHolder extends RecyclerView.ViewHolder {

        private ImageView img_view;

        ImageSlider1_ViewHolder(@NonNull View item_view) {
            super(item_view);
            img_view = item_view.findViewById(R.id.img_view);
        }

        void setImageView(HomeBannerItem banner_item) {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .priority(Priority.HIGH)
                    .format(DecodeFormat.PREFER_RGB_565);

            Glide.with(context)
                    .applyDefaultRequestOptions(options)
                    .load(banner_item.getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.app_logo)
                    .into(img_view);
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
