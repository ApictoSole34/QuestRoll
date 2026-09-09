package com.murkfeatherstudio.questroll.feature_species.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemSpeciesBinding;
import com.murkfeatherstudio.questroll.feature_species.model.CombinedSpecies;

public class SpeciesAdapter extends ListAdapter<CombinedSpecies, SpeciesAdapter.ViewHolder> {

    private final OnSpeciesClickListener listener;

    public interface OnSpeciesClickListener {
        void onSpeciesClick(CombinedSpecies species);
    }

    public SpeciesAdapter(OnSpeciesClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CombinedSpecies> DIFF =
            new DiffUtil.ItemCallback<CombinedSpecies>() {
                @Override public boolean areItemsTheSame(@NonNull CombinedSpecies a, @NonNull CombinedSpecies b) {
                    return a.id.equals(b.id);
                }
                @Override public boolean areContentsTheSame(@NonNull CombinedSpecies a, @NonNull CombinedSpecies b) {
                    return a.name.equals(b.name) && a.isSubspecies == b.isSubspecies;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSpeciesBinding binding = ItemSpeciesBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSpeciesBinding binding;

        ViewHolder(ItemSpeciesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedSpecies s, OnSpeciesClickListener listener) {
            binding.tvSpeciesName.setText(s.name);

            if (s.isSubspecies && s.subspeciesOfName != null && !s.subspeciesOfName.isEmpty()) {
                binding.tvSpeciesSubtitle.setText("Subspecies of " + s.subspeciesOfName);
                binding.tvSpeciesSubtitle.setVisibility(View.VISIBLE);
            } else if (s.documentName != null && !s.documentName.isEmpty()) {
                binding.tvSpeciesSubtitle.setText(s.documentName);
                binding.tvSpeciesSubtitle.setVisibility(View.VISIBLE);
            } else {
                binding.tvSpeciesSubtitle.setVisibility(View.GONE);
            }

            binding.tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
            binding.tvSubspeciesBadge.setVisibility(s.isSubspecies ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> { if (listener != null) listener.onSpeciesClick(s); });
        }
    }
}
