package com.murkfeatherstudio.questroll.feature_species.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.murkfeatherstudio.questroll.databinding.ItemOtherTraitBinding;

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
        ItemOtherTraitBinding binding = ItemOtherTraitBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomCreatureAction item = items.get(position);
        holder.binding.tvTraitName.setText(item.name);
        holder.binding.tvTraitDesc.setText(item.desc);
        holder.binding.btnDelete.setOnClickListener(v -> removeListener.onRemove(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemOtherTraitBinding binding;

        ViewHolder(ItemOtherTraitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}