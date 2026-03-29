package com.fizzycoyote.qusetroll.feature_ability.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomAbilityDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomAbilityEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomSkillDao;
import com.fizzycoyote.qusetroll.feature_ability.adapter.SkillAdapter;
import com.fizzycoyote.qusetroll.feature_ability.model.CombinedSkill;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;

public class CustomAbilityDetailActivity extends AppCompatActivity {

    private CustomAbilityDao abilityDao;
    private CustomSkillDao skillDao;
    private Executor executor;
    private CustomAbilityEntity current;
    private Markwon markwon;

    private TextView tvName;
    private TextView tvShortDesc;
    private TextView tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_ability_detail);

        markwon = Markwon.create(this);

        UserContentDatabase udb = UserContentDatabase.getInstance(this);
        abilityDao = udb.customAbilityDao();
        skillDao   = udb.customSkillDao();
        executor   = Open5eDatabase.getInstance(this).getQueryExecutor();

        tvName        = findViewById(R.id.tv_ability_name);
        tvShortDesc   = findViewById(R.id.tv_ability_short_desc);
        tvDescription = findViewById(R.id.tv_ability_description);


        long id = getIntent().getLongExtra("CUSTOM_ABILITY_ID", -1);

        ImageButton btnManage = findViewById(R.id.btn_manage);
        btnManage.setOnClickListener(v -> showManagePopup(v));

        abilityDao.getById(id).observe(this, ability -> {
            if (ability == null) return;
            current = ability;
            populateUI(ability);
        });

        setupSkillsList(id);
        setupButtons(id);
    }

    private void populateUI(CustomAbilityEntity ability) {
        tvName.setText(ability.name);

        tvShortDesc.setText(
                ability.shortDesc != null ? ability.shortDesc : ""
        );

        if (ability.description != null && !ability.description.isEmpty()) {
            markwon.setMarkdown(tvDescription, ability.description);
        } else {
            tvDescription.setText("No description.");
        }
    }

    private void setupSkillsList(long abilityId) {
        RecyclerView rv = findViewById(R.id.rv_skills_custom);

        SkillAdapter adapter = new SkillAdapter(skill -> {
            Intent i = new Intent(this, CustomSkillDetailActivity.class);
            i.putExtra("CUSTOM_SKILL_ID", skill.customId);
            startActivity(i);
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        skillDao.getByAbility(String.valueOf(abilityId), true)
                .observe(this, skills -> {

                    List<CombinedSkill> list = skills.stream()
                            .map(CombinedSkill::new)
                            .collect(Collectors.toList());

                    adapter.submitList(list);

                    findViewById(R.id.tv_no_custom_skills)
                            .setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void setupButtons(long abilityId) {
        findViewById(R.id.btn_add_custom_skill).setOnClickListener(v -> {
            Intent i = new Intent(this, CustomSkillCreateActivity.class);
            i.putExtra("PRESET_ABILITY_KEY", String.valueOf(abilityId));
            i.putExtra("PRESET_ABILITY_IS_CUSTOM", true);
            i.putExtra("PRESET_ABILITY_NAME",
                    current != null ? current.name : "");
            startActivity(i);
        });

        findViewById(R.id.fab_add_custom_skill).setOnClickListener(v ->
                findViewById(R.id.btn_add_custom_skill).performClick()
        );
    }

    private void showManagePopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 0, "Delete");

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == 1) { // Edit
                Intent i = new Intent(this, CustomAbilityCreateActivity.class);
                i.putExtra("CUSTOM_ABILITY_ID", current.id);
                startActivity(i);
                return true;

            } else if (itemId == 2) { // Delete
                new AlertDialog.Builder(this)
                        .setTitle("Delete ability")
                        .setMessage("Delete \"" + current.name + "\"?")
                        .setPositiveButton("Delete", (d, w) ->
                                executor.execute(() -> {
                                    abilityDao.delete(current);
                                    runOnUiThread(this::finish);
                                })
                        )
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });

        popup.show();
    }
}