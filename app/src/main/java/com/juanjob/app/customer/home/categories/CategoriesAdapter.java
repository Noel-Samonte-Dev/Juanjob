package com.juanjob.app.customer.home.categories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.juanjob.app.R;
import com.juanjob.app.customer.home.subcategories.subcategories_sheet;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.vh> {
    private List<CategoriesItem> items;
    private Context context;
    private View v;
    private RecyclerView recyclerView;

    public CategoriesAdapter(Context context, List<CategoriesItem> items, RecyclerView recyclerView) {
        this.items = items;
        this.recyclerView = recyclerView;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoriesAdapter.vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_item, parent, false);
        CategoriesAdapter.vh rvh = new CategoriesAdapter.vh(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesAdapter.vh holder, int position) {
        CategoriesItem current_item = items.get(position);
        holder.category_img.setImageResource(current_item.getImage());
        holder.category_name.setText(current_item.getCategory());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class vh extends RecyclerView.ViewHolder {
        private final ImageView category_img;
        private final TextView category_name;
        public vh(@NonNull View itemView) {
            super(itemView);
            category_img = itemView.findViewById(R.id.category_img);
            category_name = itemView.findViewById(R.id.category_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String category = category_name.getText().toString();
                    FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                    subcategories_sheet subcategories_sheet = new subcategories_sheet(category);
                    subcategories_sheet.show(manager, "null");
                }
            });
        }
    }
}
