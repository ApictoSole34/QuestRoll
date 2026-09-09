package com.murkfeatherstudio.questroll.feature_ability.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemSkillBinding;
import com.murkfeatherstudio.questroll.feature_ability.model.CombinedSkill;

import java.util.Objects;

public class SkillAdapter extends ListAdapter<CombinedSkill, SkillAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(CombinedSkill item);
    }

    private final OnClickListener listener;

    public SkillAdapter(OnClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CombinedSkill> DIFF =
            new DiffUtil.ItemCallback<CombinedSkill>() {

                @Override
                public boolean areItemsTheSame(@NonNull CombinedSkill a, @NonNull CombinedSkill b) {
                    return a.id.equals(b.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull CombinedSkill a, @NonNull CombinedSkill b) {
                    return a.name.equals(b.name) &&
                            Objects.equals(a.abilityName, b.abilityName) &&
                            a.isCustom == b.isCustom;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSkillBinding binding = ItemSkillBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSkillBinding binding;

        ViewHolder(ItemSkillBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedSkill s, OnClickListener listener) {
            binding.tvSkillName.setText(s.name);

            binding.tvAbilityName.setText(s.abilityName != null ? s.abilityName : "");

            binding.tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(s);
                }
            });
        }
    }
}
