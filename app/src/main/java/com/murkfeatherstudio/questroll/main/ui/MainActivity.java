package com.murkfeatherstudio.questroll.main.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.databinding.ActivityMainBinding;
import com.murkfeatherstudio.questroll.feature_ability.ui.AbilityListActivity;
import com.murkfeatherstudio.questroll.feature_alignment.ui.AlignmentListActivity;
import com.murkfeatherstudio.questroll.feature_background.ui.BackgroundListActivity;
import com.murkfeatherstudio.questroll.feature_campaign.ui.CampaignListActivity;
import com.murkfeatherstudio.questroll.feature_character.ui.list.CharacterListActivity;
import com.murkfeatherstudio.questroll.feature_class.ui.ClassListActivity;
import com.murkfeatherstudio.questroll.feature_condition.ui.ConditionListActivity;
import com.murkfeatherstudio.questroll.feature_creature.creature_type.ui.CreatureTypeListActivity;
import com.murkfeatherstudio.questroll.feature_creature.ui.CreatureListActivity;
import com.murkfeatherstudio.questroll.feature_damage_types.ui.DamageTypeListActivity;
import com.murkfeatherstudio.questroll.feature_dice.ui.RollDiceActivity;
import com.murkfeatherstudio.questroll.feature_environment.ui.EnvironmentListActivity;
import com.murkfeatherstudio.questroll.feature_item.item_rarity.ui.ItemRarityListActivity;
import com.murkfeatherstudio.questroll.feature_item.item_set.ui.ItemSetListActivity;
import com.murkfeatherstudio.questroll.feature_item.ui.ItemListActivity;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.ui.WeaponPropertyListActivity;
import com.murkfeatherstudio.questroll.feature_language.ui.language_list.LanguageListActivity;
import com.murkfeatherstudio.questroll.feature_loading.LoadingActivity;
import com.murkfeatherstudio.questroll.feature_rule.rule_set.ui.RulesetListActivity;
import com.murkfeatherstudio.questroll.feature_service.ui.ServiceListActivity;
import com.murkfeatherstudio.questroll.feature_species.ui.SpeciesListActivity;
import com.murkfeatherstudio.questroll.feature_spell.ui.SpellListActivity;
import com.murkfeatherstudio.questroll.feature_tools.pdf.ui.PdfListActivity;

/**
 * The main entry point of the application.
 */
public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initial drawer width set to 80% of the screen
        setDrawerWidth(false);

        binding.btnManageData.setOnClickListener(v -> showDataManagementDialog());
    }

    @Override
    public void setDrawerWidth(boolean fullScreen) {
        // JAVADOC: calculatorDrawerContainer is accessed via drawerBinding from BaseActivity
        // because it belongs to the activity_base_drawer layout which wraps this activity.
        if (drawerBinding == null || drawerBinding.calculatorDrawerContainer == null) return;
        
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = fullScreen ? metrics.widthPixels : (int) (metrics.widthPixels * 0.8);

        ViewGroup.LayoutParams params = drawerBinding.calculatorDrawerContainer.getLayoutParams();
        params.width = width;
        drawerBinding.calculatorDrawerContainer.setLayoutParams(params);
    }

    // ── NAVIGATION METHODS ──

    public void openPdfListActivity(View view) {
        startActivity(new Intent(this, PdfListActivity.class));
    }

    public void openAboutActivity(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void openCampaignListActivity(View view) { startActivity(new Intent(this, CampaignListActivity.class)); }
    public void openCharacterListActivity(View view) { startActivity(new Intent(this, CharacterListActivity.class)); }
    public void openRollDiceActivity(View view) { startActivity(new Intent(this, RollDiceActivity.class)); }
    public void openSpellListActivity(View view) { startActivity(new Intent(this, SpellListActivity.class)); }
    public void openLanguageListActivity(View view) { startActivity(new Intent(this, LanguageListActivity.class)); }
    public void openClassListActivity(View view) { startActivity(new Intent(this, ClassListActivity.class)); }
    public void openCreatureListActivity(View view) { startActivity(new Intent(this, CreatureListActivity.class)); }
    public void openCreatureTypeListActivity(View view) { startActivity(new Intent(this, CreatureTypeListActivity.class)); }
    public void openSpeciesListActivity(View view) { startActivity(new Intent(this, SpeciesListActivity.class)); }
    public void openBackgroundListActivity(View view) { startActivity(new Intent(this, BackgroundListActivity.class)); }
    public void openItemListActivity(View view) { startActivity(new Intent(this, ItemListActivity.class)); }
    public void openDamageTypeListActivity(View view) { startActivity(new Intent(this, DamageTypeListActivity.class)); }
    public void openAbilityListActivity(View view) { startActivity(new Intent(this, AbilityListActivity.class)); }
    public void openAlignmentListActivity(View view) { startActivity(new Intent(this, AlignmentListActivity.class)); }
    public void openItemRarityListActivity(View view) { startActivity(new Intent(this, ItemRarityListActivity.class)); }
    public void openWeaponPropertyListActivity(View view) { startActivity(new Intent(this, WeaponPropertyListActivity.class)); }
    public void openServiceListActivity(View view) { startActivity(new Intent(this, ServiceListActivity.class)); }
    public void openEnvironmentListActivity(View view) { startActivity(new Intent(this, EnvironmentListActivity.class)); }
    public void openRulesetListActivity(View view) { startActivity(new Intent(this, RulesetListActivity.class)); }
    public void openConditionListActivity(View view) { startActivity(new Intent(this, ConditionListActivity.class)); }
    public void openItemSetListActivity(View view) { startActivity(new Intent(this, ItemSetListActivity.class)); }

    private void showDataManagementDialog() {
        new AlertDialog.Builder(this)
                .setTitle("API Data")
                .setMessage("Refresh all data from Open5e API? This requires an internet connection.")
                .setPositiveButton("Refresh", (d, w) -> openLoadingActivity())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openLoadingActivity() {
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().remove("data_loaded").apply();
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra("force_refresh", true);
        startActivity(intent);
    }
}
