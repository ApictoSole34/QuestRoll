package com.fizzycoyote.qusetroll.feature_creature.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureEntity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creature_detail);
        markwon = Markwon.create(this);

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) { finish(); return; }

        UserContentDatabase.getInstance(this).customCreatureDao()
                .getById(id).observe(this, c -> { if (c != null) populateUI(c); });
    }

    private void populateUI(CustomCreatureEntity c) {
        Gson gson = new Gson();

        setText(R.id.tv_creature_name, c.name);
        setText(R.id.tv_size_type_alignment,
                joinNonEmpty(" ", c.sizeName, c.typeName, "•", c.alignment));
        setText(R.id.tv_cr, "CR " + (c.crText != null ? c.crText : "?")
                + (c.experiencePoints > 0 ? " (" + c.experiencePoints + " XP)" : ""));

        setText(R.id.tv_ac, "Armor Class: " + c.armorClass
                + (c.armorDetail != null && !c.armorDetail.isEmpty()
                ? " (" + c.armorDetail + ")" : ""));
        setText(R.id.tv_hp, "Hit Points: " + c.hitPoints
                + (c.hitDice != null && !c.hitDice.isEmpty() ? " (" + c.hitDice + ")" : ""));

        StringBuilder speed = new StringBuilder("Speed: ");
        if (c.speedWalk > 0) speed.append(c.speedWalk).append(" ft.");
        if (c.speedFly > 0) speed.append(", fly ").append(c.speedFly).append(" ft.");
        if (c.speedHover) speed.append(" (hover)");
        if (c.speedSwim > 0) speed.append(", swim ").append(c.speedSwim).append(" ft.");
        if (c.speedBurrow > 0) speed.append(", burrow ").append(c.speedBurrow).append(" ft.");
        if (c.speedClimb > 0) speed.append(", climb ").append(c.speedClimb).append(" ft.");
        setText(R.id.tv_speed, speed.toString());
        show(R.id.tv_speed);

        setText(R.id.tv_str, String.valueOf(c.str)); setText(R.id.tv_str_mod, mod(c.str));
        setText(R.id.tv_dex, String.valueOf(c.dex)); setText(R.id.tv_dex_mod, mod(c.dex));
        setText(R.id.tv_con, String.valueOf(c.con)); setText(R.id.tv_con_mod, mod(c.con));
        setText(R.id.tv_int, String.valueOf(c.intScore)); setText(R.id.tv_int_mod, mod(c.intScore));
        setText(R.id.tv_wis, String.valueOf(c.wis)); setText(R.id.tv_wis_mod, mod(c.wis));
        setText(R.id.tv_cha, String.valueOf(c.cha)); setText(R.id.tv_cha_mod, mod(c.cha));

        buildJsonSection(R.id.tv_saving_throws, "Saving Throws", c.savingThrowsJson);
        buildJsonSection(R.id.tv_skills, "Skills", c.skillBonusesJson);

        setIfNotEmpty(R.id.tv_damage_immunities, "Damage Immunities: ", c.damageImmunities);
        setIfNotEmpty(R.id.tv_damage_resistances, "Damage Resistances: ", c.damageResistances);
        setIfNotEmpty(R.id.tv_damage_vulnerabilities, "Damage Vulnerabilities: ", c.damageVulnerabilities);
        setIfNotEmpty(R.id.tv_condition_immunities, "Condition Immunities: ", c.conditionImmunities);

        StringBuilder senses = new StringBuilder("Senses: ");
        boolean hasSense = false;
        if (c.darkvisionRange > 0) { senses.append("darkvision ").append(c.darkvisionRange).append(" ft."); hasSense = true; }
        if (c.blindsightRange > 0) { if (hasSense) senses.append(", "); senses.append("blindsight ").append(c.blindsightRange).append(" ft."); hasSense = true; }
        if (c.tremorsenseRange > 0) { if (hasSense) senses.append(", "); senses.append("tremorsense ").append(c.tremorsenseRange).append(" ft."); hasSense = true; }
        if (c.truesightRange > 0) { if (hasSense) senses.append(", "); senses.append("truesight ").append(c.truesightRange).append(" ft."); hasSense = true; }
        if (hasSense) senses.append(", ");
        senses.append("passive Perception ").append(c.passivePerception);
        setText(R.id.tv_senses, senses.toString());
        show(R.id.tv_senses);

        setIfNotEmpty(R.id.tv_languages, "Languages: ", c.languages);

        buildActionsSection(R.id.traits_section, R.id.traits_container, c.traitsJson, gson);

        buildActionsFromJson(R.id.actions_section, R.id.actions_container,
                R.id.legendary_actions_section, R.id.legendary_actions_container,
                c.actionsJson, gson);

        setText(R.id.tv_source, "Source: Custom");
        show(R.id.tv_source);

        View btnManage = findViewById(R.id.btnManage);
        if (btnManage != null) {
            btnManage.setVisibility(View.VISIBLE);
            btnManage.setOnClickListener(v -> showManageMenu(v, c.id));
        }
    }

    private void buildJsonSection(int viewId, String label, String json) {
        if (json == null || json.isEmpty()) { hide(viewId); return; }
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
            if (first) { hide(viewId); return; }
            setText(viewId, sb.toString()); show(viewId);
        } catch (Exception e) { hide(viewId); }
    }

    private void buildActionsSection(int sectionId, int containerId, String json, Gson gson) {
        LinearLayout container = findViewById(containerId);
        container.removeAllViews();
        if (json == null || json.isEmpty()) { hide(sectionId); return; }
        try {
            Type type = new TypeToken<List<CustomCreatureAction>>(){}.getType();
            List<CustomCreatureAction> list = gson.fromJson(json, type);
            if (list == null || list.isEmpty()) { hide(sectionId); return; }
            show(sectionId);
            for (CustomCreatureAction a : list) addActionView(container, a.name, a.desc);
        } catch (Exception e) { hide(sectionId); }
    }

    private void buildActionsFromJson(int actionsSectionId, int actionsContainerId,
                                      int legendarySectionId, int legendaryContainerId,
                                      String json, Gson gson) {
        LinearLayout actionsContainer = findViewById(actionsContainerId);
        LinearLayout legendaryContainer = findViewById(legendaryContainerId);
        actionsContainer.removeAllViews();
        legendaryContainer.removeAllViews();

        if (json == null || json.isEmpty()) {
            hide(actionsSectionId); hide(legendarySectionId); return;
        }
        try {
            Type type = new TypeToken<List<CustomCreatureAction>>(){}.getType();
            List<CustomCreatureAction> list = gson.fromJson(json, type);
            if (list == null || list.isEmpty()) {
                hide(actionsSectionId); hide(legendarySectionId); return;
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
            if (hasActions) show(actionsSectionId); else hide(actionsSectionId);
            if (hasLegendary) show(legendarySectionId); else hide(legendarySectionId);
        } catch (Exception e) { hide(actionsSectionId); hide(legendarySectionId); }
    }

    private void addActionView(LinearLayout container, String name, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(8), 0, dp(4));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTypeface(null, Typeface.BOLD_ITALIC);
        tvName.setTextSize(14);
        row.addView(tvName);

        if (desc != null && !desc.isEmpty()) {
            TextView tvDesc = new TextView(this);
            markwon.setMarkdown(tvDesc, desc);
            tvDesc.setTextSize(14);
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
        int m = (score - 10) / 2;
        return m >= 0 ? "+" + m : String.valueOf(m);
    }

    private String joinNonEmpty(String sep, String... parts) {
        return Arrays.stream(parts).filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.joining(sep));
    }

    private String cap(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void setText(int id, String text) {
        ((TextView) findViewById(id)).setText(text != null ? text : "");
    }

    private void setIfNotEmpty(int id, String prefix, String val) {
        if (val != null && !val.isEmpty()) { setText(id, prefix + val); show(id); }
        else hide(id);
    }

    private void show(int id) { findViewById(id).setVisibility(View.VISIBLE); }
    private void hide(int id) { findViewById(id).setVisibility(View.GONE); }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}