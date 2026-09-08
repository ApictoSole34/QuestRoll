package com.murkfeatherstudio.questroll.feature_item.item_rarity.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;
import com.murkfeatherstudio.questroll.feature_item.item_rarity.view_model.CustomItemRarityCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class CustomItemRarityCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_rarity_id";

    private CustomItemRarityCreateViewModel viewModel;
    private TextInputEditText etName, etDescription;
    private TextInputEditText etRank;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_item_rarity_create);

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomItemRarityCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customItemRarityDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomItemRarityCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(editId == -1 ? "Create Rarity" : "Edit Rarity");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etRank = findViewById(R.id.etRank);
        etDescription = findViewById(R.id.etDescription);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, rarity -> {
            if (rarity == null) return;
            etName.setText(rarity.name);
            etRank.setText(String.valueOf(rarity.rank));
            etDescription.setText(rarity.description);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Rarity saved!" : "Rarity updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A rarity with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        int rank;
        try {
            rank = Integer.parseInt(etRank.getText().toString().trim());
        } catch (NumberFormatException e) {
            etRank.setError("Enter a number");
            return;
        }

        CustomItemRarityEntity entity = new CustomItemRarityEntity();
        entity.name = name;
        entity.rank = rank;
        entity.description = etDescription.getText().toString().trim();

        viewModel.save(entity);
    }
}