package com.fizzycoyote.qusetroll.feature_spell.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_spell.model.CombinedSpell;

public class SpellAdapter extends ListAdapter<CombinedSpell, SpellAdapter.ViewHolder> {

    private final OnSpellClickListener listener;

    public interface OnSpellClickListener {
        void onSpellClick(CombinedSpell spell);
    }

    public SpellAdapter(OnSpellClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CombinedSpell> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CombinedSpell>() {
                @Override
                public boolean areItemsTheSame(@NonNull CombinedSpell a, @NonNull CombinedSpell b) {
                    return a.id.equals(b.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull CombinedSpell a, @NonNull CombinedSpell b) {
                    return a.name.equals(b.name) && a.level == b.level;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLevel, tvSchool, tvCastingTime;
        TextView tvRitual, tvConcentration, tvCustomBadge;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_spell_name);
            tvLevel = itemView.findViewById(R.id.tv_spell_level);
            tvSchool = itemView.findViewById(R.id.tv_spell_school);
            tvCastingTime = itemView.findViewById(R.id.tv_casting_time);
            tvRitual = itemView.findViewById(R.id.tv_ritual);
            tvConcentration = itemView.findViewById(R.id.tv_concentration);
            tvCustomBadge = itemView.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedSpell spell, OnSpellClickListener listener) {
            tvName.setText(spell.name);
            tvLevel.setText(spell.level == 0 ? "Cantrip" : "Level " + spell.level);
            tvSchool.setText(spell.schoolName != null ? spell.schoolName : "");
            tvCastingTime.setText(spell.castingTime != null ? spell.castingTime : "");
            tvRitual.setVisibility(spell.ritual ? View.VISIBLE : View.GONE);
            tvConcentration.setVisibility(spell.concentration ? View.VISIBLE : View.GONE);
            if (tvCustomBadge != null) {
                tvCustomBadge.setVisibility(spell.isCustom ? View.VISIBLE : View.GONE);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSpellClick(spell);
            });
        }
    }
}