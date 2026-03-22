package com.fizzycoyote.qusetroll.feature_background.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_background.model.CombinedBackground;

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
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_background, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSource, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_background_name);
            tvSource = v.findViewById(R.id.tv_background_source);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedBackground b, OnBackgroundClickListener listener) {
            tvName.setText(b.name);
            if (b.documentName != null && !b.documentName.isEmpty() && !b.isCustom) {
                tvSource.setText(b.documentName);
                tvSource.setVisibility(View.VISIBLE);
            } else {
                tvSource.setVisibility(View.GONE);
            }
            tvCustomBadge.setVisibility(b.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> { if (listener != null) listener.onBackgroundClick(b); });
        }
    }
}