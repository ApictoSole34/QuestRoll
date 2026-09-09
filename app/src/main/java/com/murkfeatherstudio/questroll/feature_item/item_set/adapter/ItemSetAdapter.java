package com.murkfeatherstudio.questroll.feature_item.item_set.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemItemSetBinding;
import com.murkfeatherstudio.questroll.feature_item.item_set.model.CombinedItemSet;

public class ItemSetAdapter extends ListAdapter<CombinedItemSet, ItemSetAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedItemSet itemSet);
    }

    public ItemSetAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedItemSet>() {
            @Override public boolean areItemsTheSame(@NonNull CombinedItemSet a, @NonNull CombinedItemSet b) {
                return a.id.equals(b.id);
            }
            @Override public boolean areContentsTheSame(@NonNull CombinedItemSet a, @NonNull CombinedItemSet b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemItemSetBinding binding = ItemItemSetBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemItemSetBinding binding;

        ViewHolder(ItemItemSetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedItemSet s, OnItemClickListener listener) {
            binding.tvName.setText(s.name);
            binding.tvDesc.setText(s.desc != null ? s.desc : "");
            binding.tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(s));
        }
    }
}