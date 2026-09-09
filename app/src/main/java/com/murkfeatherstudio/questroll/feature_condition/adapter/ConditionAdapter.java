package com.murkfeatherstudio.questroll.feature_condition.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.databinding.ItemConditionBinding;
import com.murkfeatherstudio.questroll.feature_condition.model.CombinedCondition;

public class ConditionAdapter extends ListAdapter<CombinedCondition, ConditionAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedCondition condition);
    }

    public ConditionAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedCondition>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedCondition a, @NonNull CombinedCondition b) {
                return a.id.equals(b.id);
            }
            @Override
            public boolean areContentsTheSame(@NonNull CombinedCondition a, @NonNull CombinedCondition b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConditionBinding binding = ItemConditionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemConditionBinding binding;

        ViewHolder(ItemConditionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedCondition c, OnItemClickListener listener) {
            binding.tvName.setText(c.name);
            binding.tvDesc.setText(c.description != null ? c.description : "");
            binding.tvCustomBadge.setVisibility(c.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(c));
        }
    }
}
