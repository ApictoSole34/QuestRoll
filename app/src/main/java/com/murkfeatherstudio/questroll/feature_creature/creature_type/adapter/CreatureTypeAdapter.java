package com.murkfeatherstudio.questroll.feature_creature.creature_type.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemCreatureTypeBinding;
import com.murkfeatherstudio.questroll.feature_creature.creature_type.model.CombinedCreatureType;

public class CreatureTypeAdapter extends ListAdapter<CombinedCreatureType, CreatureTypeAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedCreatureType type);
    }

    public CreatureTypeAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedCreatureType>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedCreatureType a, @NonNull CombinedCreatureType b) {
                return a.id.equals(b.id);
            }
            @Override
            public boolean areContentsTheSame(@NonNull CombinedCreatureType a, @NonNull CombinedCreatureType b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCreatureTypeBinding binding = ItemCreatureTypeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCreatureTypeBinding binding;

        ViewHolder(ItemCreatureTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedCreatureType t, OnItemClickListener listener) {
            binding.tvName.setText(t.name);
            binding.tvDesc.setText(t.description != null ? t.description : "");
            binding.tvCustomBadge.setVisibility(t.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(t));
        }
    }
}