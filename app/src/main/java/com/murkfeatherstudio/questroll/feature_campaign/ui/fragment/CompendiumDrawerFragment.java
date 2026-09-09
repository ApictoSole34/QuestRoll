package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.databinding.FragmentCompendiumDrawerBinding;

/**
 * A drawer fragment that displays the compendium menu for quick navigation
 * while inside a campaign.
 */
public class CompendiumDrawerFragment extends Fragment {

    private FragmentCompendiumDrawerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCompendiumDrawerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        if (activity == null) return;

        binding.menuClasses.setOnClickListener(v -> activity.openClassListActivity(null));
        binding.menuSpells.setOnClickListener(v -> activity.openSpellListActivity(null));
        binding.menuCreatures.setOnClickListener(v -> activity.openCreatureListActivity(null));
        binding.menuCreatureTypes.setOnClickListener(v -> activity.openCreatureTypeListActivity(null));
        binding.menuSpecies.setOnClickListener(v -> activity.openSpeciesListActivity(null));
        binding.menuBackgrounds.setOnClickListener(v -> activity.openBackgroundListActivity(null));
        binding.menuLanguages.setOnClickListener(v -> activity.openLanguageListActivity(null));
        binding.menuAbilities.setOnClickListener(v -> activity.openAbilityListActivity(null));
        binding.menuAlignments.setOnClickListener(v -> activity.openAlignmentListActivity(null));
        binding.menuConditions.setOnClickListener(v -> activity.openConditionListActivity(null));
        binding.menuEnvironments.setOnClickListener(v -> activity.openEnvironmentListActivity(null));
        binding.menuRulesets.setOnClickListener(v -> activity.openRulesetListActivity(null));
        binding.menuServices.setOnClickListener(v -> activity.openServiceListActivity(null));
        binding.menuItems.setOnClickListener(v -> activity.openItemListActivity(null));
        binding.menuItemSets.setOnClickListener(v -> activity.openItemSetListActivity(null));
        binding.menuItemRarities.setOnClickListener(v -> activity.openItemRarityListActivity(null));
        binding.menuWeaponProperties.setOnClickListener(v -> activity.openWeaponPropertyListActivity(null));
        binding.menuDamageTypes.setOnClickListener(v -> activity.openDamageTypeListActivity(null));
        binding.menuPdf.setOnClickListener(v -> activity.openPdfListActivity(null));
    }
}
