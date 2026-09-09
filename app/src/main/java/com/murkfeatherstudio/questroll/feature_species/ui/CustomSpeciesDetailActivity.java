package com.murkfeatherstudio.questroll.feature_species.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.murkfeatherstudio.questroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.murkfeatherstudio.questroll.databinding.ActivitySpeciesDetailBinding;
import com.murkfeatherstudio.questroll.feature_species.viewmodel.CustomSpeciesCreateViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class CustomSpeciesDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "CUSTOM_SPECIES_ID";
    private Markwon markwon;
    private ActivitySpeciesDetailBinding binding;
    private boolean detailsAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpeciesDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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

    /**
     * NOTE: Part of the UI (race details and traits) is built dynamically at runtime (addView()),
     * there is no static XML layout for these fields - in these places ViewBinding does not apply.
     */
    private void populateUI(CustomSpeciesEntity s) {
        binding.tvSpeciesName.setText(s.name);

        if (s.isSubspecies && s.subspeciesOfName != null && !s.subspeciesOfName.isEmpty()) {
            binding.tvSubtitle.setText("Subspecies of " + s.subspeciesOfName);
            binding.tvSubtitle.setVisibility(View.VISIBLE);
            binding.tvSubtitle.setPaintFlags(binding.tvSubtitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            binding.tvSubtitle.setOnClickListener(v -> {
                String key = s.subspeciesOfKey;
                if (key == null || key.isEmpty()) return;
                if (key.startsWith("custom_")) {
                    long subId = Long.parseLong(key.replace("custom_", ""));
                    Intent i = new Intent(this, CustomSpeciesDetailActivity.class);
                    i.putExtra(EXTRA_ID, subId);
                    startActivity(i);
                } else {
                    Intent i = new Intent(this, SpeciesDetailActivity.class);
                    i.putExtra("SPECIES_KEY", key);
                    startActivity(i);
                }
            });
        } else {
            binding.tvSubtitle.setText("Custom Species");
            binding.tvSubtitle.setVisibility(View.VISIBLE);
            binding.tvSubtitle.setOnClickListener(null);
            binding.tvSubtitle.setPaintFlags(binding.tvSubtitle.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
        }

        if (s.desc != null && !s.desc.isEmpty()) {
            markwon.setMarkdown(binding.tvDesc, s.desc);
            binding.tvDesc.setVisibility(View.VISIBLE);
        } else {
            binding.tvDesc.setVisibility(View.GONE);
        }

        if (!detailsAdded) {
            LinearLayout detailsContainer = new LinearLayout(this);
            detailsContainer.setOrientation(LinearLayout.VERTICAL);
            detailsContainer.setPadding(0, 0, 0, dp(16));
            
            // Inserting the details container before traits_section
            View traitsSection = binding.traitsSection;
            LinearLayout parent = (LinearLayout) traitsSection.getParent();
            parent.addView(detailsContainer, parent.indexOfChild(traitsSection));
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

        binding.subspeciesSection.setVisibility(View.GONE);

        binding.btnManage.setVisibility(View.VISIBLE);
        binding.btnManage.setOnClickListener(v -> showManageMenu(v, s.id));
    }

    private void addDetailRow(LinearLayout container, String label, String value) {
        TextView row = new TextView(this);
        row.setText(label + ": " + value);
        row.setPadding(0, dp(4), 0, dp(4));
        row.setTextSize(14);
        row.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        container.addView(row);
    }

    private void buildOtherTraits(String otherTraitsJson) {
        binding.traitsContainer.removeAllViews();
        if (otherTraitsJson == null || otherTraitsJson.isEmpty()) {
            binding.traitsSection.setVisibility(View.GONE);
            return;
        }
        try {
            Type type = new TypeToken<List<CustomCreatureAction>>(){}.getType();
            List<CustomCreatureAction> traits = new Gson().fromJson(otherTraitsJson, type);
            if (traits == null || traits.isEmpty()) {
                binding.traitsSection.setVisibility(View.GONE);
                return;
            }
            binding.traitsSection.setVisibility(View.VISIBLE);
            for (CustomCreatureAction t : traits) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0, dp(8), 0, dp(4));

                TextView tvName = new TextView(this);
                tvName.setText(t.name);
                tvName.setTypeface(null, Typeface.BOLD);
                tvName.setTextSize(15);
                tvName.setTextColor(getResources().getColor(R.color.threads_gold, null));
                row.addView(tvName);

                if (t.desc != null && !t.desc.isEmpty()) {
                    TextView tvDesc = new TextView(this);
                    markwon.setMarkdown(tvDesc, t.desc);
                    tvDesc.setTextSize(14);
                    tvDesc.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                    row.addView(tvDesc);
                }
                binding.traitsContainer.addView(row);
            }
        } catch (Exception e) {
            binding.traitsSection.setVisibility(View.GONE);
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
