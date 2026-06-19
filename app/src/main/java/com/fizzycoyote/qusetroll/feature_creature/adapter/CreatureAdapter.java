package com.fizzycoyote.qusetroll.feature_creature.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_creature.model.CombinedCreature;

public class CreatureAdapter extends ListAdapter<CombinedCreature, CreatureAdapter.ViewHolder> {

    private final OnCreatureClickListener listener;

    public interface OnCreatureClickListener {
        void onCreatureClick(CombinedCreature creature);
    }

    public CreatureAdapter(OnCreatureClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CombinedCreature> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CombinedCreature>() {
                @Override
                public boolean areItemsTheSame(@NonNull CombinedCreature a, @NonNull CombinedCreature b) {
                    return a.id.equals(b.id);
                }
                @Override
                public boolean areContentsTheSame(@NonNull CombinedCreature a, @NonNull CombinedCreature b) {
                    return a.name.equals(b.name) && a.crDecimal == b.crDecimal;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_creature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCr, tvType, tvSize, tvAlignment, tvCustomBadge;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_creature_name);
            tvCr = itemView.findViewById(R.id.tv_creature_cr);
            tvType = itemView.findViewById(R.id.tv_creature_type);
            tvSize = itemView.findViewById(R.id.tv_creature_size);
            tvAlignment = itemView.findViewById(R.id.tv_creature_alignment);
            tvCustomBadge = itemView.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedCreature creature, OnCreatureClickListener listener) {
            tvName.setText(creature.name);
            tvCr.setText("CR " + (creature.crText != null ? creature.crText : "?"));
            tvType.setText(creature.typeName != null ? creature.typeName : "");
            tvSize.setText(creature.sizeName != null ? creature.sizeName : "");
            tvAlignment.setText(creature.alignment != null ? creature.alignment : "");
            if (tvCustomBadge != null)
                tvCustomBadge.setVisibility(creature.isCustom ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCreatureClick(creature);
                }
            });
        }
    }
}