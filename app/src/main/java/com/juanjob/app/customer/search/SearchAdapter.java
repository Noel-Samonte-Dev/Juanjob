package com.juanjob.app.customer.search;

import static android.content.Context.MODE_PRIVATE;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import java.util.List;
import com.juanjob.app.R;
import com.juanjob.app.customer.profile.CustomerClientProfilePage;
import com.juanjob.app.customer.services.ServicePage;
import com.juanjob.app.customer.services.ServicesItems;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchAdapter_view_holder> {

    private List<ServicesItems> items;
    private Context context;
    private View v;
    private RecyclerView recyclerView;

    public SearchAdapter(Context context, List<ServicesItems> items, RecyclerView recyclerView, SelectedService selected_service) {
        this.items = items;
        this.recyclerView = recyclerView;
        this.context = context;
        this.selected_service = selected_service;
    }

    @NonNull
    @Override
    public SearchAdapter_view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        SearchAdapter_view_holder rvh = new SearchAdapter_view_holder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter_view_holder holder, int position) {
        ServicesItems current_item = items.get(position);
        holder.setImageView(current_item.getProfile_img(), null, holder.category_img);
        holder.category_name.setText(current_item.getService_name());
        holder.service_id_tv.setText(current_item.getService_id());
        holder.client_id_tv.setText(current_item.getClient_id());
        holder.profile_name.setText(current_item.getClient_name());
        holder.price_range_tv.setText(current_item.getPrice_range());
        holder.rating_tv.setText(String.format("%.1f", Double.parseDouble(current_item.getService_desc())));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SearchAdapter_view_holder extends RecyclerView.ViewHolder {
        private CircleImageView category_img;
        private TextView category_name, service_id_tv, client_id_tv, profile_name, price_range_tv, rating_tv;

        SearchAdapter_view_holder(@NonNull View item_view) {
            super(item_view);
            service_id_tv = item_view.findViewById(R.id.service_id_tv);
            client_id_tv = item_view.findViewById(R.id.client_id_tv);
            profile_name = item_view.findViewById(R.id.profile_name);
            price_range_tv = item_view.findViewById(R.id.price_range_tv);
            rating_tv = item_view.findViewById(R.id.rating_tv);

            category_img = item_view.findViewById(R.id.category_img);
            category_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotToServicePage();
                }
            });

            category_name = item_view.findViewById(R.id.category_name);
            category_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotToServicePage();
                }
            });

            item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotToServicePage();
                }
            });
        }

        void setImageView(Bitmap bitmap, ImageView img_view, CircleImageView circleImageView) {
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
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (img_view == null) {
                                circleImageView.setImageBitmap(resource);
                            } else {
                                img_view.setImageBitmap(resource);
                            }
                        }
                    });
        }

        private void goToClientProfile() {
            getService(client_id_tv.getText().toString());
            storeClientInfo(client_id_tv.getText().toString());
            FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
            manager.beginTransaction()
                    .add(R.id.customer_fragmentContainerView, new CustomerClientProfilePage())
                    .addToBackStack(null).commit();

        }

        private void gotToServicePage() {
            storeServiceInfo(service_id_tv.getText().toString(), client_id_tv.getText().toString());
            getService(service_id_tv.getText().toString());
            FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
            manager.beginTransaction()
                    .add(R.id.customer_fragmentContainerView, new ServicePage())
                    .addToBackStack(null).commit();
        }

        private void storeServiceInfo(String service_id, String client_id) {
            SharedPreferences sp = context.getSharedPreferences("Service", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("service_id", service_id);
            editor.putString("client_id", client_id);
            editor.commit();
        }

        private void storeClientInfo(String client_id) {
            SharedPreferences sp = context.getSharedPreferences("Client", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("client_id", client_id);
            editor.commit();
        }
    }

    private SelectedService selected_service;
    public void getService(String service) {
        selected_service.onSelectedSearch(service);
    }

    public interface SelectedService {
        void onSelectedSearch(String service);
    }
}
