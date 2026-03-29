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
import com.fizzycoyote.qusetroll.feature_ability.model.CombinedAbility;

public class AbilityAdapter extends ListAdapter<CombinedAbility, AbilityAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(CombinedAbility item); }

    private final OnClickListener listener;

    public AbilityAdapter(OnClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CombinedAbility> DIFF =
            new DiffUtil.ItemCallback<CombinedAbility>() {
                @Override
                public boolean areItemsTheSame(@NonNull CombinedAbility a, @NonNull CombinedAbility b) {
                    return a.id.equals(b.id);
                }
                @Override
                public boolean areContentsTheSame(@NonNull CombinedAbility a, @NonNull CombinedAbility b) {
                    return a.name.equals(b.name);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_ability, p, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        h.bind(getItem(pos), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvShortDesc, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName        = v.findViewById(R.id.tv_ability_name);
            tvShortDesc   = v.findViewById(R.id.tv_ability_short_desc);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedAbility a, OnClickListener listener) {
            tvName.setText(a.name);
            tvShortDesc.setText(a.shortDesc != null ? a.shortDesc : "");
            if (tvCustomBadge != null)
                tvCustomBadge.setVisibility(a.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(a); });
        }
    }
}