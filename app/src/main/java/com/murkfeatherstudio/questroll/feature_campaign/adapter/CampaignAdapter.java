package com.murkfeatherstudio.questroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignEntity;
import com.murkfeatherstudio.questroll.databinding.ItemCampaignBinding;

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
        ItemCampaignBinding binding = ItemCampaignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CampaignEntity campaign = campaigns.get(position);
        holder.binding.tvCampaignName.setText(campaign.name);
        holder.binding.tvGameSystem.setText(campaign.gameSystem);
        if (campaign.description != null && !campaign.description.isEmpty()) {
            holder.binding.tvDescriptionPreview.setText(campaign.description);
            holder.binding.tvDescriptionPreview.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvDescriptionPreview.setVisibility(View.GONE);
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
        final ItemCampaignBinding binding;
        ViewHolder(ItemCampaignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
