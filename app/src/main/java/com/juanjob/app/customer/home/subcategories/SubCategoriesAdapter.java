package com.juanjob.app.customer.home.subcategories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.juanjob.app.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class SubCategoriesAdapter extends RecyclerView.Adapter<SubCategoriesAdapter.SubCategoriesAdapter_view_holder> {

    private List<SubcategoriesItem> items;
    private Context context;
    private View v;
    private RecyclerView recyclerView;

    public SubCategoriesAdapter(Context context, List<SubcategoriesItem> items, RecyclerView recyclerView, SelectedSubcategory selected_subcategory) {
        this.items = items;
        this.recyclerView = recyclerView;
        this.context = context;
        this.selected_subcategory = selected_subcategory;
    }

    @NonNull
    @Override
    public SubCategoriesAdapter.SubCategoriesAdapter_view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subcategories_item, parent, false);
        SubCategoriesAdapter.SubCategoriesAdapter_view_holder rvh = new SubCategoriesAdapter.SubCategoriesAdapter_view_holder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull SubCategoriesAdapter.SubCategoriesAdapter_view_holder holder, int position) {
        SubcategoriesItem current_item = items.get(position);
        holder.subcategory_img.setImageResource(current_item.getImage());
        holder.subcategory_name.setText(current_item.getCategory());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SubCategoriesAdapter_view_holder extends RecyclerView.ViewHolder {
        private CircleImageView subcategory_img;
        private TextView subcategory_name;

        SubCategoriesAdapter_view_holder(@NonNull View item_view) {
            super(item_view);
            subcategory_img = item_view.findViewById(R.id.category_img);
            subcategory_name = item_view.findViewById(R.id.category_name);
            item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSubcategory(subcategory_name.getText().toString());
                }
            });
        }
    }

    private SelectedSubcategory selected_subcategory;
    public void getSubcategory(String subcategory) {
        selected_subcategory.onSelect(subcategory);
    }

    public interface SelectedSubcategory {
        void onSelect(String subcategory);
    }


}
