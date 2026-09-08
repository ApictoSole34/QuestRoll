package com.murkfeatherstudio.questroll.feature_creature.creature_type.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.R;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_creature_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedCreatureType t, OnItemClickListener listener) {
            tvName.setText(t.name);
            tvDesc.setText(t.description != null ? t.description : "");
            tvCustomBadge.setVisibility(t.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(t));
        }
    }
}