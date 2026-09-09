package com.murkfeatherstudio.questroll.feature_condition.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_condition.CustomConditionEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomConditionCreateBinding;
import com.murkfeatherstudio.questroll.feature_condition.view_model.CustomConditionCreateViewModel;

import java.util.concurrent.Executors;

public class CustomConditionCreateActivity extends BaseActivity {
    public static final String EXTRA_EDIT_ID = "edit_condition_id";
    private CustomConditionCreateViewModel viewModel;
    private ActivityCustomConditionCreateBinding binding;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomConditionCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomConditionCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customConditionDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomConditionCreateViewModel.class);

        setupObservers();
        setTitle(editId == -1 ? "Create Condition" : "Edit Condition");
        binding.btnSave.setText(editId == -1 ? "Save" : "Update");
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, condition -> {
            if (condition == null) return;
            binding.etName.setText(condition.name);
            binding.etDesc.setText(condition.description);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Condition saved!" : "Condition updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A condition with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }
        CustomConditionEntity entity = new CustomConditionEntity();
        entity.name = name;
        entity.description = binding.etDesc.getText() != null ? binding.etDesc.getText().toString().trim() : "";
        viewModel.save(entity);
    }
}