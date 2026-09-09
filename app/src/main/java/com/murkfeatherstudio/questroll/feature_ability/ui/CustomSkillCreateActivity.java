package com.murkfeatherstudio.questroll.feature_ability.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomAbilityDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomAbilityEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityDao;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomSkillCreateBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class CustomSkillCreateActivity extends BaseActivity {

    private CustomSkillDao skillDao;
    private AbilityDao abilityDao;
    private CustomAbilityDao customAbilityDao;
    private Executor executor;

    private ActivityCustomSkillCreateBinding binding;

    private long editId = -1;
    private CustomSkillEntity editing;

    private final List<String>  abilityKeys     = new ArrayList<>();
    private final List<String>  abilityNames    = new ArrayList<>();
    private final List<Boolean> abilityIsCustom = new ArrayList<>();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomSkillCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserContentDatabase udb = UserContentDatabase.getInstance(this);
        Open5eDatabase o5  = Open5eDatabase.getInstance(this);
        skillDao         = udb.customSkillDao();
        customAbilityDao = udb.customAbilityDao();
        abilityDao       = o5.abilityDao();
        executor         = o5.getQueryExecutor();

        editId = getIntent().getLongExtra("CUSTOM_SKILL_ID", -1);

        loadAbilities(() -> {
            if (editId != -1) {
                setTitle("Edit Skill");
                skillDao.getById(editId).observe(this, e -> {
                    if (e != null && editing == null) {
                        editing = e;
                        binding.etSkillName.setText(e.name);
                        binding.etSkillDescription.setText(e.description);
                        for (int i = 0; i < abilityKeys.size(); i++) {
                            if (abilityKeys.get(i).equals(e.abilityKey)
                                    && abilityIsCustom.get(i) == e.parentIsCustom) {
                                binding.spinnerAbility.setSelection(i);
                                break;
                            }
                        }
                    }
                });
            } else {
                setTitle("New Custom Skill");
                String presetKey      = getIntent().getStringExtra("PRESET_ABILITY_KEY");
                boolean presetCustom  = getIntent().getBooleanExtra("PRESET_ABILITY_IS_CUSTOM", false);
                if (presetKey != null) {
                    for (int i = 0; i < abilityKeys.size(); i++) {
                        if (abilityKeys.get(i).equals(presetKey)
                                && abilityIsCustom.get(i) == presetCustom) {
                            binding.spinnerAbility.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });

        binding.btnSave.setOnClickListener(v -> save());
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private void loadAbilities(Runnable onDone) {
        final boolean[] o5Done  = {false};
        final boolean[] cusDone = {false};

        abilityDao.getAll().observe(this, list -> {
            if (o5Done[0]) return;
            o5Done[0] = true;
            for (AbilityEntity e : list) {
                abilityKeys.add(e.key);
                abilityNames.add(e.name);
                abilityIsCustom.add(false);
            }
            if (cusDone[0]) rebuildSpinner(onDone);
        });

        customAbilityDao.getAll().observe(this, list -> {
            if (cusDone[0]) return;
            cusDone[0] = true;
            for (CustomAbilityEntity e : list) {
                abilityKeys.add(String.valueOf(e.id));
                abilityNames.add(e.name + " ★");
                abilityIsCustom.add(true);
            }
            if (o5Done[0]) rebuildSpinner(onDone);
        });
    }

    private void rebuildSpinner(Runnable onDone) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, abilityNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAbility.setAdapter(adapter);
        if (onDone != null) onDone.run();
    }

    private void save() {
        String name = binding.etSkillName.getText() != null ? binding.etSkillName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etSkillName.setError("Name required");
            return;
        }

        int pos = binding.spinnerAbility.getSelectedItemPosition();
        if (pos < 0 || pos >= abilityKeys.size()) {
            Toast.makeText(this, "Select an ability", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            CustomSkillEntity entity = editing != null ? editing : new CustomSkillEntity();
            entity.name          = name;
            entity.description   = binding.etSkillDescription.getText() != null ? binding.etSkillDescription.getText().toString().trim() : "";
            entity.abilityKey    = abilityKeys.get(pos);
            entity.parentIsCustom = abilityIsCustom.get(pos);
            entity.abilityName   = abilityNames.get(pos).replace(" ★", "");
            if (editing == null) entity.createdAt = System.currentTimeMillis();

            if (editing != null) skillDao.update(entity);
            else                 skillDao.insert(entity);

            runOnUiThread(this::finish);
        });
    }
}