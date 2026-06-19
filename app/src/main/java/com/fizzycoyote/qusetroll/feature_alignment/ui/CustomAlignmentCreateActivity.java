package com.fizzycoyote.qusetroll.feature_alignment.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_alignment.CustomAlignmentEntity;
import com.fizzycoyote.qusetroll.feature_alignment.view_model.CustomAlignmentCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class CustomAlignmentCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_alignment_id";

    private CustomAlignmentCreateViewModel viewModel;
    private TextInputEditText etName, etShortName, etMorality, etAttitude, etDescription;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_alignment_create);

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomAlignmentCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customAlignmentDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomAlignmentCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(editId == -1 ? "Create Alignment" : "Edit Alignment");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etShortName = findViewById(R.id.etShortName);
        etMorality = findViewById(R.id.etMorality);
        etAttitude = findViewById(R.id.etAttitude);
        etDescription = findViewById(R.id.etDescription);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, alignment -> {
            if (alignment == null) return;
            etName.setText(alignment.name);
            etShortName.setText(alignment.shortName);
            etMorality.setText(alignment.morality);
            etAttitude.setText(alignment.societalAttitude);
            etDescription.setText(alignment.description);
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
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }

        CustomAlignmentEntity entity = new CustomAlignmentEntity();
        entity.name = name;
        entity.shortName = etShortName.getText().toString().trim();
        entity.morality = etMorality.getText().toString().trim();
        entity.societalAttitude = etAttitude.getText().toString().trim();
        entity.description = etDescription.getText().toString().trim();

        viewModel.save(entity);
    }
}