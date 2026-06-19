package com.fizzycoyote.qusetroll.feature_condition.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_condition.CustomConditionEntity;
import com.fizzycoyote.qusetroll.feature_condition.view_model.CustomConditionCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executors;

public class CustomConditionCreateActivity extends BaseActivity {
    public static final String EXTRA_EDIT_ID = "edit_condition_id";
    private CustomConditionCreateViewModel viewModel;
    private TextInputEditText etName, etDesc;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_condition_create);

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomConditionCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customConditionDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomConditionCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(editId == -1 ? "Create Condition" : "Edit Condition");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, condition -> {
            if (condition == null) return;
            etName.setText(condition.name);
            etDesc.setText(condition.description);
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
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        CustomConditionEntity entity = new CustomConditionEntity();
        entity.name = name;
        entity.description = etDesc.getText().toString().trim();
        viewModel.save(entity);
    }
}