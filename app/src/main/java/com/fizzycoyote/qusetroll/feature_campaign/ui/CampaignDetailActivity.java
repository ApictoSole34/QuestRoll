package com.fizzycoyote.qusetroll.feature_campaign.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.feature_campaign.adapter.CampaignPagerAdapter;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignCharacterSheetFragment;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignDiceFragment;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignEquipmentFragment;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignNotesFragment;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignSpellsFragment;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignTraitsFragment;
import com.fizzycoyote.qusetroll.feature_campaign.view_model.CampaignDetailViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CampaignDetailActivity extends BaseActivity {

    public static final String EXTRA_CAMPAIGN_ID = "campaign_id";

    private CampaignDetailViewModel viewModel;
    private long campaignId;
    private Button btnSheet, btnSpells, btnEquipment, btnTraits, btnNotes, btnDice;

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

        viewModel.campaign.observe(this, campaign -> {
            if (campaign != null && getSupportActionBar() != null) {
                getSupportActionBar().setTitle(campaign.name);
            }
        });

        viewModel.getCanCastSpells().observe(this, canCast -> {
            if (btnSpells != null) {
                btnSpells.setVisibility(canCast ? View.VISIBLE : View.GONE);
                if (!canCast && getCurrentFragment() instanceof CampaignSpellsFragment) {
                    onNavItemSelected(R.id.nav_sheet);
                }
            }
        });

        btnSheet = findViewById(R.id.nav_sheet);
        btnSpells = findViewById(R.id.nav_spells);
        btnEquipment = findViewById(R.id.nav_equipment);
        btnTraits = findViewById(R.id.nav_traits);
        btnNotes = findViewById(R.id.nav_notes);
        btnDice = findViewById(R.id.nav_dice);

        btnSheet.setOnClickListener(v -> onNavItemSelected(R.id.nav_sheet));
        btnSpells.setOnClickListener(v -> onNavItemSelected(R.id.nav_spells));
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
        } else if (id == R.id.nav_spells) {
            fragment = CampaignSpellsFragment.newInstance(campaignId);
        } else if (id == R.id.nav_equipment) {
            fragment = CampaignEquipmentFragment.newInstance(campaignId);
        } else if (id == R.id.nav_traits) {
            fragment = CampaignTraitsFragment.newInstance(campaignId);
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
            active.setTextColor(getColor(R.color.purple_500));
            active.setAlpha(1.0f);
        }
    }

    private void resetNavButtonsStyle() {
        int defaultColor = getColor(android.R.color.darker_gray);
        btnSheet.setTextColor(defaultColor);
        btnSpells.setTextColor(defaultColor);
        btnEquipment.setTextColor(defaultColor);
        btnTraits.setTextColor(defaultColor);
        btnNotes.setTextColor(defaultColor);
        btnDice.setTextColor(defaultColor);
        btnSheet.setAlpha(0.7f);
        btnSpells.setAlpha(0.7f);
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