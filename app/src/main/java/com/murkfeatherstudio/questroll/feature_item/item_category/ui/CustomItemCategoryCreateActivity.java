package com.murkfeatherstudio.questroll.feature_item.item_category.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomItemCategoryCreateBinding;
import com.murkfeatherstudio.questroll.feature_item.item_category.view_model.CustomItemCategoryCreateViewModel;
import java.util.concurrent.Executors;

public class CustomItemCategoryCreateActivity extends BaseActivity {
    public static final String EXTRA_EDIT_ID = "edit_category_id";
    private CustomItemCategoryCreateViewModel viewModel;
    private ActivityCustomItemCategoryCreateBinding binding;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomItemCategoryCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomItemCategoryCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customItemCategoryDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomItemCategoryCreateViewModel.class);

        setupObservers();
        setTitle(editId == -1 ? "Create Category" : "Edit Category");
        binding.btnSave.setText(editId == -1 ? "Save" : "Update");
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, cat -> {
            if (cat == null) return;
            binding.etName.setText(cat.name);
            binding.etDesc.setText(cat.description);
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
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }
        CustomItemCategoryEntity entity = new CustomItemCategoryEntity();
        entity.name = name;
        entity.description = binding.etDesc.getText() != null ? binding.etDesc.getText().toString().trim() : "";
        viewModel.save(entity);
    }
}
