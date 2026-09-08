package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;

/**
 * A drawer fragment that displays the compendium menu for quick navigation
 * while inside a campaign.
 */
public class CompendiumDrawerFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compendium_drawer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        if (activity == null) return;

        view.findViewById(R.id.menu_classes).setOnClickListener(v -> activity.openClassListActivity(null));
        view.findViewById(R.id.menu_spells).setOnClickListener(v -> activity.openSpellListActivity(null));
        view.findViewById(R.id.menu_creatures).setOnClickListener(v -> activity.openCreatureListActivity(null));
        view.findViewById(R.id.menu_creature_types).setOnClickListener(v -> activity.openCreatureTypeListActivity(null));
        view.findViewById(R.id.menu_species).setOnClickListener(v -> activity.openSpeciesListActivity(null));
        view.findViewById(R.id.menu_backgrounds).setOnClickListener(v -> activity.openBackgroundListActivity(null));
        view.findViewById(R.id.menu_languages).setOnClickListener(v -> activity.openLanguageListActivity(null));
        view.findViewById(R.id.menu_abilities).setOnClickListener(v -> activity.openAbilityListActivity(null));
        view.findViewById(R.id.menu_alignments).setOnClickListener(v -> activity.openAlignmentListActivity(null));
        view.findViewById(R.id.menu_conditions).setOnClickListener(v -> activity.openConditionListActivity(null));
        view.findViewById(R.id.menu_environments).setOnClickListener(v -> activity.openEnvironmentListActivity(null));
        view.findViewById(R.id.menu_rulesets).setOnClickListener(v -> activity.openRulesetListActivity(null));
        view.findViewById(R.id.menu_services).setOnClickListener(v -> activity.openServiceListActivity(null));
        view.findViewById(R.id.menu_items).setOnClickListener(v -> activity.openItemListActivity(null));
        view.findViewById(R.id.menu_item_sets).setOnClickListener(v -> activity.openItemSetListActivity(null));
        view.findViewById(R.id.menu_item_rarities).setOnClickListener(v -> activity.openItemRarityListActivity(null));
        view.findViewById(R.id.menu_weapon_properties).setOnClickListener(v -> activity.openWeaponPropertyListActivity(null));
        view.findViewById(R.id.menu_damage_types).setOnClickListener(v -> activity.openDamageTypeListActivity(null));
        view.findViewById(R.id.menu_pdf).setOnClickListener(v -> activity.openPdfListActivity(null));
    }
}
