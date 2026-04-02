package com.fizzycoyote.qusetroll.feature_environment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_environment.model.CombinedEnvironment;

public class EnvironmentAdapter extends ListAdapter<CombinedEnvironment, EnvironmentAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedEnvironment environment);
    }

    public EnvironmentAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedEnvironment>() {
            @Override public boolean areItemsTheSame(@NonNull CombinedEnvironment a, @NonNull CombinedEnvironment b) { return a.id.equals(b.id); }
            @Override public boolean areContentsTheSame(@NonNull CombinedEnvironment a, @NonNull CombinedEnvironment b) { return a.name.equals(b.name); }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_environment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvDesc, tvCustomBadge;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvType = v.findViewById(R.id.tv_type);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }
        void bind(CombinedEnvironment e, OnItemClickListener listener) {
            tvName.setText(e.name);
            String type = "";
            if (e.aquatic) type = "Aquatic";
            else if (e.planar) type = "Planar";
            else if (e.interior) type = "Interior";
            else type = "Land";
            tvType.setText(type);
            tvDesc.setText(e.desc != null ? (e.desc.length() > 80 ? e.desc.substring(0, 80) + "…" : e.desc) : "");
            tvCustomBadge.setVisibility(e.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(e));
        }
    }
}