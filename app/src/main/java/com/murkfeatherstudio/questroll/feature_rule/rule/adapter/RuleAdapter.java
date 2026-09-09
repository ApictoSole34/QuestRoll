package com.murkfeatherstudio.questroll.feature_rule.rule.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemRuleBinding;
import com.murkfeatherstudio.questroll.core.models.open5e.rule.RuleEntity;

public class RuleAdapter extends ListAdapter<RuleEntity, RuleAdapter.ViewHolder> {
    private final OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(RuleEntity rule); }

    public RuleAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<RuleEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull RuleEntity a, @NonNull RuleEntity b) {
                return a.key.equals(b.key);
            }
            @Override
            public boolean areContentsTheSame(@NonNull RuleEntity a, @NonNull RuleEntity b) {
                return a.key.equals(b.key) && a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRuleBinding binding = ItemRuleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRuleBinding binding;

        ViewHolder(ItemRuleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RuleEntity r, OnItemClickListener listener) {
            binding.tvName.setText(r.name);
            itemView.setOnClickListener(v -> listener.onItemClick(r));
        }
    }
}
