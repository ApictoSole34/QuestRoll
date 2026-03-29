package com.fizzycoyote.qusetroll.feature_ability.ui;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomSkillDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomSkillEntity;

import java.util.concurrent.Executor;

import io.noties.markwon.Markwon;

public class CustomSkillDetailActivity extends AppCompatActivity {

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

        // 🔥 bind view raz (a nie findViewById 10x)
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