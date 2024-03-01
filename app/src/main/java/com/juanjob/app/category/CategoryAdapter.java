package com.juanjob.app.category;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;
import com.juanjob.app.R;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.FeaturedAdapter_view_holder> {

    private List<CategoryItems> items;
    private Context context;
    private View v;
    private RecyclerView recyclerView;

    public CategoryAdapter(Context context, List<CategoryItems> items, RecyclerView recyclerView) {
        this.items = items;
        this.recyclerView = recyclerView;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryAdapter.FeaturedAdapter_view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.featured_worker_item, parent, false);
        CategoryAdapter.FeaturedAdapter_view_holder rvh = new CategoryAdapter.FeaturedAdapter_view_holder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.FeaturedAdapter_view_holder holder, int position) {
        CategoryItems current_item = items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class FeaturedAdapter_view_holder extends RecyclerView.ViewHolder {
        private ImageView img_view;
        private TextView label;

        FeaturedAdapter_view_holder(@NonNull View item_view) {
            super(item_view);
        }

        void setImageView(CategoryItems featured_items) {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .priority(Priority.HIGH)
                    .format(DecodeFormat.PREFER_RGB_565);

            Glide.with(context)
                    .applyDefaultRequestOptions(options)
                    .load(featured_items.getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.app_logo)
                    .into(img_view);
        }
    }
}
