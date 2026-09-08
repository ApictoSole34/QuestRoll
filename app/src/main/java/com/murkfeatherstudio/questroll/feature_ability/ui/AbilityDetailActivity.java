package com.murkfeatherstudio.questroll.feature_ability.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityDto;
import com.murkfeatherstudio.questroll.feature_ability.adapter.SkillAdapter;
import com.murkfeatherstudio.questroll.feature_ability.model.CombinedSkill;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class AbilityDetailActivity extends BaseActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ability_detail);

        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(HtmlPlugin.create())
                .build();

        String key = getIntent().getStringExtra("ABILITY_KEY");

        Open5eDatabase db = Open5eDatabase.getInstance(this);
        UserContentDatabase udb = UserContentDatabase.getInstance(this);

        db.abilityDao().getByKey(key).observe(this, ability -> {
            if (ability == null) return;

            TextView tvName = findViewById(R.id.tv_ability_name);
            tvName.setText(ability.name);
            tvName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
            tvName.setTextColor(getColor(R.color.threads_text_primary));

            TextView tvShort = findViewById(R.id.tv_ability_short_desc);
            tvShort.setText(ability.shortDesc);
            tvShort.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            tvShort.setTextColor(getColor(R.color.threads_text_primary));

            if (ability.descriptionsJson != null) {
                Type t = new TypeToken<List<AbilityDto.AbilityDescriptionDto>>(){}.getType();
                List<AbilityDto.AbilityDescriptionDto> descs =
                        new Gson().fromJson(ability.descriptionsJson, t);
                buildDescriptions(descs);
            }

            TextView tvSource = findViewById(R.id.tv_source);
            String sourceText = "Source: " + (ability.documentUrl != null ? ability.documentUrl : "Unknown");
            tvSource.setText(sourceText);
            tvSource.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            tvSource.setTextColor(getColor(R.color.threads_text_secondary));
            tvSource.setVisibility(View.VISIBLE);
            tvSource.setClickable(true);
            tvSource.setFocusable(true);
            tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
            tvSource.setOnClickListener(v -> {
                if (ability.documentUrl != null && !ability.documentUrl.isEmpty()) {
                    String docKey = extractKeyFromUrl(ability.documentUrl);
                    if (docKey != null) {
                        DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(docKey);
                        fragment.show(getSupportFragmentManager(), "document_detail");
                    }
                }
            });
        });

        RecyclerView rvOpen5e = findViewById(R.id.rv_skills_open5e);
        SkillAdapter open5eAdapter = new SkillAdapter(skill -> {
            Intent i = new Intent(this, SkillDetailActivity.class);
            i.putExtra("SKILL_KEY", skill.key);
            startActivity(i);
        });
        rvOpen5e.setLayoutManager(new LinearLayoutManager(this));
        rvOpen5e.setAdapter(open5eAdapter);

        db.skillDao().getByAbility(key).observe(this, skills -> {
            List<CombinedSkill> combined = skills.stream()
                    .map(s -> new CombinedSkill(s, key))
                    .collect(Collectors.toList());
            open5eAdapter.submitList(combined);
        });

        RecyclerView rvCustom = findViewById(R.id.rv_skills_custom);
        SkillAdapter customAdapter = new SkillAdapter(skill -> {
            Intent i = new Intent(this, CustomSkillDetailActivity.class);
            i.putExtra("CUSTOM_SKILL_ID", skill.customId);
            startActivity(i);
        });
        rvCustom.setLayoutManager(new LinearLayoutManager(this));
        rvCustom.setAdapter(customAdapter);

        udb.customSkillDao().getByAbility(key, false).observe(this, skills -> {
            List<CombinedSkill> combined = skills.stream()
                    .map(CombinedSkill::new)
                    .collect(Collectors.toList());
            customAdapter.submitList(combined);
        });

        FloatingActionButton fab = findViewById(R.id.fab_add_custom_skill);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, CustomSkillCreateActivity.class);
            i.putExtra("PRESET_ABILITY_KEY", key);
            i.putExtra("PRESET_ABILITY_IS_CUSTOM", false);
            i.putExtra("PRESET_ABILITY_NAME",
                    ((TextView) findViewById(R.id.tv_ability_name)).getText().toString());
            startActivity(i);
        });
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

    private String extractKeyFromUrl(String url) {
        if (url == null) return null;
        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = u.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}