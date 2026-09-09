package com.murkfeatherstudio.questroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.databinding.ItemTraitDetailsBinding;

import java.util.ArrayList;
import java.util.List;

public class BackgroundRaceTraitAdapter extends RecyclerView.Adapter<BackgroundRaceTraitAdapter.ViewHolder> {
    private List<CharacterTraitEntity> items = new ArrayList<>();

    public void setItems(List<CharacterTraitEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTraitDetailsBinding binding = ItemTraitDetailsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CharacterTraitEntity trait = items.get(position);
        holder.binding.tvTraitName.setText(trait.name);
        holder.binding.tvTraitDescription.setText(trait.description);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTraitDetailsBinding binding;
        ViewHolder(ItemTraitDetailsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}