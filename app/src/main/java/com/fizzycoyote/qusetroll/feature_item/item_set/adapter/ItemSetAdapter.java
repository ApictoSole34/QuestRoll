package com.fizzycoyote.qusetroll.feature_item.item_set.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_item.item_set.model.CombinedItemSet;

public class ItemSetAdapter extends ListAdapter<CombinedItemSet, ItemSetAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedItemSet itemSet);
    }

    public ItemSetAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedItemSet>() {
            @Override public boolean areItemsTheSame(@NonNull CombinedItemSet a, @NonNull CombinedItemSet b) {
                return a.id.equals(b.id);
            }
            @Override public boolean areContentsTheSame(@NonNull CombinedItemSet a, @NonNull CombinedItemSet b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_item_set, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvCustomBadge;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }
        void bind(CombinedItemSet s, OnItemClickListener listener) {
            tvName.setText(s.name);
            tvDesc.setText(s.desc != null ? s.desc : "");
            tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(s));
        }
    }
}