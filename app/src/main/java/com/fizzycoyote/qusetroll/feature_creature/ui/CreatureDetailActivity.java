package com.fizzycoyote.qusetroll.feature_creature.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureDto;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;

public class CreatureDetailActivity extends AppCompatActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creature_detail);

        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("CREATURE_KEY");
        Open5eDatabase.getInstance(this).creatureDao()
                .getByKey(key)
                .observe(this, creature -> {
                    if (creature != null) populateUI(creature);
                });
    }

    private void populateUI(CreatureEntity c) {
        Gson gson = new Gson();

        setText(R.id.tv_creature_name, c.name);
        setText(R.id.tv_size_type_alignment,
                join(" ", c.sizeName, c.typeName, "•", c.alignment));
        setText(R.id.tv_cr, "CR " + (c.challengeRatingText != null
                ? c.challengeRatingText : "?")
                + " (" + c.experiencePoints + " XP)");

        setText(R.id.tv_ac, "Armor Class: " + c.armorClass
                + (c.armorDetail != null && !c.armorDetail.isEmpty()
                ? " (" + c.armorDetail + ")" : ""));
        setText(R.id.tv_hp, "Hit Points: " + c.hitPoints
                + (c.hitDice != null ? " (" + c.hitDice + ")" : ""));

        buildSpeedText(c.speedJson);

        setStatBlock(c);

        buildKeyValueSection(R.id.tv_saving_throws, "Saving Throws", c.savingThrowsJson, true);

        buildKeyValueSection(R.id.tv_skills, "Skills", c.skillBonusesJson, true);

        buildSenses(c);

        if (c.languages != null && !c.languages.isEmpty()) {
            setText(R.id.tv_languages, "Languages: " + c.languages);
            show(R.id.tv_languages);
        } else {
            hide(R.id.tv_languages);
        }

        setIfNotEmpty(R.id.tv_damage_immunities, "Damage Immunities: ", c.damageImmunities);
        setIfNotEmpty(R.id.tv_damage_resistances, "Damage Resistances: ", c.damageResistances);
        setIfNotEmpty(R.id.tv_damage_vulnerabilities, "Damage Vulnerabilities: ", c.damageVulnerabilities);
        setIfNotEmpty(R.id.tv_condition_immunities, "Condition Immunities: ", c.conditionImmunities);

        buildTraits(c.traitsJson, gson);

        buildActions(c.actionsJson, gson);

        setIfNotEmpty(R.id.tv_source, "Source: ", c.documentName);
    }

    private void buildSpeedText(String speedJson) {
        if (speedJson == null) { hide(R.id.tv_speed); return; }
        try {
            Gson gson = new Gson();
            CreatureDto.CreatureSpeedDto speed = gson.fromJson(speedJson,
                    CreatureDto.CreatureSpeedDto.class);
            StringBuilder sb = new StringBuilder("Speed: ");
            if (speed.walk != null && speed.walk > 0)
                sb.append((int)(float)speed.walk).append(" ft.");
            if (speed.fly != null && speed.fly > 0)
                sb.append(", fly ").append((int)(float)speed.fly).append(" ft.");
            if (Boolean.TRUE.equals(speed.hover)) sb.append(" (hover)");
            if (speed.swim != null && speed.swim > 0)
                sb.append(", swim ").append((int)(float)speed.swim).append(" ft.");
            if (speed.burrow != null && speed.burrow > 0)
                sb.append(", burrow ").append((int)(float)speed.burrow).append(" ft.");
            if (speed.climb != null && speed.climb > 0)
                sb.append(", climb ").append((int)(float)speed.climb).append(" ft.");
            setText(R.id.tv_speed, sb.toString());
            show(R.id.tv_speed);
        } catch (Exception e) {
            hide(R.id.tv_speed);
        }
    }

    private void setStatBlock(CreatureEntity c) {
        setText(R.id.tv_str, String.valueOf(c.str));
        setText(R.id.tv_str_mod, formatMod(c.strMod));
        setText(R.id.tv_dex, String.valueOf(c.dex));
        setText(R.id.tv_dex_mod, formatMod(c.dexMod));
        setText(R.id.tv_con, String.valueOf(c.con));
        setText(R.id.tv_con_mod, formatMod(c.conMod));
        setText(R.id.tv_int, String.valueOf(c.intScore));
        setText(R.id.tv_int_mod, formatMod(c.intMod));
        setText(R.id.tv_wis, String.valueOf(c.wis));
        setText(R.id.tv_wis_mod, formatMod(c.wisMod));
        setText(R.id.tv_cha, String.valueOf(c.cha));
        setText(R.id.tv_cha_mod, formatMod(c.chaMod));
    }

    private void buildSenses(CreatureEntity c) {
        StringBuilder sb = new StringBuilder("Senses: ");
        boolean hasSense = false;
        if (c.darkvisionRange != null && c.darkvisionRange > 0) {
            sb.append("darkvision ").append((int)(float)c.darkvisionRange).append(" ft.");
            hasSense = true;
        }
        if (c.blindsightRange != null && c.blindsightRange > 0) {
            if (hasSense) sb.append(", ");
            sb.append("blindsight ").append((int)(float)c.blindsightRange).append(" ft.");
            hasSense = true;
        }
        if (c.tremorsenseRange != null && c.tremorsenseRange > 0) {
            if (hasSense) sb.append(", ");
            sb.append("tremorsense ").append((int)(float)c.tremorsenseRange).append(" ft.");
            hasSense = true;
        }
        if (c.truesightRange != null && c.truesightRange > 0) {
            if (hasSense) sb.append(", ");
            sb.append("truesight ").append((int)(float)c.truesightRange).append(" ft.");
            hasSense = true;
        }
        if (hasSense) {
            sb.append(", passive Perception ").append(c.passivePerception);
            setText(R.id.tv_senses, sb.toString());
            show(R.id.tv_senses);
        } else {
            setText(R.id.tv_senses, "passive Perception " + c.passivePerception);
            show(R.id.tv_senses);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildKeyValueSection(int viewId, String label,
                                      String json, boolean skipZeroes) {
        if (json == null || json.isEmpty()) { hide(viewId); return; }
        try {
            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(json,
                    new TypeToken<Map<String, Object>>(){}.getType());
            StringBuilder sb = new StringBuilder(label + ": ");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                double val = ((Number) entry.getValue()).doubleValue();
                if (skipZeroes && val == 0) continue;
                if (!first) sb.append(", ");
                sb.append(capitalize(entry.getKey().replace("_", " ")));
                sb.append(" ").append(val > 0 ? "+" : "").append((int) val);
                first = false;
            }
            if (first) { hide(viewId); return; }
            setText(viewId, sb.toString());
            show(viewId);
        } catch (Exception e) {
            hide(viewId);
        }
    }

    private void buildTraits(String traitsJson, Gson gson) {
        LinearLayout traitsContainer = findViewById(R.id.traits_container);
        traitsContainer.removeAllViews();

        if (traitsJson == null || traitsJson.isEmpty()) {
            hide(R.id.traits_section);
            return;
        }

        try {
            Type type = new TypeToken<List<CreatureDto.CreatureTraitDto>>(){}.getType();
            List<CreatureDto.CreatureTraitDto> traits = gson.fromJson(traitsJson, type);
            if (traits == null || traits.isEmpty()) { hide(R.id.traits_section); return; }

            show(R.id.traits_section);
            for (CreatureDto.CreatureTraitDto trait : traits) {
                addTextSection(traitsContainer, trait.name, trait.desc);
            }
        } catch (Exception e) {
            hide(R.id.traits_section);
        }
    }

    private void buildActions(String actionsJson, Gson gson) {
        LinearLayout actionsContainer = findViewById(R.id.actions_container);
        LinearLayout legendaryContainer = findViewById(R.id.legendary_actions_container);
        actionsContainer.removeAllViews();
        legendaryContainer.removeAllViews();

        if (actionsJson == null || actionsJson.isEmpty()) {
            hide(R.id.actions_section);
            hide(R.id.legendary_actions_section);
            return;
        }

        try {
            Type type = new TypeToken<List<CreatureDto.CreatureActionDto>>(){}.getType();
            List<CreatureDto.CreatureActionDto> actions = gson.fromJson(actionsJson, type);
            if (actions == null || actions.isEmpty()) {
                hide(R.id.actions_section);
                hide(R.id.legendary_actions_section);
                return;
            }

            boolean hasActions = false;
            boolean hasLegendary = false;

            for (CreatureDto.CreatureActionDto action : actions) {
                if ("LEGENDARY_ACTION".equals(action.actionType)) {
                    addTextSection(legendaryContainer, action.name, action.desc);
                    hasLegendary = true;
                } else {
                    addTextSection(actionsContainer, action.name, action.desc);
                    hasActions = true;
                }
            }

            if (hasActions) show(R.id.actions_section); else hide(R.id.actions_section);
            if (hasLegendary) show(R.id.legendary_actions_section);
            else hide(R.id.legendary_actions_section);

        } catch (Exception e) {
            hide(R.id.actions_section);
            hide(R.id.legendary_actions_section);
        }
    }

    private void addTextSection(LinearLayout container, String name, String desc) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(0, dpToPx(8), 0, dpToPx(4));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTypeface(null, Typeface.BOLD_ITALIC);
        tvName.setTextSize(14);
        section.addView(tvName);

        if (desc != null && !desc.isEmpty()) {
            TextView tvDesc = new TextView(this);
            markwon.setMarkdown(tvDesc, desc);
            tvDesc.setTextSize(14);
            section.addView(tvDesc);
        }

        container.addView(section);
    }

    private String formatMod(int mod) {
        return mod >= 0 ? "+" + mod : String.valueOf(mod);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String join(String sep, String... parts) {
        return Arrays.stream(parts)
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.joining(sep));
    }

    private void setText(int id, String text) {
        ((TextView) findViewById(id)).setText(text != null ? text : "");
    }

    private void setIfNotEmpty(int id, String prefix, String value) {
        if (value != null && !value.isEmpty()) {
            setText(id, prefix + value);
            show(id);
        } else {
            hide(id);
        }
    }

    private void show(int id) { findViewById(id).setVisibility(View.VISIBLE); }
    private void hide(int id) { findViewById(id).setVisibility(View.GONE); }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}