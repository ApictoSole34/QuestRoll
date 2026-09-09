package com.murkfeatherstudio.questroll.feature_creature.creature_type.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomCreatureTypeCreateBinding;
import com.murkfeatherstudio.questroll.feature_creature.creature_type.view_model.CustomCreatureTypeCreateViewModel;

import java.util.concurrent.Executors;

public class CustomCreatureTypeCreateActivity extends BaseActivity {
    public static final String EXTRA_EDIT_ID = "edit_creature_type_id";
    private CustomCreatureTypeCreateViewModel viewModel;
    private ActivityCustomCreatureTypeCreateBinding binding;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomCreatureTypeCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomCreatureTypeCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customCreatureTypeDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomCreatureTypeCreateViewModel.class);

        setupObservers();
        setTitle(editId == -1 ? "Create Creature Type" : "Edit Creature Type");
        binding.btnSave.setText(editId == -1 ? "Save" : "Update");
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, type -> {
            if (type == null) return;
            binding.etName.setText(type.name);
            binding.etDesc.setText(type.description);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Creature type saved!" : "Creature type updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A creature type with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }
        CustomCreatureTypeEntity entity = new CustomCreatureTypeEntity();
        entity.name = name;
        entity.description = binding.etDesc.getText() != null ? binding.etDesc.getText().toString().trim() : "";
        viewModel.save(entity);
    }
}