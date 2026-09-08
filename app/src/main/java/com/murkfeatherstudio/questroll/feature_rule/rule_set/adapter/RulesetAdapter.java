package com.murkfeatherstudio.questroll.feature_rule.rule_set.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.open5e.rule_set.RulesetEntity;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruleset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        ViewHolder(View v) { super(v); tvName = v.findViewById(R.id.tv_name); tvDesc = v.findViewById(R.id.tv_desc); }
        void bind(RulesetEntity r, OnItemClickListener listener) {
            tvName.setText(r.name);
            tvDesc.setText(r.desc != null ? r.desc : "");
            itemView.setOnClickListener(v -> listener.onItemClick(r));
        }
    }
}
