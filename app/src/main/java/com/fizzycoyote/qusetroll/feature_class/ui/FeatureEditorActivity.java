package com.fizzycoyote.qusetroll.feature_class.ui;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_table_data.CustomTableData;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.LevelAdapter;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.TableAdapter;

public class FeatureEditorActivity extends AppCompatActivity implements LevelAdapter.OnItemClickListener {
    private CustomFeatureEntity feature;
    private LevelAdapter levelAdapter;
    private TableAdapter tableAdapter;
    private EditText etFeatureName;
    private EditText etFeatureDesc;
    private Spinner spinnerFeatureType;
    private RecyclerView rvLevels;
    private RecyclerView rvTableData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_editor);

        Button btnAddLevel = findViewById(R.id.btnAddLevel);
        Button btnAddTableRow = findViewById(R.id.btnAddTableRow);

        etFeatureName = findViewById(R.id.etFeatureName);
        etFeatureDesc = findViewById(R.id.etFeatureDesc);
        spinnerFeatureType = findViewById(R.id.spinnerFeatureType);
        rvLevels = findViewById(R.id.rvLevels);
        rvTableData = findViewById(R.id.rvTableData);

        feature = getIntent().getParcelableExtra("feature");
        if (feature == null) feature = new CustomFeatureEntity();

        btnAddLevel.setOnClickListener(v -> {
            feature.customGainedAt.add(new CustomGainedAt(1, ""));
            levelAdapter.notifyItemInserted(feature.customGainedAt.size() - 1);
        });

        btnAddTableRow.setOnClickListener(v -> {
            feature.customTableData.add(new CustomTableData(1, ""));
            tableAdapter.notifyItemInserted(feature.customTableData.size() - 1);
        });

        setupRecyclers();
        setupForm();
    }

    private void setupForm() {
        etFeatureName.setText(feature.name);
        etFeatureDesc.setText(feature.description);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.feature_types, android.R.layout.simple_spinner_item);
        spinnerFeatureType.setAdapter(adapter);

        if (feature.type != null) {
            int position = adapter.getPosition(feature.type);
            spinnerFeatureType.setSelection(position);
        }
    }

    private int getTypePosition(String type) {
        String[] types = getResources().getStringArray(R.array.feature_types);
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(type)) {
                return i;
            }
        }
        return 0;
    }

    private void setupRecyclers() {
        // LevelAdapter
        levelAdapter = new LevelAdapter(feature.customGainedAt, position -> {
            levelAdapter.removeItem(position);
        });

        // TableAdapter z listenerem
        tableAdapter = new TableAdapter(feature.customTableData, position -> {
            tableAdapter.removeItem(position);
        });

        // Konfiguracja RecyclerView
        rvLevels.setLayoutManager(new LinearLayoutManager(this));
        rvLevels.setAdapter(levelAdapter);

        rvTableData.setLayoutManager(new LinearLayoutManager(this));
        rvTableData.setAdapter(tableAdapter);

        // Wyłącz zagnieżdżone scrollowanie
        rvLevels.setNestedScrollingEnabled(false);
        rvTableData.setNestedScrollingEnabled(false);
    }

    @Override
    public void onDeleteClick(int position) {
        levelAdapter.removeItem(position);
    }

    public void onSaveFeature(View view) {
        feature.name = ((EditText)findViewById(R.id.etFeatureName)).getText().toString();
        feature.description = ((EditText)findViewById(R.id.etFeatureDesc)).getText().toString();
        feature.type = ((Spinner)findViewById(R.id.spinnerFeatureType)).getSelectedItem().toString();

        Intent result = new Intent();
        result.putExtra("feature", feature);
        setResult(RESULT_OK, result);
        finish();
    }
}
