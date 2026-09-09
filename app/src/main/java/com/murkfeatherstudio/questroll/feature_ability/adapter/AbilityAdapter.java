package com.murkfeatherstudio.questroll.feature_ability.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemAbilityBinding;
import com.murkfeatherstudio.questroll.feature_ability.model.CombinedAbility;

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
        ItemAbilityBinding binding = ItemAbilityBinding.inflate(LayoutInflater.from(p.getContext()), p, false);
        return new ViewHolder(binding);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        h.bind(getItem(pos), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAbilityBinding binding;

        ViewHolder(ItemAbilityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedAbility a, OnClickListener listener) {
            binding.tvAbilityName.setText(a.name);
            binding.tvAbilityShortDesc.setText(a.shortDesc != null ? a.shortDesc : "");
            
            binding.tvCustomBadge.setVisibility(a.isCustom ? View.VISIBLE : View.GONE);
            
            itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(a); });
        }
    }
}
