package com.murkfeatherstudio.questroll.core.base;

import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.feature_ability.ui.AbilityListActivity;
import com.murkfeatherstudio.questroll.feature_alignment.ui.AlignmentListActivity;
import com.murkfeatherstudio.questroll.feature_background.ui.BackgroundListActivity;
import com.murkfeatherstudio.questroll.feature_campaign.ui.CampaignListActivity;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.CompendiumDrawerFragment;
import com.murkfeatherstudio.questroll.feature_campaign.ui.fragment.QuickCampaignFragment;
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
import com.murkfeatherstudio.questroll.feature_rule.rule_set.ui.RulesetListActivity;
import com.murkfeatherstudio.questroll.feature_service.ui.ServiceListActivity;
import com.murkfeatherstudio.questroll.feature_species.ui.SpeciesListActivity;
import com.murkfeatherstudio.questroll.feature_spell.ui.SpellListActivity;
import com.murkfeatherstudio.questroll.feature_tools.pdf.ui.PdfListActivity;
import com.murkfeatherstudio.questroll.main.ui.AboutActivity;

import java.util.Random;

/**
 * Base activity that provides a global themed background and an automatic
 * Navigation Drawer (Quick Campaign & Calculator) for most screens.
 * Subclasses benefit from shared navigation logic and consistent UI decoration.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Array of drawable resources used for the randomized background. */
    private static final int[] BACKGROUNDS = {
            R.drawable.threads_bg_1, R.drawable.threads_bg_2, R.drawable.threads_bg_3,
            R.drawable.threads_bg_4, R.drawable.threads_bg_5, R.drawable.threads_bg_6,
            R.drawable.threads_bg_7, R.drawable.threads_bg_8
    };

    /** The root drawer layout, if present in the current layout configuration. */
    protected DrawerLayout drawerLayout;

    /**
     * Overridden to wrap the activity layout in a Base Drawer if applicable.
     * Also triggers the randomized background setup.
     *
     * @param layoutResID Resource ID to be inflated as the main content.
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (shouldShowDrawer()) {
            // Inflate the base drawer layout
            drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base_drawer, null);
            FrameLayout container = drawerLayout.findViewById(R.id.activity_content_container);

            // Inflate the actual activity layout into the container
            getLayoutInflater().inflate(layoutResID, container, true);
            super.setContentView(drawerLayout);

            setupDrawerContent();
        } else {
            super.setContentView(layoutResID);
        }
        setupBackground();
    }

    /**
     * Injects the correct fragment into the left drawer container based on the activity type.
     * Campaigns get the Compendium Drawer, while other activities get the Quick Campaign view.
     */
    private void setupDrawerContent() {
        Fragment drawerFragment;
        if (isCampaignActivity()) {
            drawerFragment = new CompendiumDrawerFragment();
        } else {
            drawerFragment = new QuickCampaignFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.left_drawer_container, drawerFragment)
                .commit();
    }

    /**
     * Determines if the drawer should be injected for the current activity.
     * Screens like Splash or Loading are excluded to maintain a clean UI.
     *
     * @return True if the drawer should be shown.
     */
    private boolean shouldShowDrawer() {
        String className = getClass().getName();
        return !className.contains("SplashActivity") &&
               !className.contains("LoadingActivity");
    }

    /**
     * Helper to detect if the current activity belongs to the campaign feature set.
     */
    private boolean isCampaignActivity() {
        return getClass().getName().contains(".feature_campaign.ui");
    }

    /**
     * Selects a random background from {@link #BACKGROUNDS} and applies randomized
     * translation/scaling to create a unique look for every screen session.
     */
    private void setupBackground() {
        ImageView backgroundImage = findViewById(R.id.backgroundImage);
        if (backgroundImage == null) return;

        Random random = new Random();
        int randomIndex = random.nextInt(BACKGROUNDS.length);

        backgroundImage.setImageResource(BACKGROUNDS[randomIndex]);
        backgroundImage.setScaleX(1.35f);
        backgroundImage.setScaleY(1.35f);

        float offsetX = (random.nextFloat() - 0.5f) * 300f;
        float offsetY = (random.nextFloat() - 0.5f) * 600f;
        backgroundImage.setTranslationX(offsetX);
        backgroundImage.setTranslationY(offsetY);
    }

    // --- DRAWER HELPERS ---

    /** Opens the left navigation drawer (Start side). */
    public void openLeftDrawer() {
        if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
    }

    /** Opens the right navigation drawer (End side). */
    public void openRightDrawer() {
        if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.END);
    }

    /**
     * Adjusts the width of the calculator drawer.
     *
     * @param fullScreen If true, set to full screen width; otherwise, 80% of screen width.
     */
    public void setDrawerWidth(boolean fullScreen) {
        View calculatorDrawer = findViewById(R.id.calculator_drawer_container);
        if (calculatorDrawer == null) return;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = fullScreen ? metrics.widthPixels : (int) (metrics.widthPixels * 0.8);

        ViewGroup.LayoutParams params = calculatorDrawer.getLayoutParams();
        params.width = width;
        calculatorDrawer.setLayoutParams(params);
    }

    // --- GLOBAL NAVIGATION METHODS ---

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
}
