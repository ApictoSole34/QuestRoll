package com.murkfeatherstudio.questroll.feature_spell.spell_school.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemSpellSchoolBinding;
import com.murkfeatherstudio.questroll.feature_spell.spell_school.model.CombinedSpellSchool;

public class SpellSchoolAdapter extends ListAdapter<CombinedSpellSchool, SpellSchoolAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedSpellSchool school);
    }

    public SpellSchoolAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedSpellSchool>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedSpellSchool a, @NonNull CombinedSpellSchool b) {
                return a.id.equals(b.id);
            }
            @Override
            public boolean areContentsTheSame(@NonNull CombinedSpellSchool a, @NonNull CombinedSpellSchool b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSpellSchoolBinding binding = ItemSpellSchoolBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSpellSchoolBinding binding;

        ViewHolder(ItemSpellSchoolBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedSpellSchool s, OnItemClickListener listener) {
            binding.tvName.setText(s.name);
            binding.tvDesc.setText(s.description != null ? s.description : "");
            binding.tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(s));
        }
    }
}