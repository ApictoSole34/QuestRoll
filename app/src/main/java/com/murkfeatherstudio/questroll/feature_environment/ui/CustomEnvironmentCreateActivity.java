package com.murkfeatherstudio.questroll.feature_environment.ui;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_environment.CustomEnvironmentEntity;
import com.murkfeatherstudio.questroll.feature_environment.view_model.CustomEnvironmentCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class CustomEnvironmentCreateActivity extends BaseActivity {
    public static final String EXTRA_EDIT_ID = "edit_environment_id";
    private CustomEnvironmentCreateViewModel viewModel;
    private TextInputEditText etName, etDesc;
    private RadioGroup rgType;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_environment_create);
        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomEnvironmentCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customEnvironmentDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomEnvironmentCreateViewModel.class);
        initViews();
        setupObservers();
        setTitle(editId == -1 ? "Create Environment" : "Edit Environment");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        rgType = findViewById(R.id.rgType);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, env -> {
            if (env == null) return;
            etName.setText(env.name);
            etDesc.setText(env.desc);

            if (env.aquatic) rgType.check(R.id.rbAquatic);
            else if (env.planar) rgType.check(R.id.rbPlanar);
            else if (env.interior) rgType.check(R.id.rbInterior);
            else rgType.check(R.id.rbLand);
        });
        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Environment saved!" : "Environment updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "An environment with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        CustomEnvironmentEntity entity = new CustomEnvironmentEntity();
        entity.name = name;
        entity.desc = etDesc.getText().toString().trim();

        int checkedId = rgType.getCheckedRadioButtonId();
        entity.aquatic = (checkedId == R.id.rbAquatic);
        entity.planar = (checkedId == R.id.rbPlanar);
        entity.interior = (checkedId == R.id.rbInterior);

        viewModel.save(entity);
    }
}