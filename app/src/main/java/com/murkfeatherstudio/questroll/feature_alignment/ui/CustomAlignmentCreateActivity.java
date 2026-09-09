package com.murkfeatherstudio.questroll.feature_alignment.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_alignment.CustomAlignmentEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomAlignmentCreateBinding;
import com.murkfeatherstudio.questroll.feature_alignment.view_model.CustomAlignmentCreateViewModel;

import java.util.concurrent.Executors;

public class CustomAlignmentCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_alignment_id";

    private CustomAlignmentCreateViewModel viewModel;
    private ActivityCustomAlignmentCreateBinding binding;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomAlignmentCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomAlignmentCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customAlignmentDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomAlignmentCreateViewModel.class);

        setupObservers();
        setTitle(editId == -1 ? "Create Alignment" : "Edit Alignment");
        binding.btnSave.setText(editId == -1 ? "Save" : "Update");
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, alignment -> {
            if (alignment == null) return;
            binding.etName.setText(alignment.name);
            binding.etShortName.setText(alignment.shortName);
            binding.etMorality.setText(alignment.morality);
            binding.etAttitude.setText(alignment.societalAttitude);
            binding.etDescription.setText(alignment.description);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Alignment saved!" : "Alignment updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "An alignment with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }

        CustomAlignmentEntity entity = new CustomAlignmentEntity();
        entity.name = name;
        entity.shortName = binding.etShortName.getText() != null ? binding.etShortName.getText().toString().trim() : "";
        entity.morality = binding.etMorality.getText() != null ? binding.etMorality.getText().toString().trim() : "";
        entity.societalAttitude = binding.etAttitude.getText() != null ? binding.etAttitude.getText().toString().trim() : "";
        entity.description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";

        viewModel.save(entity);
    }
}