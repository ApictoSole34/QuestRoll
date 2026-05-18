package com.fizzycoyote.qusetroll.feature_species.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.fizzycoyote.qusetroll.feature_species.viewmodel.CustomSpeciesCreateViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class CustomSpeciesDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "CUSTOM_SPECIES_ID";
    private Markwon markwon;
    private boolean detailsAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_detail);
        markwon = Markwon.create(this);

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }

        UserContentDatabase.getInstance(this).customSpeciesDao()
                .getById(id).observe(this, s -> {
                    if (s != null) populateUI(s);
                });
    }

    private void populateUI(CustomSpeciesEntity s) {
        ((TextView) findViewById(R.id.tv_species_name)).setText(s.name);

        TextView tvSubtitle = findViewById(R.id.tv_subtitle);
        if (s.isSubspecies && s.subspeciesOfName != null && !s.subspeciesOfName.isEmpty()) {
            tvSubtitle.setText("Subspecies of " + s.subspeciesOfName);
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setPaintFlags(tvSubtitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvSubtitle.setOnClickListener(v -> {
                String key = s.subspeciesOfKey;
                if (key == null || key.isEmpty()) return;
                if (key.startsWith("custom_")) {
                    long id = Long.parseLong(key.replace("custom_", ""));
                    Intent i = new Intent(this, CustomSpeciesDetailActivity.class);
                    i.putExtra(EXTRA_ID, id);
                    startActivity(i);
                } else {
                    Intent i = new Intent(this, SpeciesDetailActivity.class);
                    i.putExtra("SPECIES_KEY", key);
                    startActivity(i);
                }
            });
        } else {
            tvSubtitle.setText("Custom Species");
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setOnClickListener(null);
            tvSubtitle.setPaintFlags(tvSubtitle.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
        }

        TextView tvDesc = findViewById(R.id.tv_desc);
        if (s.desc != null && !s.desc.isEmpty()) {
            markwon.setMarkdown(tvDesc, s.desc);
            tvDesc.setVisibility(View.VISIBLE);
        } else {
            tvDesc.setVisibility(View.GONE);
        }

        LinearLayout traitsContainer = findViewById(R.id.traits_container);
        View parent = (View) traitsContainer.getParent();
        if (parent instanceof LinearLayout && !detailsAdded) {
            LinearLayout detailsContainer = new LinearLayout(this);
            detailsContainer.setOrientation(LinearLayout.VERTICAL);
            detailsContainer.setPadding(0, 0, 0, dp(16));
            ((LinearLayout) parent).addView(detailsContainer, ((LinearLayout) parent).indexOfChild(traitsContainer));
            detailsAdded = true;

            if (s.speed != null && !s.speed.isEmpty()) {
                addDetailRow(detailsContainer, "Speed", s.speed);
            }
            if (s.size != null && !s.size.isEmpty()) {
                addDetailRow(detailsContainer, "Size", s.size);
            }
            if (s.abilityBonusesJson != null && !s.abilityBonusesJson.isEmpty()) {
                try {
                    Type type = new TypeToken<List<CustomSpeciesCreateViewModel.AbilityBonus>>(){}.getType();
                    List<CustomSpeciesCreateViewModel.AbilityBonus> bonuses = new Gson().fromJson(s.abilityBonusesJson, type);
                    if (bonuses != null && !bonuses.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (CustomSpeciesCreateViewModel.AbilityBonus ab : bonuses) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(ab.ability).append(" +").append(ab.bonus);
                        }
                        addDetailRow(detailsContainer, "Ability Score Bonuses", sb.toString());
                    }
                } catch (Exception e) { /* ignore */ }
            }
            if (s.languageKeysJson != null && !s.languageKeysJson.isEmpty()) {
                try {
                    Type type = new TypeToken<List<String>>(){}.getType();
                    List<String> langKeys = new Gson().fromJson(s.languageKeysJson, type);
                    if (langKeys != null && !langKeys.isEmpty()) {
                        addDetailRow(detailsContainer, "Known Languages", String.join(", ", langKeys));
                    }
                } catch (Exception e) { /* ignore */ }
            }
            if (s.languageChoices > 0) {
                addDetailRow(detailsContainer, "Additional Language Choices", String.valueOf(s.languageChoices));
            }
        }

        buildOtherTraits(s.otherTraitsJson);

        findViewById(R.id.traits_section).setVisibility(View.GONE);
        findViewById(R.id.subspecies_section).setVisibility(View.GONE);

        View btnManage = findViewById(R.id.btnManage);
        if (btnManage != null) {
            btnManage.setVisibility(View.VISIBLE);
            btnManage.setOnClickListener(v -> showManageMenu(v, s.id));
        }
    }

    private void addDetailRow(LinearLayout container, String label, String value) {
        TextView row = new TextView(this);
        row.setText(label + ": " + value);
        row.setPadding(0, dp(4), 0, dp(4));
        row.setTextSize(14);
        container.addView(row);
    }

    private void buildOtherTraits(String otherTraitsJson) {
        LinearLayout container = findViewById(R.id.traits_container);
        container.removeAllViews();
        if (otherTraitsJson == null || otherTraitsJson.isEmpty()) {
            findViewById(R.id.traits_section).setVisibility(View.GONE);
            return;
        }
        try {
            Type type = new TypeToken<List<CustomCreatureAction>>(){}.getType();
            List<CustomCreatureAction> traits = new Gson().fromJson(otherTraitsJson, type);
            if (traits == null || traits.isEmpty()) {
                findViewById(R.id.traits_section).setVisibility(View.GONE);
                return;
            }
            findViewById(R.id.traits_section).setVisibility(View.VISIBLE);
            for (CustomCreatureAction t : traits) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0, dp(8), 0, dp(4));

                TextView tvName = new TextView(this);
                tvName.setText(t.name);
                tvName.setTypeface(null, Typeface.BOLD);
                tvName.setTextSize(15);
                row.addView(tvName);

                if (t.desc != null && !t.desc.isEmpty()) {
                    TextView tvDesc = new TextView(this);
                    markwon.setMarkdown(tvDesc, t.desc);
                    tvDesc.setTextSize(14);
                    row.addView(tvDesc);
                }
                container.addView(row);
            }
        } catch (Exception e) {
            findViewById(R.id.traits_section).setVisibility(View.GONE);
        }
    }

    private void showManageMenu(View anchor, long id) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent i = new Intent(this, CustomSpeciesCreateActivity.class);
                i.putExtra(CustomSpeciesCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Species")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (d, w) ->
                                UserContentDatabase.getInstance(this).getQueryExecutor()
                                        .execute(() -> {
                                            UserContentDatabase.getInstance(this)
                                                    .customSpeciesDao().delete(id);
                                            runOnUiThread(this::finish);
                                        }))
                        .setNegativeButton("Cancel", null).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}