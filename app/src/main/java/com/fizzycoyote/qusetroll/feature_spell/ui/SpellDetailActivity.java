package com.fizzycoyote.qusetroll.feature_spell.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;

public class SpellDetailActivity extends AppCompatActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_detail);

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
        ((TextView) findViewById(R.id.tv_spell_name)).setText(spell.name);

        String levelSchool = (spell.level == 0 ? "Cantrip" : "Level " + spell.level)
                + (spell.schoolName != null ? " • " + spell.schoolName : "");
        ((TextView) findViewById(R.id.tv_level_school)).setText(levelSchool);

        findViewById(R.id.chip_ritual).setVisibility(spell.ritual ? View.VISIBLE : View.GONE);
        findViewById(R.id.chip_concentration).setVisibility(spell.concentration ? View.VISIBLE : View.GONE);

        setTextView(R.id.tv_casting_time, "Casting Time", spell.castingTime);
        setTextView(R.id.tv_range, "Range", spell.rangeText);
        setTextView(R.id.tv_duration, "Duration", spell.duration);
        setTextView(R.id.tv_target, "Target", spell.targetType);

        TextView tvSource = findViewById(R.id.tv_source);
        String sourceText = "Source: " + (spell.documentName != null ? spell.documentName : "Unknown");
        tvSource.setText(sourceText);
        tvSource.setVisibility(View.VISIBLE);
        tvSource.setClickable(true);
        tvSource.setFocusable(true);
        tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        tvSource.setOnClickListener(v -> {
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
        setTextView(R.id.tv_components, "Components", components.toString());

        if (spell.savingThrowAbility != null && !spell.savingThrowAbility.isEmpty()) {
            setTextView(R.id.tv_saving_throw, "Saving Throw",
                    capitalize(spell.savingThrowAbility));
            findViewById(R.id.tv_saving_throw).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tv_saving_throw).setVisibility(View.GONE);
        }

        if (spell.damageRoll != null && !spell.damageRoll.isEmpty()) {
            String damageText = spell.damageRoll;
            if (spell.damageTypes != null && !spell.damageTypes.isEmpty()) {
                damageText += " " + String.join(", ", spell.damageTypes);
            }
            setTextView(R.id.tv_damage, "Damage", damageText);
            findViewById(R.id.tv_damage).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tv_damage).setVisibility(View.GONE);
        }

        if (spell.classes != null && !spell.classes.isEmpty()) {
            setTextView(R.id.tv_classes, "Classes", String.join(", ", spell.classes));
            findViewById(R.id.tv_classes).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tv_classes).setVisibility(View.GONE);
        }

        TextView tvDesc = findViewById(R.id.tv_desc);
        if (spell.desc != null && !spell.desc.isEmpty()) {
            markwon.setMarkdown(tvDesc, spell.desc);
        }

        TextView tvHigherLevel = findViewById(R.id.tv_higher_level);
        LinearLayout higherLevelSection = findViewById(R.id.higher_level_section);
        if (spell.higherLevel != null && !spell.higherLevel.isEmpty()) {
            markwon.setMarkdown(tvHigherLevel, spell.higherLevel);
            higherLevelSection.setVisibility(View.VISIBLE);
        } else {
            higherLevelSection.setVisibility(View.GONE);
        }

        buildCastingOptions(spell);

        if (spell.documentName != null) {
            setTextView(R.id.tv_source, "Source", spell.documentName);
        }
    }

    private void buildCastingOptions(SpellEntity spell) {
        LinearLayout castingOptionsSection = findViewById(R.id.casting_options_section);
        LinearLayout castingOptionsContainer = findViewById(R.id.casting_options_container);

        if (spell.castingOptionsJson == null || spell.castingOptionsJson.isEmpty()) {
            castingOptionsSection.setVisibility(View.GONE);
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
                castingOptionsSection.setVisibility(View.GONE);
                return;
            }

            castingOptionsSection.setVisibility(View.VISIBLE);
            castingOptionsContainer.removeAllViews();

            for (Map<String, Object> option : meaningfulOptions) {
                View optionView = buildCastingOptionView(option);
                if (optionView != null) {
                    castingOptionsContainer.addView(optionView);
                }
            }

        } catch (Exception e) {
            castingOptionsSection.setVisibility(View.GONE);
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

    private void setTextView(int viewId, String label, String value) {
        TextView tv = findViewById(viewId);
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