package com.murkfeatherstudio.questroll.feature_creature.ui;

import android.content.Intent;
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
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCreatureDetailBinding;
import com.murkfeatherstudio.questroll.feature_character.engine.CharacterEngine;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;

public class CustomCreatureDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "CUSTOM_CREATURE_ID";
    private Markwon markwon;
    private ActivityCreatureDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatureDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        markwon = Markwon.create(this);

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) { finish(); return; }

        UserContentDatabase.getInstance(this).customCreatureDao()
                .getById(id).observe(this, c -> { if (c != null) populateUI(c); });
    }

    private void populateUI(CustomCreatureEntity c) {
        Gson gson = new Gson();

        binding.tvCreatureName.setText(c.name);
        binding.tvSizeTypeAlignment.setText(
                joinNonEmpty(" ", c.sizeName, c.typeName, "•", c.alignment));
        binding.tvCr.setText("CR " + (c.crText != null ? c.crText : "?")
                + (c.experiencePoints > 0 ? " (" + c.experiencePoints + " XP)" : ""));

        binding.tvAc.setText("Armor Class: " + c.armorClass
                + (c.armorDetail != null && !c.armorDetail.isEmpty()
                ? " (" + c.armorDetail + ")" : ""));
        binding.tvHp.setText("Hit Points: " + c.hitPoints
                + (c.hitDice != null && !c.hitDice.isEmpty() ? " (" + c.hitDice + ")" : ""));

        StringBuilder speed = new StringBuilder("Speed: ");
        if (c.speedWalk > 0) speed.append(c.speedWalk).append(" ft.");
        if (c.speedFly > 0) speed.append(", fly ").append(c.speedFly).append(" ft.");
        if (c.speedHover) speed.append(" (hover)");
        if (c.speedSwim > 0) speed.append(", swim ").append(c.speedSwim).append(" ft.");
        if (c.speedBurrow > 0) speed.append(", burrow ").append(c.speedBurrow).append(" ft.");
        if (c.speedClimb > 0) speed.append(", climb ").append(c.speedClimb).append(" ft.");
        binding.tvSpeed.setText(speed.toString());
        binding.tvSpeed.setVisibility(View.VISIBLE);

        binding.tvStr.setText(String.valueOf(c.str)); binding.tvStrMod.setText(mod(c.str));
        binding.tvDex.setText(String.valueOf(c.dex)); binding.tvDexMod.setText(mod(c.dex));
        binding.tvCon.setText(String.valueOf(c.con)); binding.tvConMod.setText(mod(c.con));
        binding.tvInt.setText(String.valueOf(c.intScore)); binding.tvIntMod.setText(mod(c.intScore));
        binding.tvWis.setText(String.valueOf(c.wis)); binding.tvWisMod.setText(mod(c.wis));
        binding.tvCha.setText(String.valueOf(c.cha)); binding.tvChaMod.setText(mod(c.cha));

        buildJsonSection(binding.tvSavingThrows, "Saving Throws", c.savingThrowsJson);
        buildJsonSection(binding.tvSkills, "Skills", c.skillBonusesJson);

        setIfNotEmpty(binding.tvDamageImmunities, "Damage Immunities: ", c.damageImmunities);
        setIfNotEmpty(binding.tvDamageResistances, "Damage Resistances: ", c.damageResistances);
        setIfNotEmpty(binding.tvDamageVulnerabilities, "Damage Vulnerabilities: ", c.damageVulnerabilities);
        setIfNotEmpty(binding.tvConditionImmunities, "Condition Immunities: ", c.conditionImmunities);

        StringBuilder senses = new StringBuilder("Senses: ");
        boolean hasSense = false;
        if (c.darkvisionRange > 0) { senses.append("darkvision ").append(c.darkvisionRange).append(" ft."); hasSense = true; }
        if (c.blindsightRange > 0) { if (hasSense) senses.append(", "); senses.append("blindsight ").append(c.blindsightRange).append(" ft."); hasSense = true; }
        if (c.tremorsenseRange > 0) { if (hasSense) senses.append(", "); senses.append("tremorsense ").append(c.tremorsenseRange).append(" ft."); hasSense = true; }
        if (c.truesightRange > 0) { if (hasSense) senses.append(", "); senses.append("truesight ").append(c.truesightRange).append(" ft."); hasSense = true; }
        if (hasSense) senses.append(", ");
        senses.append("passive Perception ").append(c.passivePerception);
        binding.tvSenses.setText(senses.toString());
        binding.tvSenses.setVisibility(View.VISIBLE);

        setIfNotEmpty(binding.tvLanguages, "Languages: ", c.languages);

        buildActionsSection(binding.traitsSection, binding.traitsContainer, c.traitsJson, gson);

        buildActionsFromJson(binding.actionsSection, binding.actionsContainer,
                binding.legendaryActionsSection, binding.legendaryActionsContainer,
                c.actionsJson, gson);

        binding.tvSource.setText("Source: Custom");
        binding.tvSource.setVisibility(View.VISIBLE);

        binding.btnManage.setVisibility(View.VISIBLE);
        binding.btnManage.setOnClickListener(v -> showManageMenu(v, c.id));
    }

    private void buildJsonSection(TextView textView, String label, String json) {
        if (json == null || json.isEmpty()) { textView.setVisibility(View.GONE); return; }
        try {
            Map<String, Object> map = new Gson().fromJson(json,
                    new TypeToken<Map<String, Object>>(){}.getType());
            StringBuilder sb = new StringBuilder(label + ": ");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                double val = ((Number) entry.getValue()).doubleValue();
                if (val == 0) continue;
                if (!first) sb.append(", ");
                sb.append(cap(entry.getKey().replace("_", " ")));
                sb.append(" ").append(val > 0 ? "+" : "").append((int) val);
                first = false;
            }
            if (first) { textView.setVisibility(View.GONE); return; }
            textView.setText(sb.toString());
            textView.setVisibility(View.VISIBLE);
        } catch (Exception e) { textView.setVisibility(View.GONE); }
    }

    /**
     * NOTE: traits_container is managed dynamically (addView()).
     * Views for individual traits are created at runtime based on JSON data.
     * ViewBinding is not applicable to these dynamically generated children.
     */
    private void buildActionsSection(View sectionView, LinearLayout container, String json, Gson gson) {
        container.removeAllViews();
        if (json == null || json.isEmpty()) { sectionView.setVisibility(View.GONE); return; }
        try {
            Type type = new TypeToken<List<CustomCreatureAction>>(){}.getType();
            List<CustomCreatureAction> list = gson.fromJson(json, type);
            if (list == null || list.isEmpty()) { sectionView.setVisibility(View.GONE); return; }
            sectionView.setVisibility(View.VISIBLE);
            for (CustomCreatureAction a : list) addActionView(container, a.name, a.desc);
        } catch (Exception e) { sectionView.setVisibility(View.GONE); }
    }

    /**
     * NOTE: actions_container and legendary_actions_container are managed dynamically.
     * Views are added at runtime using addActionView(). ViewBinding is not applicable
     * for dynamic elements inside these containers.
     */
    private void buildActionsFromJson(View actionsSection, LinearLayout actionsContainer,
                                      View legendarySection, LinearLayout legendaryContainer,
                                      String json, Gson gson) {
        actionsContainer.removeAllViews();
        legendaryContainer.removeAllViews();

        if (json == null || json.isEmpty()) {
            actionsSection.setVisibility(View.GONE);
            legendarySection.setVisibility(View.GONE);
            return;
        }
        try {
            Type type = new TypeToken<List<CustomCreatureAction>>(){}.getType();
            List<CustomCreatureAction> list = gson.fromJson(json, type);
            if (list == null || list.isEmpty()) {
                actionsSection.setVisibility(View.GONE);
                legendarySection.setVisibility(View.GONE);
                return;
            }
            boolean hasActions = false, hasLegendary = false;
            for (CustomCreatureAction a : list) {
                if ("LEGENDARY_ACTION".equals(a.actionType)) {
                    addActionView(legendaryContainer, a.name, a.desc);
                    hasLegendary = true;
                } else {
                    addActionView(actionsContainer, a.name, a.desc);
                    hasActions = true;
                }
            }
            actionsSection.setVisibility(hasActions ? View.VISIBLE : View.GONE);
            legendarySection.setVisibility(hasLegendary ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            actionsSection.setVisibility(View.GONE);
            legendarySection.setVisibility(View.GONE);
        }
    }

    private void addActionView(LinearLayout container, String name, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(8), 0, dp(4));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTypeface(null, Typeface.BOLD_ITALIC);
        tvName.setTextSize(14);
        tvName.setTextColor(getResources().getColor(R.color.threads_gold, null));
        row.addView(tvName);

        if (desc != null && !desc.isEmpty()) {
            TextView tvDesc = new TextView(this);
            markwon.setMarkdown(tvDesc, desc);
            tvDesc.setTextSize(14);
            tvDesc.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
            row.addView(tvDesc);
        }
        container.addView(row);
    }

    private void showManageMenu(View anchor, long id) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent i = new Intent(this, CustomCreatureCreateActivity.class);
                i.putExtra(CustomCreatureCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i); return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Creature")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (d, w) ->
                                UserContentDatabase.getInstance(this).getQueryExecutor().execute(() -> {
                                    UserContentDatabase.getInstance(this)
                                            .customCreatureDao().delete(id);
                                    runOnUiThread(this::finish);
                                }))
                        .setNegativeButton("Cancel", null).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private String mod(int score) {
        int m = CharacterEngine.getAbilityModifier(score);
        return m >= 0 ? "+" + m : String.valueOf(m);
    }

    private String joinNonEmpty(String sep, String... parts) {
        return Arrays.stream(parts).filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.joining(sep));
    }

    private String cap(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void setIfNotEmpty(TextView tv, String prefix, String val) {
        if (val != null && !val.isEmpty()) {
            tv.setText(prefix + val);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}
