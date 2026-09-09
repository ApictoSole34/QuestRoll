package com.murkfeatherstudio.questroll.feature_spell.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomCastingOption;
import com.murkfeatherstudio.questroll.databinding.ItemCastingOptionBinding;

import java.util.ArrayList;
import java.util.List;

public class CastingOptionAdapter extends RecyclerView.Adapter<CastingOptionAdapter.ViewHolder> {

    private List<CustomCastingOption> options = new ArrayList<>();
    private final OnDeleteListener listener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public CastingOptionAdapter(OnDeleteListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CustomCastingOption> newOptions) {
        options = newOptions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCastingOptionBinding binding = ItemCastingOptionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(options.get(position), position, listener);
    }

    @Override
    public int getItemCount() { return options.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCastingOptionBinding binding;

        ViewHolder(ItemCastingOptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CustomCastingOption option, int position, OnDeleteListener listener) {
            binding.tvOptionType.setText(option.type);

            StringBuilder details = new StringBuilder();
            if (option.damageRoll != null && !option.damageRoll.isEmpty()) {
                details.append("Damage: ").append(option.damageRoll);
            }
            if (option.range != null && !option.range.isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("Range: ").append(option.range);
            }
            if (option.duration != null && !option.duration.isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("Duration: ").append(option.duration);
            }
            if (option.desc != null && !option.desc.isEmpty()) {
                if (details.length() > 0) details.append("\n");
                details.append(option.desc);
            }

            binding.tvOptionDetails.setText(details.toString());
            binding.tvOptionDetails.setVisibility(details.length() > 0 ? View.VISIBLE : View.GONE);

            binding.btnDeleteOption.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(position);
            });
        }
    }
}
