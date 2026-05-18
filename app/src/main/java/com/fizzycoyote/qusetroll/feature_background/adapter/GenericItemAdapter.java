package com.fizzycoyote.qusetroll.feature_background.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GenericItemAdapter<T> extends RecyclerView.Adapter<GenericItemAdapter.ViewHolder> {
    private final List<T> items = new ArrayList<>();
    private final Consumer<T> onRemove;
    private final ItemToString<T> toStringConverter;

    public interface ItemToString<T> {
        String toString(T item);
    }

    public GenericItemAdapter(Consumer<T> onRemove, ItemToString<T> toStringConverter) {
        this.onRemove = onRemove;
        this.toStringConverter = toStringConverter;
    }

    public void setItems(List<T> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T item = items.get(position);
        holder.textView.setText(toStringConverter.toString(item));
        holder.itemView.setOnLongClickListener(v -> {
            onRemove.accept(item);
            items.remove(position);
            notifyItemRemoved(position);
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
