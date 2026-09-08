package com.murkfeatherstudio.questroll.feature_damage_types.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_damage_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSource, tvDescription, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvSource = v.findViewById(R.id.tv_source);
            tvDescription = v.findViewById(R.id.tv_description);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedDamageType t, OnItemClickListener listener) {
            tvName.setText(t.name);
            tvSource.setText(t.source);
            tvDescription.setText(t.description != null ? t.description : "");
            tvCustomBadge.setVisibility(t.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(t);
            });
        }
    }
}