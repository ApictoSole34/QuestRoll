package com.murkfeatherstudio.questroll.feature_item.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemItemBinding;
import com.murkfeatherstudio.questroll.feature_item.model.CombinedItem;

public class ItemAdapter extends ListAdapter<CombinedItem, ItemAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedItem item);
    }

    public ItemAdapter(OnItemClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CombinedItem> DIFF =
            new DiffUtil.ItemCallback<CombinedItem>() {
                @Override public boolean areItemsTheSame(@NonNull CombinedItem a, @NonNull CombinedItem b) {
                    return a.id.equals(b.id);
                }
                @Override public boolean areContentsTheSame(@NonNull CombinedItem a, @NonNull CombinedItem b) {
                    return a.name.equals(b.name);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemItemBinding binding = ItemItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemItemBinding binding;

        ViewHolder(ItemItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedItem item, OnItemClickListener listener) {
            binding.tvItemName.setText(item.name);
            binding.tvItemCategory.setText(item.categoryName != null ? item.categoryName : "Misc");
            binding.tvItemCategory.setVisibility(View.VISIBLE);

            if (item.rarityName != null && !item.rarityName.isEmpty()) {
                binding.tvItemRarity.setText(item.rarityName);
                binding.tvItemRarity.setVisibility(View.VISIBLE);
            } else {
                binding.tvItemRarity.setVisibility(View.GONE);
            }

            binding.tvCustomBadge.setVisibility(item.isCustom ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}
