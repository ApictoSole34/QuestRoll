package com.murkfeatherstudio.questroll.feature_spell.spell_school.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.murkfeatherstudio.questroll.feature_spell.spell_school.view_model.CustomSpellSchoolCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class CustomSpellSchoolCreateActivity extends BaseActivity {
    public static final String EXTRA_EDIT_ID = "edit_spell_school_id";
    private CustomSpellSchoolCreateViewModel viewModel;
    private TextInputEditText etName, etDesc;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_spell_school_create);
        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomSpellSchoolCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customSpellSchoolDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomSpellSchoolCreateViewModel.class);
        initViews();
        setupObservers();
        setTitle(editId == -1 ? "Create Spell School" : "Edit Spell School");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, school -> {
            if (school == null) return;
            etName.setText(school.name);
            etDesc.setText(school.description);
        });
        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Spell school saved!" : "Spell school updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A spell school with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) { etName.setError("Required"); return; }
        CustomSpellSchoolEntity entity = new CustomSpellSchoolEntity();
        entity.name = name;
        entity.description = etDesc.getText().toString().trim();
        viewModel.save(entity);
    }
}