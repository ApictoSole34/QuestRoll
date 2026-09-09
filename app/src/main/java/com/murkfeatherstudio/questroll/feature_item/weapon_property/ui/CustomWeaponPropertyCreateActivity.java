package com.murkfeatherstudio.questroll.feature_item.weapon_property.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomWeaponPropertyCreateBinding;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.view_model.CustomWeaponPropertyCreateViewModel;

import java.util.concurrent.Executors;

public class CustomWeaponPropertyCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_property_id";

    private CustomWeaponPropertyCreateViewModel viewModel;
    private ActivityCustomWeaponPropertyCreateBinding binding;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomWeaponPropertyCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomWeaponPropertyCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customWeaponPropertyDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomWeaponPropertyCreateViewModel.class);

        setupObservers();
        setTitle(editId == -1 ? "Create Property" : "Edit Property");
        binding.btnSave.setText(editId == -1 ? "Save" : "Update");
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, prop -> {
            if (prop == null) return;
            binding.etName.setText(prop.name);
            binding.etType.setText(prop.type);
            binding.etDesc.setText(prop.desc);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Property saved!" : "Property updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A property with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }

        CustomWeaponPropertyEntity entity = new CustomWeaponPropertyEntity();
        entity.name = name;
        entity.type = binding.etType.getText() != null ? binding.etType.getText().toString().trim() : "";
        entity.desc = binding.etDesc.getText() != null ? binding.etDesc.getText().toString().trim() : "";

        viewModel.save(entity);
    }
}
