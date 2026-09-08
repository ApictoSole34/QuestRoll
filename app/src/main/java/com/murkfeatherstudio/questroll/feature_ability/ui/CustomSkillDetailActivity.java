package com.murkfeatherstudio.questroll.feature_ability.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillEntity;

import java.util.concurrent.Executor;

import io.noties.markwon.Markwon;

public class CustomSkillDetailActivity extends BaseActivity {

    private CustomSkillDao skillDao;
    private Executor executor;
    private CustomSkillEntity current;
    private Markwon markwon;

    private TextView tvName;
    private TextView tvAbility;
    private TextView tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_skill_detail);

        markwon = Markwon.create(this);
        skillDao = UserContentDatabase.getInstance(this).customSkillDao();
        executor = Open5eDatabase.getInstance(this).getQueryExecutor();

        tvName = findViewById(R.id.tv_skill_name);
        tvAbility = findViewById(R.id.tv_ability_name);
        tvDescription = findViewById(R.id.tv_skill_description);

        long id = getIntent().getLongExtra("CUSTOM_SKILL_ID", -1);

        skillDao.getById(id).observe(this, skill -> {
            if (skill == null) return;
            current = skill;
            populateUI(skill);
        });
    }

    private void populateUI(CustomSkillEntity skill) {
        tvName.setText(skill.name);

        tvAbility.setText(
                skill.abilityName != null ? skill.abilityName : ""
        );

        if (skill.description != null && !skill.description.isEmpty()) {
            markwon.setMarkdown(tvDescription, skill.description);
        } else {
            tvDescription.setText("No description.");
        }
    }

}