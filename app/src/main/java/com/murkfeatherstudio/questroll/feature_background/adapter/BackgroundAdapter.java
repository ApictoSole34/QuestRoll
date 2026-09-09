package com.murkfeatherstudio.questroll.feature_background.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemBackgroundBinding;
import com.murkfeatherstudio.questroll.feature_background.model.CombinedBackground;

public class BackgroundAdapter extends ListAdapter<CombinedBackground, BackgroundAdapter.ViewHolder> {

    private final OnBackgroundClickListener listener;

    public interface OnBackgroundClickListener {
        void onBackgroundClick(CombinedBackground background);
    }

    public BackgroundAdapter(OnBackgroundClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CombinedBackground> DIFF =
            new DiffUtil.ItemCallback<CombinedBackground>() {
                @Override public boolean areItemsTheSame(@NonNull CombinedBackground a, @NonNull CombinedBackground b) {
                    return a.id.equals(b.id);
                }
                @Override public boolean areContentsTheSame(@NonNull CombinedBackground a, @NonNull CombinedBackground b) {
                    return a.name.equals(b.name);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBackgroundBinding binding = ItemBackgroundBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBackgroundBinding binding;

        ViewHolder(ItemBackgroundBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedBackground b, OnBackgroundClickListener listener) {
            binding.tvBackgroundName.setText(b.name);
            if (b.documentName != null && !b.documentName.isEmpty() && !b.isCustom) {
                binding.tvBackgroundSource.setText(b.documentName);
                binding.tvBackgroundSource.setVisibility(View.VISIBLE);
            } else {
                binding.tvBackgroundSource.setVisibility(View.GONE);
            }
            binding.tvCustomBadge.setVisibility(b.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> { if (listener != null) listener.onBackgroundClick(b); });
        }
    }
}
