package com.fizzycoyote.qusetroll.feature_service.ui;

import static android.content.Intent.getIntent;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_service.CustomServiceEntity;
import com.fizzycoyote.qusetroll.feature_service.view_model.CustomServiceCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class CustomServiceCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_service_id";

    private CustomServiceCreateViewModel viewModel;
    private TextInputEditText etName, etCost, etDetail, etDesc;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_service_create);

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomServiceCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customServiceDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomServiceCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(editId == -1 ? "Create Service" : "Edit Service");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etCost = findViewById(R.id.etCost);
        etDetail = findViewById(R.id.etDetail);
        etDesc = findViewById(R.id.etDesc);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, service -> {
            if (service == null) return;
            etName.setText(service.name);
            etCost.setText(service.cost);
            etDetail.setText(service.detail);
            etDesc.setText(service.desc);
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
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }

        CustomServiceEntity entity = new CustomServiceEntity();
        entity.name = name;
        entity.cost = etCost.getText().toString().trim();
        entity.detail = etDetail.getText().toString().trim();
        entity.desc = etDesc.getText().toString().trim();

        viewModel.save(entity);
    }
}
