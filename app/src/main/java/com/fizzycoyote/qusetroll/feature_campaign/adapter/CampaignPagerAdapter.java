package com.fizzycoyote.qusetroll.feature_campaign.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignCharacterSheetFragment;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignDiceFragment;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignEquipmentFragment;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignNotesFragment;
import com.fizzycoyote.qusetroll.feature_campaign.ui.fragment.CampaignSpellsFragment;

public class CampaignPagerAdapter extends FragmentStateAdapter {
    private final long campaignId;

    public CampaignPagerAdapter(@NonNull FragmentActivity fragmentActivity, long campaignId) {
        super(fragmentActivity);
        this.campaignId = campaignId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return CampaignCharacterSheetFragment.newInstance(campaignId);
            case 1:
                return CampaignSpellsFragment.newInstance(campaignId);
            case 2:
                return CampaignEquipmentFragment.newInstance(campaignId);
            case 3:
                return CampaignNotesFragment.newInstance(campaignId);
            default:
                return CampaignDiceFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}