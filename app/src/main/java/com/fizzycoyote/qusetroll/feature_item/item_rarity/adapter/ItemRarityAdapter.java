package com.fizzycoyote.qusetroll.feature_item.item_rarity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_item.item_rarity.model.CombinedItemRarity;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_item_rarity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRank, tvDescription, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvRank = v.findViewById(R.id.tv_rank);
            tvDescription = v.findViewById(R.id.tv_description);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedItemRarity r, OnItemClickListener listener) {
            tvName.setText(r.name);
            tvRank.setText("Rank: " + r.rank);
            if (r.description != null && !r.description.isEmpty()) {
                tvDescription.setText(r.description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            tvCustomBadge.setVisibility(r.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(r));
        }
    }
}