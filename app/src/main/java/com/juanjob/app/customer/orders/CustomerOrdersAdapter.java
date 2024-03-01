package com.juanjob.app.customer.orders;

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

import java.text.SimpleDateFormat;
import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.juanjob.app.R;
import com.juanjob.app.helpers.BitmapHelper;

public class CustomerOrdersAdapter extends RecyclerView.Adapter<CustomerOrdersAdapter.CustomerOrdersAdapter_view_holder> {

    private List<CustomerOrderItem> items;
    private Context context;
    private View v;
    private RecyclerView recyclerView;

    public CustomerOrdersAdapter(Context context, List<CustomerOrderItem> items, RecyclerView recyclerView) {
        this.items = items;
        this.recyclerView = recyclerView;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomerOrdersAdapter.CustomerOrdersAdapter_view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_order_item, parent, false);
        CustomerOrdersAdapter.CustomerOrdersAdapter_view_holder rvh = new CustomerOrdersAdapter.CustomerOrdersAdapter_view_holder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerOrdersAdapter.CustomerOrdersAdapter_view_holder holder, int position) {
        CustomerOrderItem current_item = items.get(position);
        holder.category.setText(current_item.getName());
        holder.location.setText(current_item.getLocation());
        holder.price_tv.setText(current_item.getPrice());
        holder.order_id.setText(current_item.getOrder_id());
        holder.customer_id_tv.setText(current_item.getCustomer_id());
        holder.order_status.setText(current_item.getOrder_status());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss-a");
        String date = sdf.format(current_item.getDate_ordered_long());
        holder.date_ordered.setText(date);
        holder.setImageView(current_item.getOrder_image());

        if (current_item.getOrder_status().equals("Completed")) {
            holder.view_service.setBackgroundResource(R.drawable.custom_btn_green_bg);
        }

        if (current_item.getOrder_status().equals("Cancelled")) {
            holder.view_service.setBackgroundResource(R.drawable.custom_btn_bg_orange);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CustomerOrdersAdapter_view_holder extends RecyclerView.ViewHolder {
        private TextView order_id, view_service, price_tv, category, customer_id_tv, location, order_status, date_ordered;
        private ImageView order_img;

        CustomerOrdersAdapter_view_holder(@NonNull View item_view) {
            super(item_view);
            date_ordered = item_view.findViewById(R.id.date_ordered);
            order_status = item_view.findViewById(R.id.order_status);
            order_img = item_view.findViewById(R.id.order_img);
            order_id = item_view.findViewById(R.id.order_id);
            customer_id_tv = item_view.findViewById(R.id.customer_id_tv);
            view_service = item_view.findViewById(R.id.view_service);
            location = item_view.findViewById(R.id.location);
            price_tv = item_view.findViewById(R.id.price_tv);
            category = item_view.findViewById(R.id.category);
            view_service.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToOrderInfo();
                }
            });
        }

        private void goToOrderInfo() {
            storeOrderID(order_id.getText().toString());
            FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
            manager.beginTransaction()
                    .add(R.id.customer_fragmentContainerView, new CustomerOrderInfoPage())
                    .addToBackStack(null).commit();
        }

        private void storeOrderID(String order_id) {
            SharedPreferences sp = context.getSharedPreferences("Customer Orders", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("order_id", order_id);
            editor.commit();
        }

        void setImageView(String img_str) {
            Bitmap bitmap = BitmapHelper.urlStrToBitmap(img_str);
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
                    .into(order_img);
        }
    }
}
