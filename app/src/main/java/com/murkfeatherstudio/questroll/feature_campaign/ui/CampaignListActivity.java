package com.murkfeatherstudio.questroll.feature_campaign.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.databinding.ActivityCampaignListBinding;
import com.murkfeatherstudio.questroll.feature_campaign.adapter.CampaignAdapter;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.CampaignListViewModel;

public class CampaignListActivity extends BaseActivity {

    private ActivityCampaignListBinding binding;
    private CampaignListViewModel viewModel;
    private CampaignAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCampaignListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Campaigns");

        binding.recyclerCampaigns.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CampaignAdapter(
                campaign -> {
                    Intent intent = new Intent(this, CampaignDetailActivity.class);
                    intent.putExtra(CampaignDetailActivity.EXTRA_CAMPAIGN_ID, campaign.id);
                    startActivity(intent);
                },
                campaign -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete campaign")
                            .setMessage("Delete \"" + campaign.name + "\"? This will also delete all notes.")
                            .setPositiveButton("Delete", (d, w) -> viewModel.deleteCampaign(campaign))
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );
        binding.recyclerCampaigns.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(CampaignListViewModel.class);
        viewModel.campaigns.observe(this, campaigns -> {
            if (campaigns != null) adapter.setCampaigns(campaigns);
        });

        binding.fabAddCampaign.setOnClickListener(v ->
                startActivity(new Intent(this, CreateEditCampaignActivity.class)));
    }
}
