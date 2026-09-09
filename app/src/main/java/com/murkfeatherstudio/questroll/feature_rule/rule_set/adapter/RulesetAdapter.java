package com.murkfeatherstudio.questroll.feature_rule.rule_set.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.open5e.rule_set.RulesetEntity;
import com.murkfeatherstudio.questroll.databinding.ItemRulesetBinding;

public class RulesetAdapter extends ListAdapter<RulesetEntity, RulesetAdapter.ViewHolder> {
    private final OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(RulesetEntity ruleset); }

    public RulesetAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<RulesetEntity>() {
            @Override public boolean areItemsTheSame(@NonNull RulesetEntity a, @NonNull RulesetEntity b) { return a.key.equals(b.key); }
            @Override public boolean areContentsTheSame(@NonNull RulesetEntity a, @NonNull RulesetEntity b) { return a.name.equals(b.name); }
        });
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRulesetBinding binding = ItemRulesetBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRulesetBinding binding;
        
        ViewHolder(ItemRulesetBinding binding) { 
            super(binding.getRoot()); 
            this.binding = binding;
        }
        
        void bind(RulesetEntity r, OnItemClickListener listener) {
            binding.tvName.setText(r.name);
            binding.tvDesc.setText(r.desc != null ? r.desc : "");
            itemView.setOnClickListener(v -> listener.onItemClick(r));
        }
    }
}
