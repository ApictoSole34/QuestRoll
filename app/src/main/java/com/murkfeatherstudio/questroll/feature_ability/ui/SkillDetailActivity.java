package com.murkfeatherstudio.questroll.feature_ability.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityDto;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.skill.SkillEntity;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class SkillDetailActivity extends BaseActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_detail);

        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(HtmlPlugin.create())
                .build();

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
        TextView tvName = findViewById(R.id.tv_skill_name);
        tvName.setText(skill.name);
        tvName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
        tvName.setTextColor(getColor(R.color.threads_text_primary));

        Chip chipAbility = findViewById(R.id.chip_ability);
        chipAbility.setTextColor(getColor(R.color.threads_text_primary));
        chipAbility.setChipBackgroundColorResource(R.color.threads_surface);

        Open5eDatabase.getInstance(this).abilityDao()
                .getByKey(skill.abilityKey)
                .observe(this, ability -> {
                    if (ability != null) {
                        chipAbility.setText(ability.name);
                        chipAbility.setTypeface(ResourcesCompat.getFont(this, R.font.inter_medium));
                    }
                });

        TextView tvSource = findViewById(R.id.tv_source);
        if (skill.documentKey != null && !skill.documentKey.isEmpty()) {
            String displayName = formatDocumentName(skill.documentKey);
            tvSource.setText("Source: " + displayName);
            tvSource.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            tvSource.setTextColor(getColor(R.color.threads_text_secondary));
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
            label.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_semibold));
            label.setTextSize(14);
            label.setTextColor(getColor(R.color.threads_gold));
            container.addView(label);

            TextView body = new TextView(this);
            markwon.setMarkdown(body, d.desc != null ? d.desc : "");
            body.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            body.setTextSize(15);
            body.setTextColor(getColor(R.color.threads_text_primary));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, dp(4), 0, dp(16));
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

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}