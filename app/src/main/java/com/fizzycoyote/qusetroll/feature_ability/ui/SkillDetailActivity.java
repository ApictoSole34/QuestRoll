package com.fizzycoyote.qusetroll.feature_ability.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityDto;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class SkillDetailActivity extends BaseActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_detail);

        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("SKILL_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).skillDao()
                .getByKey(key)
                .observe(this, skill -> {
                    if (skill != null) populateUI(skill);
                });
    }

    private void populateUI(SkillEntity skill) {
        ((TextView) findViewById(R.id.tv_skill_name)).setText(skill.name);

        Open5eDatabase.getInstance(this).abilityDao()
                .getByKey(skill.abilityKey)
                .observe(this, ability -> {
                    if (ability != null) {
                        ((com.google.android.material.chip.Chip) findViewById(R.id.chip_ability))
                                .setText(ability.name);
                    }
                });

        TextView tvSource = findViewById(R.id.tv_source);
        if (skill.documentKey != null && !skill.documentKey.isEmpty()) {
            String displayName = formatDocumentName(skill.documentKey);
            tvSource.setText("Source: " + displayName);
            tvSource.setVisibility(View.VISIBLE);
            tvSource.setClickable(true);
            tvSource.setFocusable(true);
            tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
            tvSource.setOnClickListener(v -> {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(skill.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            });
        } else {
            tvSource.setVisibility(View.GONE);
        }

        if (skill.descriptionsJson != null) {
            Type t = new TypeToken<List<AbilityDto.AbilityDescriptionDto>>(){}.getType();
            List<AbilityDto.AbilityDescriptionDto> descs =
                    new Gson().fromJson(skill.descriptionsJson, t);
            buildDescriptions(descs);
        }
    }

    private void buildDescriptions(List<AbilityDto.AbilityDescriptionDto> descs) {
        LinearLayout container = findViewById(R.id.descriptions_container);
        container.removeAllViews();
        if (descs == null) return;

        for (AbilityDto.AbilityDescriptionDto d : descs) {
            TextView label = new TextView(this);
            label.setText(d.gamesystem != null ? d.gamesystem.toUpperCase() : "");
            label.setTypeface(null, Typeface.BOLD);
            label.setTextSize(12);
            container.addView(label);

            TextView body = new TextView(this);
            markwon.setMarkdown(body, d.desc != null ? d.desc : "");
            body.setTextSize(14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 4, 0, 16);
            body.setLayoutParams(lp);
            container.addView(body);
        }
    }

    private String formatDocumentName(String key) {
        if (key == null) return "Unknown";
        switch (key) {
            case "core": return "Core Rules";
            case "a5e-ag": return "Adventurer's Guide (A5e)";
            case "srd-2014": return "SRD 5.1";
            case "srd-2024": return "SRD 5.2";
            default: return key;
        }
    }
}