package com.murkfeatherstudio.questroll.feature_spell.spell_school.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomSpellSchoolCreateBinding;
import com.murkfeatherstudio.questroll.feature_spell.spell_school.view_model.CustomSpellSchoolCreateViewModel;

import java.util.concurrent.Executors;

public class CustomSpellSchoolCreateActivity extends BaseActivity {
    public static final String EXTRA_EDIT_ID = "edit_spell_school_id";
    private CustomSpellSchoolCreateViewModel viewModel;
    private ActivityCustomSpellSchoolCreateBinding binding;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomSpellSchoolCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomSpellSchoolCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customSpellSchoolDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomSpellSchoolCreateViewModel.class);

        setupObservers();
        setTitle(editId == -1 ? "Create Spell School" : "Edit Spell School");
        binding.btnSave.setText(editId == -1 ? "Save" : "Update");
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, school -> {
            if (school == null) return;
            binding.etName.setText(school.name);
            binding.etDesc.setText(school.description);
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
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }
        CustomSpellSchoolEntity entity = new CustomSpellSchoolEntity();
        entity.name = name;
        entity.description = binding.etDesc.getText() != null ? binding.etDesc.getText().toString().trim() : "";
        viewModel.save(entity);
    }
}
