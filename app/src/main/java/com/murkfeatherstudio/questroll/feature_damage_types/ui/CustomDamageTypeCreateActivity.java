package com.murkfeatherstudio.questroll.feature_damage_types.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_damage_types.CustomDamageTypeEntity;
import com.murkfeatherstudio.questroll.feature_damage_types.view_model.CustomDamageTypeCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class CustomDamageTypeCreateActivity extends BaseActivity {
    public static final String EXTRA_EDIT_ID = "edit_damage_type_id";
    private CustomDamageTypeCreateViewModel viewModel;
    private TextInputEditText etName, etDescription;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_damage_type_create);

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomDamageTypeCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customDamageTypeDao(),
                        editId,
                        Executors.newSingleThreadExecutor()))
                .get(CustomDamageTypeCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(editId == -1 ? "Create Damage Type" : "Edit Damage Type");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, type -> {
            if (type == null) return;
            etName.setText(type.name);
            etDescription.setText(type.description);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Damage type saved!" : "Damage type updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A damage type with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) { etName.setError("Required"); return; }

        CustomDamageTypeEntity type = new CustomDamageTypeEntity();
        type.name = name;
        type.description = etDescription.getText().toString().trim();
        viewModel.save(type);
    }
}