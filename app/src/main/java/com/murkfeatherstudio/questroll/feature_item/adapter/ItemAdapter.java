package com.murkfeatherstudio.questroll.feature_item.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
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
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvRarity, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_item_name);
            tvCategory = v.findViewById(R.id.tv_item_category);
            tvRarity = v.findViewById(R.id.tv_item_rarity);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedItem item, OnItemClickListener listener) {
            tvName.setText(item.name);
            tvCategory.setText(item.categoryName != null ? item.categoryName : "Misc");
            tvCategory.setVisibility(View.VISIBLE);

            if (item.rarityName != null && !item.rarityName.isEmpty()) {
                tvRarity.setText(item.rarityName);
                tvRarity.setVisibility(View.VISIBLE);
            } else {
                tvRarity.setVisibility(View.GONE);
            }

            tvCustomBadge.setVisibility(item.isCustom ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}