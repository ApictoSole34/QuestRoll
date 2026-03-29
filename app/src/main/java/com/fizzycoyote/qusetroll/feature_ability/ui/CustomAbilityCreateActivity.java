package com.fizzycoyote.qusetroll.feature_ability.ui;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomAbilityDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomAbilityEntity;

import java.util.concurrent.Executor;

public class CustomAbilityCreateActivity extends AppCompatActivity {

    private CustomAbilityDao dao;
    private Executor executor;
    private long editId = -1;
    private CustomAbilityEntity editing;

    private EditText etName, etShortDesc, etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_ability_create);

        dao      = UserContentDatabase.getInstance(this).customAbilityDao();
        executor = Open5eDatabase.getInstance(this).getQueryExecutor();

        etName        = findViewById(R.id.et_ability_name);
        etShortDesc   = findViewById(R.id.et_ability_short_desc);
        etDescription = findViewById(R.id.et_ability_description);

        editId = getIntent().getLongExtra("CUSTOM_ABILITY_ID", -1);

        if (editId != -1) {
            setTitle("Edit Ability");
            dao.getById(editId).observe(this, entity -> {
                if (entity != null && editing == null) {
                    editing = entity;
                    etName.setText(entity.name);
                    etShortDesc.setText(entity.shortDesc);
                    etDescription.setText(entity.description);
                }
            });
        } else {
            setTitle("New Custom Ability");
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> save());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }

    private void save() {
        String name  = etName.getText().toString().trim();
        if (name.isEmpty()) { etName.setError("Name required"); return; }

        executor.execute(() -> {
            CustomAbilityEntity entity = editing != null ? editing : new CustomAbilityEntity();
            entity.name        = name;
            entity.shortDesc   = etShortDesc.getText().toString().trim();
            entity.description = etDescription.getText().toString().trim();
            if (editing == null) entity.createdAt = System.currentTimeMillis();

            if (editing != null) dao.update(entity);
            else                 dao.insert(entity);

            runOnUiThread(this::finish);
        });
    }
}