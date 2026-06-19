package com.fizzycoyote.qusetroll.feature_rule.rule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.open5e.rule.RuleEntity;

public class RuleAdapter extends ListAdapter<RuleEntity, RuleAdapter.ViewHolder> {
    private final OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(RuleEntity rule); }

    public RuleAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<RuleEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull RuleEntity a, @NonNull RuleEntity b) {
                return a.key.equals(b.key);  // <-- zmiana: url -> key
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ViewHolder(View v) { super(v); tvName = v.findViewById(R.id.tv_name); }
        void bind(RuleEntity r, OnItemClickListener listener) {
            tvName.setText(r.name);
            itemView.setOnClickListener(v -> listener.onItemClick(r));
        }
    }
}
