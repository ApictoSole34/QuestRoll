package com.murkfeatherstudio.questroll.feature_spell.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.spell.SpellEntity;
import com.murkfeatherstudio.questroll.databinding.ActivitySpellDetailBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;

public class SpellDetailActivity extends BaseActivity {

    private Markwon markwon;
    private ActivitySpellDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpellDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .build();

        String spellKey = getIntent().getStringExtra("SPELL_KEY");
        Open5eDatabase db = Open5eDatabase.getInstance(this);

        db.spellDao().getSpellByKey(spellKey).observe(this, spell -> {
            if (spell != null) populateUI(spell);
        });
    }

    private void populateUI(SpellEntity spell) {
        binding.tvSpellName.setText(spell.name);

        String levelSchool = (spell.level == 0 ? "Cantrip" : "Level " + spell.level)
                + (spell.schoolName != null ? " • " + spell.schoolName : "");
        binding.tvLevelSchool.setText(levelSchool);

        binding.chipRitual.setVisibility(spell.ritual ? View.VISIBLE : View.GONE);
        binding.chipConcentration.setVisibility(spell.concentration ? View.VISIBLE : View.GONE);

        setTextView(binding.tvCastingTime, "Casting Time", spell.castingTime);
        setTextView(binding.tvRange, "Range", spell.rangeText);
        setTextView(binding.tvDuration, "Duration", spell.duration);
        setTextView(binding.tvTarget, "Target", spell.targetType);

        String sourceText = "Source: " + (spell.documentName != null ? spell.documentName : "Unknown");
        binding.tvSource.setText(sourceText);
        binding.tvSource.setVisibility(View.VISIBLE);
        binding.tvSource.setClickable(true);
        binding.tvSource.setFocusable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        binding.tvSource.setOnClickListener(v -> {
            if (spell.documentKey != null && !spell.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(spell.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });

        StringBuilder components = new StringBuilder();
        if (spell.verbal) components.append("V");
        if (spell.somatic) {
            if (components.length() > 0) components.append(", ");
            components.append("S");
        }
        if (spell.material) {
            if (components.length() > 0) components.append(", ");
            components.append("M");
            if (spell.materialSpecified != null && !spell.materialSpecified.isEmpty()) {
                components.append(" (").append(spell.materialSpecified).append(")");
            }
        }
        setTextView(binding.tvComponents, "Components", components.toString());

        if (spell.savingThrowAbility != null && !spell.savingThrowAbility.isEmpty()) {
            setTextView(binding.tvSavingThrow, "Saving Throw",
                    capitalize(spell.savingThrowAbility));
            binding.tvSavingThrow.setVisibility(View.VISIBLE);
        } else {
            binding.tvSavingThrow.setVisibility(View.GONE);
        }

        if (spell.damageRoll != null && !spell.damageRoll.isEmpty()) {
            String damageText = spell.damageRoll;
            if (spell.damageTypes != null && !spell.damageTypes.isEmpty()) {
                damageText += " " + String.join(", ", spell.damageTypes);
            }
            setTextView(binding.tvDamage, "Damage", damageText);
            binding.tvDamage.setVisibility(View.VISIBLE);
        } else {
            binding.tvDamage.setVisibility(View.GONE);
        }

        if (spell.classes != null && !spell.classes.isEmpty()) {
            setTextView(binding.tvClasses, "Classes", String.join(", ", spell.classes));
            binding.tvClasses.setVisibility(View.VISIBLE);
        } else {
            binding.tvClasses.setVisibility(View.GONE);
        }

        if (spell.desc != null && !spell.desc.isEmpty()) {
            markwon.setMarkdown(binding.tvDesc, spell.desc);
        }

        if (spell.higherLevel != null && !spell.higherLevel.isEmpty()) {
            markwon.setMarkdown(binding.tvHigherLevel, spell.higherLevel);
            binding.higherLevelSection.setVisibility(View.VISIBLE);
        } else {
            binding.higherLevelSection.setVisibility(View.GONE);
        }

        buildCastingOptions(spell);
    }

    /**
     * JAVADOC: castingOptionsContainer is a dynamic layout managed via addView(). 
     * Views for different casting options are created programmatically based on 
     * the spell JSON content at runtime. Since these views are not defined in the 
     * static XML layout, View Binding cannot be used to reference them.
     */
    private void buildCastingOptions(SpellEntity spell) {
        if (spell.castingOptionsJson == null || spell.castingOptionsJson.isEmpty()) {
            binding.castingOptionsSection.setVisibility(View.GONE);
            return;
        }

        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> options = gson.fromJson(spell.castingOptionsJson, listType);

            List<Map<String, Object>> meaningfulOptions = new ArrayList<>();
            for (Map<String, Object> option : options) {
                String type = (String) option.get("type");
                if ("default".equals(type)) continue;
                boolean hasData = option.values().stream()
                        .anyMatch(v -> v != null && !"type".equals(v));
                if (hasData) meaningfulOptions.add(option);
            }

            if (meaningfulOptions.isEmpty()) {
                binding.castingOptionsSection.setVisibility(View.GONE);
                return;
            }

            binding.castingOptionsSection.setVisibility(View.VISIBLE);
            binding.castingOptionsContainer.removeAllViews();

            for (Map<String, Object> option : meaningfulOptions) {
                View optionView = buildCastingOptionView(option);
                if (optionView != null) {
                    binding.castingOptionsContainer.addView(optionView);
                }
            }

        } catch (Exception e) {
            binding.castingOptionsSection.setVisibility(View.GONE);
        }
    }

    private View buildCastingOptionView(Map<String, Object> option) {
        String type = (String) option.get("type");
        if (type == null) return null;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView tvType = new TextView(this);
        tvType.setText(formatOptionType(type));
        tvType.setTypeface(null, Typeface.BOLD);
        tvType.setTextSize(14);
        layout.addView(tvType);

        StringBuilder details = new StringBuilder();
        appendIfNotNull(details, "Damage", option.get("damage_roll"));
        appendIfNotNull(details, "Range", option.get("range"));
        appendIfNotNull(details, "Duration", option.get("duration"));
        appendIfNotNull(details, "Targets", option.get("target_count"));
        appendIfNotNull(details, "Description", option.get("desc"));

        if (details.length() > 0) {
            TextView tvDetails = new TextView(this);
            tvDetails.setText(details.toString().trim());
            tvDetails.setTextSize(13);
            layout.addView(tvDetails);
        }

        return layout;
    }

    private String formatOptionType(String type) {
        return type.replace("_", " ").substring(0, 1).toUpperCase()
                + type.replace("_", " ").substring(1);
    }

    private void appendIfNotNull(StringBuilder sb, String label, Object value) {
        if (value != null && !value.toString().isEmpty() && !"null".equals(value.toString())) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(label).append(": ").append(value);
        }
    }

    private void setTextView(TextView tv, String label, String value) {
        if (value != null && !value.isEmpty()) {
            tv.setText(label + ": " + value);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}