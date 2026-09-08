package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignEntity;
import com.murkfeatherstudio.questroll.feature_campaign.ui.CampaignDetailActivity;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.QuickCampaignViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple drawer fragment that displays a list of campaigns for quick navigation.
 * Updated to use themed buttons for a better look.
 */
public class QuickCampaignFragment extends Fragment {

    private QuickCampaignViewModel viewModel;
    private CampaignAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quick_campaign_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QuickCampaignViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_quick_campaigns);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CampaignAdapter();
        recyclerView.setAdapter(adapter);

        viewModel.getAllCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            adapter.setCampaigns(campaigns);
        });
    }

    private class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.ViewHolder> {
        private List<CampaignEntity> campaigns = new ArrayList<>();

        void setCampaigns(List<CampaignEntity> campaigns) {
            this.campaigns = campaigns;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quick_campaign, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CampaignEntity campaign = campaigns.get(position);
            holder.nameText.setText(campaign.name);
            holder.systemText.setText(campaign.gameSystem);
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CampaignDetailActivity.class);
                intent.putExtra(CampaignDetailActivity.EXTRA_CAMPAIGN_ID, campaign.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return campaigns.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            TextView systemText;
            ViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.tv_name);
                systemText = itemView.findViewById(R.id.tv_system);
            }
        }
    }
}
