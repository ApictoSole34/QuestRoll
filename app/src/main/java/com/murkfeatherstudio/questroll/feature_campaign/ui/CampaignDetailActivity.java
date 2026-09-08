package com.murkfeatherstudio.questroll.feature_campaign.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignCharacterSheetFragment;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignDiceFragment;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignEquipmentFragment;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignNotesFragment;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignTraitsFragment;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.CampaignDetailViewModel;

public class CampaignDetailActivity extends BaseActivity {

    public static final String EXTRA_CAMPAIGN_ID = "campaign_id";

    private CampaignDetailViewModel viewModel;
    private long campaignId;
    private Button btnSheet, btnEquipment, btnTraits, btnNotes, btnDice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign_detail);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        campaignId = getIntent().getLongExtra(EXTRA_CAMPAIGN_ID, -1L);
        if (campaignId == -1L) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(CampaignDetailViewModel.class);
        viewModel.setCampaignId(campaignId);

        // Initialize Calculator Drawer Width
        setDrawerWidth(false);

        viewModel.campaign.observe(this, campaign -> {
            if (campaign != null && getSupportActionBar() != null) {
                getSupportActionBar().setTitle(campaign.name);
            }
        });

        btnSheet = findViewById(R.id.nav_sheet);
        btnEquipment = findViewById(R.id.nav_equipment);
        btnTraits = findViewById(R.id.nav_traits);
        btnNotes = findViewById(R.id.nav_notes);
        btnDice = findViewById(R.id.nav_dice);

        btnSheet.setOnClickListener(v -> onNavItemSelected(R.id.nav_sheet));
        btnEquipment.setOnClickListener(v -> onNavItemSelected(R.id.nav_equipment));
        btnTraits.setOnClickListener(v -> onNavItemSelected(R.id.nav_traits));
        btnNotes.setOnClickListener(v -> onNavItemSelected(R.id.nav_notes));
        btnDice.setOnClickListener(v -> onNavItemSelected(R.id.nav_dice));

        if (savedInstanceState == null) {
            onNavItemSelected(R.id.nav_sheet);
        }
    }

    private void onNavItemSelected(int id) {
        Fragment fragment = null;
        if (id == R.id.nav_sheet) {
            fragment = CampaignCharacterSheetFragment.newInstance(campaignId);
        } else if (id == R.id.nav_equipment) {
            fragment = CampaignEquipmentFragment.newInstance(campaignId);
        } else if (id == R.id.nav_traits) {
            fragment = CampaignTraitsFragment.newInstance(campaignId, false);
        } else if (id == R.id.nav_notes) {
            fragment = CampaignNotesFragment.newInstance(campaignId);
        } else if (id == R.id.nav_dice) {
            fragment = CampaignDiceFragment.newInstance();
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }

        resetNavButtonsStyle();
        Button active = findViewById(id);
        if (active != null) {
            active.setTextColor(getResources().getColor(R.color.threads_gold, null));
            active.setAlpha(1.0f);
        }
    }

    private void resetNavButtonsStyle() {
        int defaultColor = getResources().getColor(R.color.threads_text_secondary, null);
        btnSheet.setTextColor(defaultColor);
        btnEquipment.setTextColor(defaultColor);
        btnTraits.setTextColor(defaultColor);
        btnNotes.setTextColor(defaultColor);
        btnDice.setTextColor(defaultColor);
        btnSheet.setAlpha(0.7f);
        btnEquipment.setAlpha(0.7f);
        btnTraits.setAlpha(0.7f);
        btnNotes.setAlpha(0.7f);
        btnDice.setAlpha(0.7f);
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_campaign_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_edit_campaign) {
            Intent intent = new Intent(this, CreateEditCampaignActivity.class);
            intent.putExtra(CreateEditCampaignActivity.EXTRA_CAMPAIGN_ID, campaignId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
