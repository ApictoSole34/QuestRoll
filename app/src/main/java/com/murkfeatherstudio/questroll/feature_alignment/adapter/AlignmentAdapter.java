package com.murkfeatherstudio.questroll.feature_alignment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemAlignmentBinding;
import com.murkfeatherstudio.questroll.feature_alignment.model.CombinedAlignment;

public class AlignmentAdapter extends ListAdapter<CombinedAlignment, AlignmentAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedAlignment alignment);
    }

    public AlignmentAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedAlignment>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedAlignment a, @NonNull CombinedAlignment b) {
                return a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull CombinedAlignment a, @NonNull CombinedAlignment b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAlignmentBinding binding = ItemAlignmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAlignmentBinding binding;

        ViewHolder(ItemAlignmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedAlignment a, OnItemClickListener listener) {
            binding.tvAlignmentName.setText(a.name);
            binding.tvAlignmentShort.setText(a.shortName != null ? a.shortName : "");
            binding.tvAlignmentMorality.setText(a.morality != null ? a.morality : "");
            binding.tvCustomBadge.setVisibility(a.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(a);
            });
        }
    }
}
