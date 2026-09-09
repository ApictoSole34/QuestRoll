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
import com.murkfeatherstudio.questroll.databinding.ActivityCampaignDetailBinding;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignCharacterSheetFragment;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignDiceFragment;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignEquipmentFragment;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignNotesFragment;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CampaignTraitsFragment;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.CampaignDetailViewModel;

/**
 * Activity coordinating the campaign dashboard and its various tabs.
 */
public class CampaignDetailActivity extends BaseActivity {

    public static final String EXTRA_CAMPAIGN_ID = "campaign_id";

    private CampaignDetailViewModel viewModel;
    private long campaignId;
    private ActivityCampaignDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCampaignDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
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

        binding.navSheet.setOnClickListener(v -> onNavItemSelected(R.id.nav_sheet));
        binding.navEquipment.setOnClickListener(v -> onNavItemSelected(R.id.nav_equipment));
        binding.navTraits.setOnClickListener(v -> onNavItemSelected(R.id.nav_traits));
        binding.navNotes.setOnClickListener(v -> onNavItemSelected(R.id.nav_notes));
        binding.navDice.setOnClickListener(v -> onNavItemSelected(R.id.nav_dice));

        if (savedInstanceState == null) {
            onNavItemSelected(R.id.nav_sheet);
        }
    }

    /**
     * Switches between different campaign fragments.
     *
     * @param id The resource ID of the navigation item.
     *
     * JAVADOC: findViewById is used here because 'id' is passed dynamically to this method
     * from various click listeners. View Binding provides static field access to views,
     * but doesn't offer a clean way to select a view based on a runtime resource ID
     * variable without using reflection or a large switch-case. Traditional
     * findViewById is kept for this specific dynamic UI interaction where we need to
     * find a button to highlight it.
     */
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
        binding.navSheet.setTextColor(defaultColor);
        binding.navEquipment.setTextColor(defaultColor);
        binding.navTraits.setTextColor(defaultColor);
        binding.navNotes.setTextColor(defaultColor);
        binding.navDice.setTextColor(defaultColor);

        binding.navSheet.setAlpha(0.7f);
        binding.navEquipment.setAlpha(0.7f);
        binding.navTraits.setAlpha(0.7f);
        binding.navNotes.setAlpha(0.7f);
        binding.navDice.setAlpha(0.7f);
    }

    private Fragment getCurrentFragment() {
        /**
         * JAVADOC: findViewById is used here as part of the fragment management API
         * (findFragmentById) which requires the integer ID of the container.
         */
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
