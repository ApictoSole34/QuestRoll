package com.fizzycoyote.qusetroll.feature_species.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;

import java.util.ArrayList;
import java.util.List;

public class OtherTraitAdapter extends RecyclerView.Adapter<OtherTraitAdapter.ViewHolder> {

    private List<CustomCreatureAction> items = new ArrayList<>();
    private final OnItemRemoveListener removeListener;

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }

    public OtherTraitAdapter(OnItemRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void submitList(List<CustomCreatureAction> list) {
        this.items = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_trait, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomCreatureAction item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvDesc.setText(item.desc);
        holder.btnDelete.setOnClickListener(v -> removeListener.onRemove(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        View btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_trait_name);
            tvDesc = itemView.findViewById(R.id.tv_trait_desc);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}