package com.murkfeatherstudio.questroll.feature_creature.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemCreatureBinding;
import com.murkfeatherstudio.questroll.feature_creature.model.CombinedCreature;

public class CreatureAdapter extends ListAdapter<CombinedCreature, CreatureAdapter.ViewHolder> {

    private final OnCreatureClickListener listener;

    public interface OnCreatureClickListener {
        void onCreatureClick(CombinedCreature creature);
    }

    public CreatureAdapter(OnCreatureClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CombinedCreature> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CombinedCreature>() {
                @Override
                public boolean areItemsTheSame(@NonNull CombinedCreature a, @NonNull CombinedCreature b) {
                    return a.id.equals(b.id);
                }
                @Override
                public boolean areContentsTheSame(@NonNull CombinedCreature a, @NonNull CombinedCreature b) {
                    return a.name.equals(b.name) && a.crDecimal == b.crDecimal;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCreatureBinding binding = ItemCreatureBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCreatureBinding binding;

        ViewHolder(ItemCreatureBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedCreature creature, OnCreatureClickListener listener) {
            binding.tvCreatureName.setText(creature.name);
            binding.tvCreatureCr.setText("CR " + (creature.crText != null ? creature.crText : "?"));
            binding.tvCreatureType.setText(creature.typeName != null ? creature.typeName : "");
            binding.tvCreatureSize.setText(creature.sizeName != null ? creature.sizeName : "");
            binding.tvCreatureAlignment.setText(creature.alignment != null ? creature.alignment : "");
            
            binding.tvCustomBadge.setVisibility(creature.isCustom ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCreatureClick(creature);
                }
            });
        }
    }
}