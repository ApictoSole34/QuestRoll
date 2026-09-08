package com.murkfeatherstudio.questroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StringItemAdapter extends RecyclerView.Adapter<StringItemAdapter.ViewHolder> {
    private List<String> items = new ArrayList<>();
    private final Consumer<Integer> onRemove;

    public StringItemAdapter(Consumer<Integer> onRemove) {
        this.onRemove = onRemove;
    }

    public void setItems(List<String> items) {
        this.items = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(items.get(position));
        holder.itemView.setOnLongClickListener(v -> {
            onRemove.accept(position);
            items.remove(position);
            notifyItemRemoved(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}