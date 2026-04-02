package com.fizzycoyote.qusetroll.feature_item.weapon_property.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_item.weapon_property.model.CombinedWeaponProperty;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weapon_property, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvDesc, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvType = v.findViewById(R.id.tv_type);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedWeaponProperty p, OnItemClickListener listener) {
            tvName.setText(p.name);
            tvType.setText(p.type);
            tvDesc.setText(p.desc != null ? (p.desc.length() > 100 ? p.desc.substring(0, 100) + "…" : p.desc) : "");
            tvCustomBadge.setVisibility(p.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(p));
        }
    }
}