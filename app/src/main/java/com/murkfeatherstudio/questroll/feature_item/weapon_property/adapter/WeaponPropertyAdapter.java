package com.murkfeatherstudio.questroll.feature_item.weapon_property.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemWeaponPropertyBinding;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.model.CombinedWeaponProperty;

public class WeaponPropertyAdapter extends ListAdapter<CombinedWeaponProperty, WeaponPropertyAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedWeaponProperty property);
    }

    public WeaponPropertyAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedWeaponProperty>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedWeaponProperty a, @NonNull CombinedWeaponProperty b) {
                return a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull CombinedWeaponProperty a, @NonNull CombinedWeaponProperty b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeaponPropertyBinding binding = ItemWeaponPropertyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemWeaponPropertyBinding binding;

        ViewHolder(ItemWeaponPropertyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedWeaponProperty p, OnItemClickListener listener) {
            binding.tvName.setText(p.name);
            binding.tvType.setText(p.type);
            binding.tvDesc.setText(p.desc != null ? (p.desc.length() > 100 ? p.desc.substring(0, 100) + "…" : p.desc) : "");
            binding.tvCustomBadge.setVisibility(p.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(p));
        }
    }
}