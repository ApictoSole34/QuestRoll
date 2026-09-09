package com.murkfeatherstudio.questroll.feature_item.item_rarity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemItemRarityBinding;
import com.murkfeatherstudio.questroll.feature_item.item_rarity.model.CombinedItemRarity;

public class ItemRarityAdapter extends ListAdapter<CombinedItemRarity, ItemRarityAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedItemRarity rarity);
    }

    public ItemRarityAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedItemRarity>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedItemRarity a, @NonNull CombinedItemRarity b) {
                return a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull CombinedItemRarity a, @NonNull CombinedItemRarity b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemItemRarityBinding binding = ItemItemRarityBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemItemRarityBinding binding;

        ViewHolder(ItemItemRarityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedItemRarity r, OnItemClickListener listener) {
            binding.tvName.setText(r.name);
            binding.tvRank.setText("Rank: " + r.rank);
            if (r.description != null && !r.description.isEmpty()) {
                binding.tvDescription.setText(r.description);
                binding.tvDescription.setVisibility(View.VISIBLE);
            } else {
                binding.tvDescription.setVisibility(View.GONE);
            }
            binding.tvCustomBadge.setVisibility(r.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(r));
        }
    }
}