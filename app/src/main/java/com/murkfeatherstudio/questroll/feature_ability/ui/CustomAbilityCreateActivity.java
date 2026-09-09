package com.murkfeatherstudio.questroll.feature_ability.ui;

import android.os.Bundle;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomAbilityDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomAbilityEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomAbilityCreateBinding;

import java.util.concurrent.Executor;

public class CustomAbilityCreateActivity extends BaseActivity {

    private CustomAbilityDao dao;
    private Executor executor;
    private long editId = -1;
    private CustomAbilityEntity editing;
    private ActivityCustomAbilityCreateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomAbilityCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dao      = UserContentDatabase.getInstance(this).customAbilityDao();
        executor = Open5eDatabase.getInstance(this).getQueryExecutor();

        editId = getIntent().getLongExtra("CUSTOM_ABILITY_ID", -1);

        if (editId != -1) {
            setTitle("Edit Ability");
            dao.getById(editId).observe(this, entity -> {
                if (entity != null && editing == null) {
                    editing = entity;
                    binding.etAbilityName.setText(entity.name);
                    binding.etAbilityShortDesc.setText(entity.shortDesc);
                    binding.etAbilityDescription.setText(entity.description);
                }
            });
        } else {
            setTitle("New Custom Ability");
        }

        binding.btnSave.setOnClickListener(v -> save());
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private void save() {
        String name  = binding.etAbilityName.getText() != null ? binding.etAbilityName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etAbilityName.setError("Name required");
            return;
        }

        executor.execute(() -> {
            CustomAbilityEntity entity = editing != null ? editing : new CustomAbilityEntity();
            entity.name        = name;
            entity.shortDesc   = binding.etAbilityShortDesc.getText() != null ? binding.etAbilityShortDesc.getText().toString().trim() : "";
            entity.description = binding.etAbilityDescription.getText() != null ? binding.etAbilityDescription.getText().toString().trim() : "";
            if (editing == null) entity.createdAt = System.currentTimeMillis();

            if (editing != null) dao.update(entity);
            else                 dao.insert(entity);

            runOnUiThread(this::finish);
        });
    }
}