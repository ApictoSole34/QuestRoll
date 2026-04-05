package com.fizzycoyote.qusetroll.feature_item.item_category.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;
import com.fizzycoyote.qusetroll.feature_item.item_category.view_model.CustomItemCategoryCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executors;

public class CustomItemCategoryCreateActivity extends AppCompatActivity {
    public static final String EXTRA_EDIT_ID = "edit_category_id";
    private CustomItemCategoryCreateViewModel viewModel;
    private TextInputEditText etName, etDesc;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_item_category_create);

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomItemCategoryCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customItemCategoryDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomItemCategoryCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(editId == -1 ? "Create Category" : "Edit Category");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, cat -> {
            if (cat == null) return;
            etName.setText(cat.name);
            etDesc.setText(cat.description);
        });
        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Category saved!" : "Category updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A category with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        CustomItemCategoryEntity entity = new CustomItemCategoryEntity();
        entity.name = name;
        entity.description = etDesc.getText().toString().trim();
        viewModel.save(entity);
    }
}