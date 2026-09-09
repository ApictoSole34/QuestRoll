package com.murkfeatherstudio.questroll.feature_item.item_category.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.databinding.ItemItemCategoryBinding;
import com.murkfeatherstudio.questroll.feature_item.item_category.model.CombinedItemCategory;

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
        ItemItemCategoryBinding binding = ItemItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemItemCategoryBinding binding;

        ViewHolder(ItemItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedItemCategory c, OnItemClickListener listener) {
            binding.tvName.setText(c.name);
            binding.tvDesc.setText(c.description != null ? c.description : "");
            binding.tvCustomBadge.setVisibility(c.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(c));
        }
    }
}