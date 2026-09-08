package com.murkfeatherstudio.questroll.feature_species.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
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
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_species, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSubtitle, tvCustomBadge, tvSubspeciesBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_species_name);
            tvSubtitle = v.findViewById(R.id.tv_species_subtitle);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
            tvSubspeciesBadge = v.findViewById(R.id.tv_subspecies_badge);
        }

        void bind(CombinedSpecies s, OnSpeciesClickListener listener) {
            tvName.setText(s.name);

            if (s.isSubspecies && s.subspeciesOfName != null && !s.subspeciesOfName.isEmpty()) {
                tvSubtitle.setText("Subspecies of " + s.subspeciesOfName);
                tvSubtitle.setVisibility(View.VISIBLE);
            } else if (s.documentName != null && !s.documentName.isEmpty()) {
                tvSubtitle.setText(s.documentName);
                tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                tvSubtitle.setVisibility(View.GONE);
            }

            tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
            tvSubspeciesBadge.setVisibility(s.isSubspecies ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> { if (listener != null) listener.onSpeciesClick(s); });
        }
    }
}