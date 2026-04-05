package com.fizzycoyote.qusetroll.feature_item.item_category.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_item.item_category.model.CombinedItemCategory;

public class ItemCategoryAdapter extends ListAdapter<CombinedItemCategory, ItemCategoryAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedItemCategory category);
    }

    public ItemCategoryAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedItemCategory>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedItemCategory a, @NonNull CombinedItemCategory b) {
                return a.id.equals(b.id);
            }
            @Override
            public boolean areContentsTheSame(@NonNull CombinedItemCategory a, @NonNull CombinedItemCategory b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedItemCategory c, OnItemClickListener listener) {
            tvName.setText(c.name);
            tvDesc.setText(c.description != null ? c.description : "");
            tvCustomBadge.setVisibility(c.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(c));
        }
    }
}