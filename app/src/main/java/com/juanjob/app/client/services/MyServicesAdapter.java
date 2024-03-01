package com.juanjob.app.client.services;

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
import com.juanjob.app.customer.services.ServicesItems;

public class MyServicesAdapter extends RecyclerView.Adapter<MyServicesAdapter.MyServicesAdapter_view_holder> {

    private List<ServicesItems> items;
    private Context context;
    private View v;
    private RecyclerView recyclerView;

    public MyServicesAdapter(Context context, List<ServicesItems> items, RecyclerView recyclerView) {
        this.items = items;
        this.recyclerView = recyclerView;
        this.context = context;
    }

    @NonNull
    @Override
    public MyServicesAdapter_view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_services_item, parent, false);
        MyServicesAdapter_view_holder rvh = new MyServicesAdapter_view_holder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyServicesAdapter_view_holder holder, int position) {
        ServicesItems current_item = items.get(position);
        holder.setImageView(current_item.getUrl());
        holder.description.setText(current_item.getService_desc());
        holder.service_id.setText(current_item.getService_id());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyServicesAdapter_view_holder extends RecyclerView.ViewHolder {
        private ImageView img_view, delete_btn;
        private TextView description, view_service, service_id;

        MyServicesAdapter_view_holder(@NonNull View item_view) {
            super(item_view);
            img_view = item_view.findViewById(R.id.img_view);
            img_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            delete_btn = item_view.findViewById(R.id.delete_btn);
            delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            service_id = item_view.findViewById(R.id.service_id);
            description = item_view.findViewById(R.id.description);
            view_service = item_view.findViewById(R.id.view_service);
            view_service.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    storeServiceId(service_id.getText().toString());
                    goToCreateServicePage();
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
                    .placeholder(R.drawable.app_logo)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            img_view.setImageBitmap(resource);
                        }
                    });
        }

        private void goToCreateServicePage() {
            FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
            manager.beginTransaction()
                    .add(R.id.client_fragmentContainerView, new ServicesPage())
                    .addToBackStack(null).commit();
        }

        private void storeServiceId(String service_id) {
            SharedPreferences sp = context.getSharedPreferences("Service", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("service_id", service_id);
            editor.commit();
        }
    }
}
