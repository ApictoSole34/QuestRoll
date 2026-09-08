package com.murkfeatherstudio.questroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trait_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CharacterTraitEntity trait = items.get(position);
        holder.tvName.setText(trait.name);
        holder.tvDescription.setText(trait.description);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_trait_name);
            tvDescription = itemView.findViewById(R.id.tv_trait_description);
        }
    }
}