package com.fizzycoyote.qusetroll.feature_ability.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_ability.model.CombinedSkill;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_skill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CombinedSkill item = getItem(position);
        holder.bind(item, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvAbility;
        private final TextView tvCustomBadge;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_skill_name);
            tvAbility = itemView.findViewById(R.id.tv_ability_name);
            tvCustomBadge = itemView.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedSkill s, OnClickListener listener) {
            tvName.setText(s.name);

            if (tvAbility != null) {
                tvAbility.setText(s.abilityName != null ? s.abilityName : "");
            }

            if (tvCustomBadge != null) {
                tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(s);
                }
            });
        }
    }
}