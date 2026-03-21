package com.fizzycoyote.qusetroll.main.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.ui.list.CharacterListActivity;
import com.fizzycoyote.qusetroll.feature_class.ui.ClassListActivity;
import com.fizzycoyote.qusetroll.feature_creature.ui.CreatureListActivity;
import com.fizzycoyote.qusetroll.feature_dice.ui.RollDiceActivity;
import com.fizzycoyote.qusetroll.feature_language.ui.language_list.LanguageListActivity;
import com.fizzycoyote.qusetroll.feature_loading.LoadingActivity;
import com.fizzycoyote.qusetroll.feature_species.ui.SpeciesListActivity;
import com.fizzycoyote.qusetroll.feature_spell.ui.SpellListActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnManageData).setOnClickListener(v -> showDataManagementDialog());
    }

    // ── NAVIGATION

    public void openRollDiceActivity(View view) {
        startActivity(new Intent(this, RollDiceActivity.class));
    }

    public void openSpellListActivity(View view) {
        startActivity(new Intent(this, SpellListActivity.class));
    }

    public void openCharacterListActivity(View view) {
        startActivity(new Intent(this, CharacterListActivity.class));
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

    public void openSpeciesListActivity(View view) {
        startActivity(new Intent(this, SpeciesListActivity.class));
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