package com.fizzycoyote.qusetroll.main.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.feature_ability.ui.AbilityListActivity;
import com.fizzycoyote.qusetroll.feature_alignment.ui.AlignmentListActivity;
import com.fizzycoyote.qusetroll.feature_background.ui.BackgroundListActivity;
import com.fizzycoyote.qusetroll.feature_campaign.ui.CampaignListActivity;
import com.fizzycoyote.qusetroll.feature_character.ui.list.CharacterListActivity;
import com.fizzycoyote.qusetroll.feature_class.ui.ClassListActivity;
import com.fizzycoyote.qusetroll.feature_condition.ui.ConditionListActivity;
import com.fizzycoyote.qusetroll.feature_creature.creature_type.ui.CreatureTypeListActivity;
import com.fizzycoyote.qusetroll.feature_creature.ui.CreatureListActivity;
import com.fizzycoyote.qusetroll.feature_damage_types.ui.DamageTypeListActivity;
import com.fizzycoyote.qusetroll.feature_dice.ui.RollDiceActivity;
import com.fizzycoyote.qusetroll.feature_environment.ui.EnvironmentListActivity;
import com.fizzycoyote.qusetroll.feature_item.item_rarity.ui.ItemRarityListActivity;
import com.fizzycoyote.qusetroll.feature_item.item_set.ui.ItemSetListActivity;
import com.fizzycoyote.qusetroll.feature_item.ui.ItemListActivity;
import com.fizzycoyote.qusetroll.feature_item.weapon_property.ui.WeaponPropertyListActivity;
import com.fizzycoyote.qusetroll.feature_language.ui.language_list.LanguageListActivity;
import com.fizzycoyote.qusetroll.feature_loading.LoadingActivity;
import com.fizzycoyote.qusetroll.feature_rule.rule_set.ui.RulesetListActivity;
import com.fizzycoyote.qusetroll.feature_service.ui.ServiceListActivity;
import com.fizzycoyote.qusetroll.feature_species.ui.SpeciesListActivity;
import com.fizzycoyote.qusetroll.feature_spell.ui.SpellListActivity;

/**
 * The main entry point of the application after data has been loaded.
 * <p>
 * This activity serves as a navigation hub, providing access to all major features
 * of the app, including character management, campaign tracking, dice rolling,
 * and the D&D 5e game data compendium.
 * </p>
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnManageData).setOnClickListener(v -> showDataManagementDialog());
    }

    // ── NAVIGATION

    public void openCampaignListActivity(View view) {
        startActivity(new Intent(this, CampaignListActivity.class));
    }

    public void openCharacterListActivity(View view) {
        startActivity(new Intent(this, CharacterListActivity.class));
    }

    public void openRollDiceActivity(View view) {
        startActivity(new Intent(this, RollDiceActivity.class));
    }

    public void openSpellListActivity(View view) {
        startActivity(new Intent(this, SpellListActivity.class));
    }

    public void openLanguageListActivity(View view) {
        startActivity(new Intent(this, LanguageListActivity.class));
    }

    public void openClassListActivity(View view) {
        startActivity(new Intent(this, ClassListActivity.class));
    }

    public void openCreatureListActivity(View view) {
        startActivity(new Intent(this, CreatureListActivity.class));
    }

    public void openCreatureTypeListActivity(View view) {
        startActivity(new Intent(this, CreatureTypeListActivity.class));
    }

    public void openSpeciesListActivity(View view) {
        startActivity(new Intent(this, SpeciesListActivity.class));
    }

    public void openBackgroundListActivity(View view) {
        startActivity(new Intent(this, BackgroundListActivity.class));
    }

    public void openItemListActivity(View view) {
        startActivity(new Intent(this, ItemListActivity.class));
    }

    public void openDamageTypeListActivity(View view) {
        startActivity(new Intent(this, DamageTypeListActivity.class));
    }

    public void openAbilityListActivity(View view) {
        startActivity(new Intent(this, AbilityListActivity.class));
    }

    public void openAlignmentListActivity(View view) {
        startActivity(new Intent(this, AlignmentListActivity.class));
    }

    public void openItemRarityListActivity(View view) {
        startActivity(new Intent(this, ItemRarityListActivity.class));
    }

    public void openWeaponPropertyListActivity(View view) {
        startActivity(new Intent(this, WeaponPropertyListActivity.class));
    }

    public void openServiceListActivity(View view) {
        startActivity(new Intent(this, ServiceListActivity.class));
    }

    public void openEnvironmentListActivity(View view) {
        startActivity(new Intent(this, EnvironmentListActivity.class));
    }

    public void openRulesetListActivity(View view) {
        startActivity(new Intent(this, RulesetListActivity.class));
    }

    public void openConditionListActivity(View view) {
        startActivity(new Intent(this, ConditionListActivity.class));
    }

    public void openItemSetListActivity(View view) {
        startActivity(new Intent(this, ItemSetListActivity.class));
    }

    // ── DATA MANAGEMENT

    private void showDataManagementDialog() {
        new AlertDialog.Builder(this)
                .setTitle("API Data")
                .setMessage("Refresh all data from open5e API? This may take a while and requires an internet connection.")
                .setPositiveButton("Refresh", (d, w) -> openLoadingActivity())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openLoadingActivity() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .remove("data_loaded")
                .apply();

        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra("force_refresh", true);
        startActivity(intent);
    }
}
