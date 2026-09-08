package com.murkfeatherstudio.questroll.feature_species.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ability_bonus, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomSpeciesCreateViewModel.AbilityBonus item = items.get(position);
        holder.tvAbility.setText(item.ability);
        holder.tvBonus.setText("+" + item.bonus);
        holder.btnDelete.setOnClickListener(v -> removeListener.onRemove(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAbility, tvBonus;
        View btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvAbility = itemView.findViewById(R.id.tv_ability);
            tvBonus = itemView.findViewById(R.id.tv_bonus);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}