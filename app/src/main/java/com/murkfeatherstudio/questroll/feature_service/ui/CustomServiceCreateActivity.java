package com.murkfeatherstudio.questroll.feature_service.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_service.CustomServiceEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomServiceCreateBinding;
import com.murkfeatherstudio.questroll.feature_service.view_model.CustomServiceCreateViewModel;

import java.util.concurrent.Executors;

public class CustomServiceCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_service_id";

    private CustomServiceCreateViewModel viewModel;
    private ActivityCustomServiceCreateBinding binding;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomServiceCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomServiceCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customServiceDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomServiceCreateViewModel.class);

        setupUI();
        setupObservers();
        
        // Initialize Calculator Drawer Width
        setDrawerWidth(false);
    }

    private void setupUI() {
        setTitle(editId == -1 ? "Create Service" : "Edit Service");
        binding.btnSave.setText(editId == -1 ? "Save" : "Update");
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, service -> {
            if (service == null) return;
            binding.etName.setText(service.name);
            binding.etCost.setText(service.cost);
            binding.etDetail.setText(service.detail);
            binding.etDesc.setText(service.desc);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Service saved!" : "Service updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A service with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }

        CustomServiceEntity entity = new CustomServiceEntity();
        entity.name = name;
        entity.cost = binding.etCost.getText() != null ? binding.etCost.getText().toString().trim() : "";
        entity.detail = binding.etDetail.getText() != null ? binding.etDetail.getText().toString().trim() : "";
        entity.desc = binding.etDesc.getText() != null ? binding.etDesc.getText().toString().trim() : "";

        viewModel.save(entity);
    }
}
