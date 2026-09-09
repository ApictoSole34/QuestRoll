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
import com.murkfeatherstudio.questroll.databinding.ActivitySkillDetailBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class SkillDetailActivity extends BaseActivity {

    private Markwon markwon;
    private ActivitySkillDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySkillDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.tvSkillName.setText(skill.name);
        binding.tvSkillName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
        binding.tvSkillName.setTextColor(getColor(R.color.threads_text_primary));

        binding.chipAbility.setTextColor(getColor(R.color.threads_text_primary));
        binding.chipAbility.setChipBackgroundColorResource(R.color.threads_surface);

        Open5eDatabase.getInstance(this).abilityDao()
                .getByKey(skill.abilityKey)
                .observe(this, ability -> {
                    if (ability != null) {
                        binding.chipAbility.setText(ability.name);
                        binding.chipAbility.setTypeface(ResourcesCompat.getFont(this, R.font.inter_medium));
                    }
                });

        if (skill.documentKey != null && !skill.documentKey.isEmpty()) {
            String displayName = formatDocumentName(skill.documentKey);
            binding.tvSource.setText("Source: " + displayName);
            binding.tvSource.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            binding.tvSource.setTextColor(getColor(R.color.threads_text_secondary));
            binding.tvSource.setVisibility(View.VISIBLE);
            binding.tvSource.setClickable(true);
            binding.tvSource.setFocusable(true);
            binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
            binding.tvSource.setOnClickListener(v -> {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(skill.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            });
        } else {
            binding.tvSource.setVisibility(View.GONE);
        }

        if (skill.descriptionsJson != null) {
            Type t = new TypeToken<List<AbilityDto.AbilityDescriptionDto>>(){}.getType();
            List<AbilityDto.AbilityDescriptionDto> descs =
                    new Gson().fromJson(skill.descriptionsJson, t);
            buildDescriptions(descs);
        }
    }

    /**
     * NOTE: descriptions_container is managed dynamically (addView()).
     * TextView views for individual game systems are created at runtime
     * based on JSON data. ViewBinding is not applicable to these
     * dynamically generated children.
     */
    private void buildDescriptions(List<AbilityDto.AbilityDescriptionDto> descs) {
        binding.descriptionsContainer.removeAllViews();
        if (descs == null) return;

        for (AbilityDto.AbilityDescriptionDto d : descs) {
            TextView label = new TextView(this);
            label.setText(d.gamesystem != null ? d.gamesystem.toUpperCase() : "");
            label.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_semibold));
            label.setTextSize(14);
            label.setTextColor(getColor(R.color.threads_gold));
            binding.descriptionsContainer.addView(label);

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
            binding.descriptionsContainer.addView(body);
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
