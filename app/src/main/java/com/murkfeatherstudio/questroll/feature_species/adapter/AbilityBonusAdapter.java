package com.murkfeatherstudio.questroll.feature_species.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemAbilityBonusBinding;
import com.murkfeatherstudio.questroll.feature_species.viewmodel.CustomSpeciesCreateViewModel;

import java.util.ArrayList;
import java.util.List;

public class AbilityBonusAdapter extends RecyclerView.Adapter<AbilityBonusAdapter.ViewHolder> {

    private List<CustomSpeciesCreateViewModel.AbilityBonus> items = new ArrayList<>();
    private final OnItemRemoveListener removeListener;

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }

    public AbilityBonusAdapter(OnItemRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void submitList(List<CustomSpeciesCreateViewModel.AbilityBonus> list) {
        this.items = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAbilityBonusBinding binding = ItemAbilityBonusBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomSpeciesCreateViewModel.AbilityBonus item = items.get(position);
        holder.binding.tvAbility.setText(item.ability);
        holder.binding.tvBonus.setText("+" + item.bonus);
        holder.binding.btnDelete.setOnClickListener(v -> removeListener.onRemove(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAbilityBonusBinding binding;

        ViewHolder(ItemAbilityBonusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}