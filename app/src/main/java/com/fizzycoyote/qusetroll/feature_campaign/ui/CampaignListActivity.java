package com.fizzycoyote.qusetroll.feature_campaign.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.feature_campaign.adapter.CampaignAdapter;
import com.fizzycoyote.qusetroll.feature_campaign.view_model.CampaignListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CampaignListActivity extends BaseActivity {

    private CampaignListViewModel viewModel;
    private CampaignAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign_list);
        setTitle("Campaigns");

        RecyclerView recyclerView = findViewById(R.id.recycler_campaigns);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(CampaignListViewModel.class);
        viewModel.campaigns.observe(this, campaigns -> {
            if (campaigns != null) adapter.setCampaigns(campaigns);
        });

        FloatingActionButton fab = findViewById(R.id.fab_add_campaign);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, CreateEditCampaignActivity.class)));
    }
}