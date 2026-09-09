package com.murkfeatherstudio.questroll.feature_ability.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomAbilityDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomAbilityEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillDao;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomAbilityDetailBinding;
import com.murkfeatherstudio.questroll.feature_ability.adapter.SkillAdapter;
import com.murkfeatherstudio.questroll.feature_ability.model.CombinedSkill;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;

public class CustomAbilityDetailActivity extends BaseActivity {

    private CustomAbilityDao abilityDao;
    private CustomSkillDao skillDao;
    private Executor executor;
    private CustomAbilityEntity current;
    private Markwon markwon;
    private ActivityCustomAbilityDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomAbilityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        markwon = Markwon.create(this);

        UserContentDatabase udb = UserContentDatabase.getInstance(this);
        abilityDao = udb.customAbilityDao();
        skillDao   = udb.customSkillDao();
        executor   = Open5eDatabase.getInstance(this).getQueryExecutor();

        long id = getIntent().getLongExtra("CUSTOM_ABILITY_ID", -1);

        binding.btnManage.setOnClickListener(this::showManagePopup);

        abilityDao.getById(id).observe(this, ability -> {
            if (ability == null) return;
            current = ability;
            populateUI(ability);
        });

        setupSkillsList(id);
        setupButtons(id);
    }

    private void populateUI(CustomAbilityEntity ability) {
        binding.tvAbilityName.setText(ability.name);
        binding.tvAbilityShortDesc.setText(ability.shortDesc != null ? ability.shortDesc : "");

        if (ability.description != null && !ability.description.isEmpty()) {
            markwon.setMarkdown(binding.tvAbilityDescription, ability.description);
        } else {
            binding.tvAbilityDescription.setText("No description.");
        }
    }

    private void setupSkillsList(long abilityId) {
        SkillAdapter adapter = new SkillAdapter(skill -> {
            Intent i = new Intent(this, CustomSkillDetailActivity.class);
            i.putExtra("CUSTOM_SKILL_ID", skill.customId);
            startActivity(i);
        });

        binding.rvSkillsCustom.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSkillsCustom.setAdapter(adapter);

        skillDao.getByAbility(String.valueOf(abilityId), true)
                .observe(this, skills -> {
                    List<CombinedSkill> list = skills.stream()
                            .map(CombinedSkill::new)
                            .collect(Collectors.toList());

                    adapter.submitList(list);
                    binding.tvNoCustomSkills.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void setupButtons(long abilityId) {
        binding.btnAddCustomSkill.setOnClickListener(v -> {
            Intent i = new Intent(this, CustomSkillCreateActivity.class);
            i.putExtra("PRESET_ABILITY_KEY", String.valueOf(abilityId));
            i.putExtra("PRESET_ABILITY_IS_CUSTOM", true);
            i.putExtra("PRESET_ABILITY_NAME", current != null ? current.name : "");
            startActivity(i);
        });

        binding.fabAddCustomSkill.setOnClickListener(v ->
                binding.btnAddCustomSkill.performClick()
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
