package com.murkfeatherstudio.questroll.feature_item.item_rarity.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomItemRarityCreateBinding;
import com.murkfeatherstudio.questroll.feature_item.item_rarity.view_model.CustomItemRarityCreateViewModel;

import java.util.concurrent.Executors;

public class CustomItemRarityCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_rarity_id";

    private CustomItemRarityCreateViewModel viewModel;
    private ActivityCustomItemRarityCreateBinding binding;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomItemRarityCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomItemRarityCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customItemRarityDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomItemRarityCreateViewModel.class);

        setupObservers();
        setTitle(editId == -1 ? "Create Rarity" : "Edit Rarity");
        binding.btnSave.setText(editId == -1 ? "Save" : "Update");
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, rarity -> {
            if (rarity == null) return;
            binding.etName.setText(rarity.name);
            binding.etRank.setText(String.valueOf(rarity.rank));
            binding.etDescription.setText(rarity.description);
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
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }
        int rank;
        try {
            String rankStr = binding.etRank.getText() != null ? binding.etRank.getText().toString().trim() : "";
            rank = Integer.parseInt(rankStr);
        } catch (NumberFormatException e) {
            binding.etRank.setError("Enter a number");
            return;
        }

        CustomItemRarityEntity entity = new CustomItemRarityEntity();
        entity.name = name;
        entity.rank = rank;
        entity.description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";

        viewModel.save(entity);
    }
}