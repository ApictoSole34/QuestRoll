package com.murkfeatherstudio.questroll.feature_service.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemServiceBinding;
import com.murkfeatherstudio.questroll.feature_service.model.CombinedService;

public class ServiceAdapter extends ListAdapter<CombinedService, ServiceAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedService service);
    }

    public ServiceAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedService>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedService a, @NonNull CombinedService b) {
                return a.id.equals(b.id);
            }
            @Override
            public boolean areContentsTheSame(@NonNull CombinedService a, @NonNull CombinedService b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceBinding binding = ItemServiceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemServiceBinding binding;

        ViewHolder(ItemServiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedService s, OnItemClickListener listener) {
            binding.tvName.setText(s.name);
            binding.tvCostDetail.setText(s.cost + " gp / " + s.detail);
            binding.tvDesc.setText(s.desc != null ? (s.desc.length() > 100 ? s.desc.substring(0, 100) + "…" : s.desc) : "");
            binding.tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(s));
        }
    }
}