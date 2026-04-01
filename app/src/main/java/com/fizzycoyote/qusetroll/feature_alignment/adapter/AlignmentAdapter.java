package com.fizzycoyote.qusetroll.feature_alignment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_alignment.model.CombinedAlignment;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvShortName, tvMorality, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_alignment_name);
            tvShortName = v.findViewById(R.id.tv_alignment_short);
            tvMorality = v.findViewById(R.id.tv_alignment_morality);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedAlignment a, OnItemClickListener listener) {
            tvName.setText(a.name);
            tvShortName.setText(a.shortName != null ? a.shortName : "");
            tvMorality.setText(a.morality != null ? a.morality : "");
            tvCustomBadge.setVisibility(a.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(a));
        }
    }
}