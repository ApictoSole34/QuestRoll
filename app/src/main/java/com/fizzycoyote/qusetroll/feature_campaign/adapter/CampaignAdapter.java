package com.fizzycoyote.qusetroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.campaign.CampaignEntity;

import java.util.ArrayList;
import java.util.List;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.ViewHolder> {

    private List<CampaignEntity> campaigns = new ArrayList<>();
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(CampaignEntity campaign);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(CampaignEntity campaign);
    }

    public CampaignAdapter(OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setCampaigns(List<CampaignEntity> campaigns) {
        this.campaigns = campaigns;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campaign, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CampaignEntity campaign = campaigns.get(position);
        holder.tvName.setText(campaign.name);
        holder.tvGameSystem.setText(campaign.gameSystem);
        if (campaign.description != null && !campaign.description.isEmpty()) {
            holder.tvDescription.setText(campaign.description);
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(campaign));
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(campaign);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return campaigns.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGameSystem, tvDescription;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_campaign_name);
            tvGameSystem = itemView.findViewById(R.id.tv_game_system);
            tvDescription = itemView.findViewById(R.id.tv_description_preview);
        }
    }
}
