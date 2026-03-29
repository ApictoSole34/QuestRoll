package com.fizzycoyote.qusetroll.feature_ability.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityDto;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class SkillDetailActivity extends AppCompatActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_detail);

        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("SKILL_KEY");

        Open5eDatabase.getInstance(this).skillDao()
                .getByKey(key)
                .observe(this, skill -> {
                    if (skill == null) return;
                    populateUI(skill);
                });
    }

    private void populateUI(SkillEntity skill) {
        ((TextView) findViewById(R.id.tv_skill_name)).setText(skill.name);

        // Ability chip – load ability name
        Open5eDatabase.getInstance(this).abilityDao()
                .getByKey(skill.abilityKey)
                .observe(this, ability -> {
                    if (ability != null) {
                        ((com.google.android.material.chip.Chip) findViewById(R.id.chip_ability))
                                .setText(ability.name);
                    }
                });

        // Descriptions
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
}