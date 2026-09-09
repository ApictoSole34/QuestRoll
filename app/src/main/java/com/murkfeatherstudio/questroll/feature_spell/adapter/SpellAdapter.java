package com.murkfeatherstudio.questroll.feature_spell.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemSpellBinding;
import com.murkfeatherstudio.questroll.feature_spell.model.CombinedSpell;

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
        ItemSpellBinding binding = ItemSpellBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSpellBinding binding;

        ViewHolder(ItemSpellBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedSpell spell, OnSpellClickListener listener) {
            binding.tvSpellName.setText(spell.name);
            binding.tvSpellLevel.setText(spell.level == 0 ? "Cantrip" : "Level " + spell.level);
            binding.tvSpellSchool.setText(spell.schoolName != null ? spell.schoolName : "");
            binding.tvCastingTime.setText(spell.castingTime != null ? spell.castingTime : "");
            binding.tvRitual.setVisibility(spell.ritual ? View.VISIBLE : View.GONE);
            binding.tvConcentration.setVisibility(spell.concentration ? View.VISIBLE : View.GONE);
            binding.tvCustomBadge.setVisibility(spell.isCustom ? View.VISIBLE : View.GONE);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSpellClick(spell);
            });
        }
    }
}
