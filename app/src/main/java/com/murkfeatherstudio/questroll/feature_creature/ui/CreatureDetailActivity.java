package com.murkfeatherstudio.questroll.feature_creature.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.creature.CreatureDto;
import com.murkfeatherstudio.questroll.core.models.open5e.creature.CreatureEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCreatureDetailBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class CreatureDetailActivity extends BaseActivity {

    private Markwon markwon;
    private ActivityCreatureDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatureDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Markwon with tables
        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(HtmlPlugin.create())
                .build();

        String key = getIntent().getStringExtra("CREATURE_KEY");
        Open5eDatabase.getInstance(this).creatureDao()
                .getByKey(key)
                .observe(this, creature -> {
                    if (creature != null) populateUI(creature);
                });
    }

    private void populateUI(CreatureEntity c) {
        Gson gson = new Gson();

        binding.tvCreatureName.setText(c.name);
        binding.tvCreatureName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
        binding.tvCreatureName.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.tvSizeTypeAlignment.setText(join(" ", c.sizeName, c.typeName, "•", c.alignment));
        binding.tvSizeTypeAlignment.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvSizeTypeAlignment.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));

        binding.tvCr.setText("CR " + (c.challengeRatingText != null ? c.challengeRatingText : "?")
                + " (" + c.experiencePoints + " XP)");
        binding.tvCr.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvCr.setTextColor(getResources().getColor(R.color.threads_gold, null));

        binding.tvAc.setText("Armor Class: " + c.armorClass
                + (c.armorDetail != null && !c.armorDetail.isEmpty()
                ? " (" + c.armorDetail + ")" : ""));
        binding.tvAc.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvAc.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.tvHp.setText("Hit Points: " + c.hitPoints
                + (c.hitDice != null ? " (" + c.hitDice + ")" : ""));
        binding.tvHp.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvHp.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        buildSpeedText(c.speedJson);
        setStatBlock(c);
        buildKeyValueSection(binding.tvSavingThrows, "Saving Throws", c.savingThrowsJson, true);
        buildKeyValueSection(binding.tvSkills, "Skills", c.skillBonusesJson, true);
        buildSenses(c);

        if (c.languages != null && !c.languages.isEmpty()) {
            binding.tvLanguages.setText("Languages: " + c.languages);
            binding.tvLanguages.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            binding.tvLanguages.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
            binding.tvLanguages.setVisibility(View.VISIBLE);
        } else {
            binding.tvLanguages.setVisibility(View.GONE);
        }

        setIfNotEmpty(binding.tvDamageImmunities, "Damage Immunities: ", c.damageImmunities);
        setIfNotEmpty(binding.tvDamageResistances, "Damage Resistances: ", c.damageResistances);
        setIfNotEmpty(binding.tvDamageVulnerabilities, "Damage Vulnerabilities: ", c.damageVulnerabilities);
        setIfNotEmpty(binding.tvConditionImmunities, "Condition Immunities: ", c.conditionImmunities);

        buildTraits(c.traitsJson, gson);
        buildActions(c.actionsJson, gson);

        String sourceText = "Source: " + (c.documentName != null ? c.documentName : "");
        binding.tvSource.setText(sourceText);
        binding.tvSource.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvSource.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
        binding.tvSource.setVisibility(View.VISIBLE);
        binding.tvSource.setClickable(true);
        binding.tvSource.setFocusable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        binding.tvSource.setOnClickListener(v -> {
            if (c.documentKey != null && !c.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(c.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });
    }

    private void buildSpeedText(String speedJson) {
        if (speedJson == null) { binding.tvSpeed.setVisibility(View.GONE); return; }
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

            binding.tvSpeed.setText(sb.toString());
            binding.tvSpeed.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            binding.tvSpeed.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
            binding.tvSpeed.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            binding.tvSpeed.setVisibility(View.GONE);
        }
    }

    private void setStatBlock(CreatureEntity c) {
        /**
         * JAVADOC: The stat block views (STR, DEX, CON, INT, WIS, CHA and their modifiers) 
         * are accessed directly via binding. These views are part of the static XML layout.
         */
        binding.tvStr.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvStr.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.tvStrMod.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvStrMod.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        
        binding.tvDex.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvDex.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.tvDexMod.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvDexMod.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.tvCon.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvCon.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.tvConMod.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvConMod.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.tvInt.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvInt.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.tvIntMod.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvIntMod.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.tvWis.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvWis.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.tvWisMod.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvWisMod.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.tvCha.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvCha.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.tvChaMod.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvChaMod.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.tvStr.setText(String.valueOf(c.str));
        binding.tvStrMod.setText(formatMod(c.strMod));
        binding.tvDex.setText(String.valueOf(c.dex));
        binding.tvDexMod.setText(formatMod(c.dexMod));
        binding.tvCon.setText(String.valueOf(c.con));
        binding.tvConMod.setText(formatMod(c.conMod));
        binding.tvInt.setText(String.valueOf(c.intScore));
        binding.tvIntMod.setText(formatMod(c.intMod));
        binding.tvWis.setText(String.valueOf(c.wis));
        binding.tvWisMod.setText(formatMod(c.wisMod));
        binding.tvCha.setText(String.valueOf(c.cha));
        binding.tvChaMod.setText(formatMod(c.chaMod));
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
        } else {
            sb = new StringBuilder("passive Perception " + c.passivePerception);
        }

        binding.tvSenses.setText(sb.toString());
        binding.tvSenses.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvSenses.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.tvSenses.setVisibility(View.VISIBLE);
    }

    private void buildKeyValueSection(TextView textView, String label,
                                      String json, boolean skipZeroes) {
        if (json == null || json.isEmpty()) { textView.setVisibility(View.GONE); return; }
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
            if (first) { textView.setVisibility(View.GONE); return; }

            textView.setText(sb.toString());
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            textView.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
            textView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            textView.setVisibility(View.GONE);
        }
    }

    private void buildTraits(String traitsJson, Gson gson) {
        /**
         * JAVADOC: traitsContainer is a dynamic layout managed via addView(). 
         * Text sections are created programmatically based on the creature's trait data 
         * found in the JSON. Since these views are generated at runtime and their 
         * number varies, View Binding is not applicable to these dynamic children.
         */
        binding.traitsContainer.removeAllViews();

        if (traitsJson == null || traitsJson.isEmpty()) {
            binding.traitsSection.setVisibility(View.GONE);
            return;
        }

        try {
            Type type = new TypeToken<List<CreatureDto.CreatureTraitDto>>(){}.getType();
            List<CreatureDto.CreatureTraitDto> traits = gson.fromJson(traitsJson, type);
            if (traits == null || traits.isEmpty()) { binding.traitsSection.setVisibility(View.GONE); return; }

            binding.traitsSection.setVisibility(View.VISIBLE);
            for (CreatureDto.CreatureTraitDto trait : traits) {
                addTextSection(binding.traitsContainer, trait.name, trait.desc);
            }
        } catch (Exception e) {
            binding.traitsSection.setVisibility(View.GONE);
        }
    }

    private void buildActions(String actionsJson, Gson gson) {
        /**
         * JAVADOC: actionsContainer and legendaryActionsContainer are dynamic layouts. 
         * We use removeAllViews() and addTextSection() because the actions and 
         * legendary actions are generated programmatically based on the JSON content 
         * at runtime. View Binding cannot be used for views that do not exist 
         * in the static XML layout.
         */
        binding.actionsContainer.removeAllViews();
        binding.legendaryActionsContainer.removeAllViews();

        if (actionsJson == null || actionsJson.isEmpty()) {
            binding.actionsSection.setVisibility(View.GONE);
            binding.legendaryActionsSection.setVisibility(View.GONE);
            return;
        }

        try {
            Type type = new TypeToken<List<CreatureDto.CreatureActionDto>>(){}.getType();
            List<CreatureDto.CreatureActionDto> actions = gson.fromJson(actionsJson, type);
            if (actions == null || actions.isEmpty()) {
                binding.actionsSection.setVisibility(View.GONE);
                binding.legendaryActionsSection.setVisibility(View.GONE);
                return;
            }

            boolean hasActions = false;
            boolean hasLegendary = false;

            for (CreatureDto.CreatureActionDto action : actions) {
                if ("LEGENDARY_ACTION".equals(action.actionType)) {
                    addTextSection(binding.legendaryActionsContainer, action.name, action.desc);
                    hasLegendary = true;
                } else {
                    addTextSection(binding.actionsContainer, action.name, action.desc);
                    hasActions = true;
                }
            }

            binding.actionsSection.setVisibility(hasActions ? View.VISIBLE : View.GONE);
            binding.legendaryActionsSection.setVisibility(hasLegendary ? View.VISIBLE : View.GONE);

        } catch (Exception e) {
            binding.actionsSection.setVisibility(View.GONE);
            binding.legendaryActionsSection.setVisibility(View.GONE);
        }
    }

    private void addTextSection(LinearLayout container, String name, String desc) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(0, dpToPx(8), 0, dpToPx(4));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
        tvName.setTextColor(getResources().getColor(R.color.threads_gold, null));
        tvName.setTextSize(15);
        section.addView(tvName);

        if (desc != null && !desc.isEmpty()) {
            TextView tvDesc = new TextView(this);
            markwon.setMarkdown(tvDesc, desc);
            tvDesc.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            tvDesc.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
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

    private void setIfNotEmpty(TextView textView, String prefix, String value) {
        if (value != null && !value.isEmpty()) {
            textView.setText(prefix + value);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            textView.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}