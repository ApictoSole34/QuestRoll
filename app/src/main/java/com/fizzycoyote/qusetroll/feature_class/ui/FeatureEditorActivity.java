package com.fizzycoyote.qusetroll.feature_class.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_table_data.CustomTableData;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.LevelAdapter;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.TableAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class FeatureEditorActivity extends AppCompatActivity {

    public static final String EXTRA_FEATURE = "feature";
    public static final String EXTRA_FEATURE_INDEX = "feature_index";

    private static final String[] FEATURE_TYPES = {
            "CLASS_LEVEL_FEATURE",
            "CLASS_TABLE_DATA",
            "CLASS_FEATURE_OPTION_LIST",
            "PROFICIENCIES",
            "PROFICIENCY_BONUS",
            "STARTING_EQUIPMENT",
            "SPELL_SLOTS",
            "CANTRIPS_KNOWN",
            "SPELLS_KNOWN"
    };

    private CustomFeatureEntity feature;
    private LevelAdapter levelAdapter;
    private TableAdapter tableAdapter;

    private TextInputEditText etFeatureName;
    private TextInputEditText etFeatureDesc;
    private AutoCompleteTextView actvFeatureType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_editor);

        feature = getIntent().getParcelableExtra(EXTRA_FEATURE);
        if (feature == null) feature = new CustomFeatureEntity();

        initViews();
        setupTypeDropdown();
        setupRecyclers();
        setupButtons();
        populateForm();
    }

    private void initViews() {
        etFeatureName = findViewById(R.id.etFeatureName);
        etFeatureDesc = findViewById(R.id.etFeatureDesc);
        actvFeatureType = findViewById(R.id.actvFeatureType);
    }

    private void setupTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                FEATURE_TYPES
        );
        actvFeatureType.setAdapter(adapter);
    }

    private void setupRecyclers() {
        RecyclerView rvLevels = findViewById(R.id.rvLevels);
        RecyclerView rvTableData = findViewById(R.id.rvTableData);

        if (feature.customGainedAt == null) feature.customGainedAt = new ArrayList<>();
        if (feature.customTableData == null) feature.customTableData = new ArrayList<>();

        levelAdapter = new LevelAdapter(
                feature.customGainedAt,
                position -> levelAdapter.removeItem(position)
        );

        tableAdapter = new TableAdapter(
                feature.customTableData,
                position -> tableAdapter.removeItem(position)
        );

        rvLevels.setLayoutManager(new LinearLayoutManager(this));
        rvLevels.setAdapter(levelAdapter);
        rvLevels.setNestedScrollingEnabled(false);

        rvTableData.setLayoutManager(new LinearLayoutManager(this));
        rvTableData.setAdapter(tableAdapter);
        rvTableData.setNestedScrollingEnabled(false);
    }

    private void setupButtons() {
        findViewById(R.id.btnAddLevel).setOnClickListener(v -> {
            feature.customGainedAt.add(new CustomGainedAt(1, ""));
            levelAdapter.notifyItemInserted(feature.customGainedAt.size() - 1);
        });

        findViewById(R.id.btnAddTableRow).setOnClickListener(v -> {
            feature.customTableData.add(new CustomTableData(1, ""));
            tableAdapter.notifyItemInserted(feature.customTableData.size() - 1);
        });

        findViewById(R.id.btnSaveFeature).setOnClickListener(v -> saveFeature());
    }

    private void populateForm() {
        if (feature.name != null) etFeatureName.setText(feature.name);
        if (feature.description != null) etFeatureDesc.setText(feature.description);
        if (feature.type != null) actvFeatureType.setText(feature.type, false);
    }

    private void saveFeature() {
        String name = etFeatureName.getText() != null
                ? etFeatureName.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etFeatureName.setError("Feature name is required");
            return;
        }

        String type = actvFeatureType.getText().toString().trim();
        if (type.isEmpty()) {
            actvFeatureType.setError("Feature type is required");
            return;
        }

        feature.name = name;
        feature.description = etFeatureDesc.getText() != null
                ? etFeatureDesc.getText().toString().trim() : "";
        feature.type = type;

        Intent result = new Intent();
        result.putExtra(EXTRA_FEATURE, feature);
        result.putExtra(EXTRA_FEATURE_INDEX,
                getIntent().getIntExtra(EXTRA_FEATURE_INDEX, -1));

        setResult(RESULT_OK, result);
        finish();
    }
}