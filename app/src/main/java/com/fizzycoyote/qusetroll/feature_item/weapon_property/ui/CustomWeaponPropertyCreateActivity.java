package com.fizzycoyote.qusetroll.feature_item.weapon_property.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;
import com.fizzycoyote.qusetroll.feature_item.weapon_property.view_model.CustomWeaponPropertyCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class CustomWeaponPropertyCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_property_id";

    private CustomWeaponPropertyCreateViewModel viewModel;
    private TextInputEditText etName, etDesc, etType;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_weapon_property_create);

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomWeaponPropertyCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customWeaponPropertyDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomWeaponPropertyCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(editId == -1 ? "Create Property" : "Edit Property");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etType = findViewById(R.id.etType);
        etDesc = findViewById(R.id.etDesc);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, prop -> {
            if (prop == null) return;
            etName.setText(prop.name);
            etType.setText(prop.type);
            etDesc.setText(prop.desc);
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
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }

        CustomWeaponPropertyEntity entity = new CustomWeaponPropertyEntity();
        entity.name = name;
        entity.type = etType.getText().toString().trim();
        entity.desc = etDesc.getText().toString().trim();

        viewModel.save(entity);
    }
}
