package com.juanjob.app.customer.home.featured_workers;

import static android.content.Context.MODE_PRIVATE;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;
import com.juanjob.app.R;
import com.juanjob.app.customer.profile.CustomerClientProfilePage;
import de.hdodenhof.circleimageview.CircleImageView;

public class FeaturedWorkerAdapter extends RecyclerView.Adapter<FeaturedWorkerAdapter.FeaturedWorkerAdapter_view_holder> {

    private List<FeaturedWorkerItem> items;
    private Context context;
    private View v;
    private RecyclerView recyclerView;

    public FeaturedWorkerAdapter(Context context, List<FeaturedWorkerItem> items, RecyclerView recyclerView, SelectedWorker selected_worker) {
        this.items = items;
        this.recyclerView = recyclerView;
        this.context = context;
        this.selected_worker = selected_worker;
    }

    @NonNull
    @Override
    public FeaturedWorkerAdapter.FeaturedWorkerAdapter_view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.featured_worker_item, parent, false);
        FeaturedWorkerAdapter.FeaturedWorkerAdapter_view_holder rvh = new FeaturedWorkerAdapter.FeaturedWorkerAdapter_view_holder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedWorkerAdapter.FeaturedWorkerAdapter_view_holder holder, int position) {
        FeaturedWorkerItem current_item = items.get(position);
        holder.setImageView(current_item.getProfile_img());
        holder.profile_name.setText(current_item.getName());
        holder.client_id_tv.setText(current_item.getClient_id());
        holder.category_tv.setText(current_item.getCategory());
        holder.rating_tv.setText(String.format("%.1f", current_item.getRating()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class FeaturedWorkerAdapter_view_holder extends RecyclerView.ViewHolder {
        private CircleImageView profile_img;
        private TextView profile_name, client_id_tv, category_tv, rating_tv;

        FeaturedWorkerAdapter_view_holder(@NonNull View item_view) {
            super(item_view);
            profile_img = item_view.findViewById(R.id.profile_img);
            profile_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToClientProfile();
                }
            });

            profile_name = item_view.findViewById(R.id.profile_name);
            profile_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToClientProfile();
                }
            });

            client_id_tv = item_view.findViewById(R.id.client_id_tv);
            category_tv = item_view.findViewById(R.id.category_tv);
            rating_tv = item_view.findViewById(R.id.rating_tv);
            item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToClientProfile();
                }
            });
        }

        void setImageView(Bitmap bitmap) {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .priority(Priority.HIGH)
                    .format(DecodeFormat.PREFER_RGB_565);

            Glide.with(context)
                    .applyDefaultRequestOptions(options)
                    .asBitmap()
                    .load(bitmap)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.default_img)
                    .into(profile_img);
        }

        private void goToClientProfile() {
            getWorker(client_id_tv.getText().toString());
            storeClientInfo(client_id_tv.getText().toString());
            FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
            manager.beginTransaction()
                    .add(R.id.customer_fragmentContainerView, new CustomerClientProfilePage())
                    .addToBackStack(null).commit();

        }

        private void storeClientInfo(String client_id) {
            SharedPreferences sp = context.getSharedPreferences("Client", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("client_id", client_id);
            editor.commit();
        }
    }

    private SelectedWorker selected_worker;
    public void getWorker(String worker) {
        selected_worker.onSelectedWorker(worker);
    }

    public interface SelectedWorker {
        void onSelectedWorker(String worker);
    }
}
