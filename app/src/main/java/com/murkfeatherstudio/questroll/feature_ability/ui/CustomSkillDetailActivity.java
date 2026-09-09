package com.murkfeatherstudio.questroll.feature_ability.ui;

import android.os.Bundle;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomSkillDetailBinding;

import java.util.concurrent.Executor;

import io.noties.markwon.Markwon;

public class CustomSkillDetailActivity extends BaseActivity {

    private CustomSkillDao skillDao;
    private Executor executor;
    private CustomSkillEntity current;
    private Markwon markwon;
    private ActivityCustomSkillDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomSkillDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        markwon = Markwon.create(this);
        skillDao = UserContentDatabase.getInstance(this).customSkillDao();
        executor = Open5eDatabase.getInstance(this).getQueryExecutor();

        long id = getIntent().getLongExtra("CUSTOM_SKILL_ID", -1);

        skillDao.getById(id).observe(this, skill -> {
            if (skill == null) return;
            current = skill;
            populateUI(skill);
        });
    }

    private void populateUI(CustomSkillEntity skill) {
        binding.tvSkillName.setText(skill.name);

        binding.tvAbilityName.setText(
                skill.abilityName != null ? skill.abilityName : ""
        );

        if (skill.description != null && !skill.description.isEmpty()) {
            markwon.setMarkdown(binding.tvSkillDescription, skill.description);
        } else {
            binding.tvSkillDescription.setText("No description.");
        }
    }

}