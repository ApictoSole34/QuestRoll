package com.murkfeatherstudio.questroll.feature_damage_types.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemDamageTypeBinding;
import com.murkfeatherstudio.questroll.feature_damage_types.model.CombinedDamageType;

public class DamageTypeAdapter extends ListAdapter<CombinedDamageType, DamageTypeAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedDamageType type);
    }

    public DamageTypeAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedDamageType>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedDamageType a, @NonNull CombinedDamageType b) {
                return a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull CombinedDamageType a, @NonNull CombinedDamageType b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDamageTypeBinding binding = ItemDamageTypeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDamageTypeBinding binding;

        ViewHolder(ItemDamageTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedDamageType t, OnItemClickListener listener) {
            binding.tvName.setText(t.name);
            binding.tvSource.setText(t.source);
            binding.tvDescription.setText(t.description != null ? t.description : "");
            binding.tvCustomBadge.setVisibility(t.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(t);
            });
        }
    }
}