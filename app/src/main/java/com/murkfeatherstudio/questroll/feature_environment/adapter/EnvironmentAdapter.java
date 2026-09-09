package com.murkfeatherstudio.questroll.feature_environment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemEnvironmentBinding;
import com.murkfeatherstudio.questroll.feature_environment.model.CombinedEnvironment;

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
        ItemEnvironmentBinding binding = ItemEnvironmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemEnvironmentBinding binding;

        ViewHolder(ItemEnvironmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedEnvironment e, OnItemClickListener listener) {
            binding.tvName.setText(e.name);
            String type = "";
            if (e.aquatic) type = "Aquatic";
            else if (e.planar) type = "Planar";
            else if (e.interior) type = "Interior";
            else type = "Land";
            binding.tvType.setText(type);
            binding.tvDesc.setText(e.desc != null ? (e.desc.length() > 80 ? e.desc.substring(0, 80) + "…" : e.desc) : "");
            binding.tvCustomBadge.setVisibility(e.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(e));
        }
    }
}
