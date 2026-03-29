package com.fizzycoyote.qusetroll.feature_class.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityDao;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.FeatureAdapter;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;
import com.fizzycoyote.qusetroll.feature_class.repository.ClassRepository;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassCreateViewModel;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassCreateViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClassCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_CLASS_ID = "edit_class_id";

    private static final String[] HIT_DICE = {"D6", "D8", "D10", "D12"};
    private static final String[] CASTER_TYPES = {"NONE", "FULL", "HALF", "THIRD", "WARLOCK"};

    private static final CombinedClass NO_PARENT =
            new CombinedClass("", "None (base class)", false, null, null);

    private ClassCreateViewModel viewModel;
    private FeatureAdapter featureAdapter;

    private TextInputEditText etClassName;
    private TextInputEditText etDescription;
    private AutoCompleteTextView actvHitDice;
    private AutoCompleteTextView actvCasterType;
    private AutoCompleteTextView actvSubclass;
    private MaterialButton btnSelectSavingThrows;

    private CombinedClass selectedParentClass = null;

    private final ActivityResultLauncher<Intent> featureEditorLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                        CustomFeatureEntity feature =
                                result.getData().getParcelableExtra(FeatureEditorActivity.EXTRA_FEATURE);
                        int index =
                                result.getData().getIntExtra(FeatureEditorActivity.EXTRA_FEATURE_INDEX, -1);

                        if (feature == null) return;

                        if (index >= 0) {
                            viewModel.updateFeature(index, feature);
                        } else {
                            viewModel.addFeature(feature);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_create);

        setupViewModel();
        initViews();
        setupStaticDropdowns();
        setupRecyclerView();
        setupObservers();
        setupListeners();

        setTitle(viewModel.isEditMode() ? "Edit Class" : "Create Class");
    }

    // ── SETUP

    private void setupViewModel() {
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);
        Executor executor = Executors.newSingleThreadExecutor();

        ClassRepository repository = new ClassRepository(
                open5eDb.characterClassDao(),
                customDb.customCharacterClassDao(),
                executor
        );

        long editClassId = getIntent().getLongExtra(
                EXTRA_EDIT_CLASS_ID, ClassCreateViewModel.NO_ID);

        ClassCreateViewModelFactory factory =
                new ClassCreateViewModelFactory(repository, editClassId);
        viewModel = new ViewModelProvider(this, factory).get(ClassCreateViewModel.class);
    }

    private void initViews() {
        etClassName = findViewById(R.id.etClassName);
        etDescription = findViewById(R.id.etDescription);
        actvHitDice = findViewById(R.id.actvHitDice);
        actvCasterType = findViewById(R.id.actvCasterType);
        actvSubclass = findViewById(R.id.actvSubclass);
        btnSelectSavingThrows = findViewById(R.id.btnSelectSavingThrows);
    }

    private void setupStaticDropdowns() {
        actvHitDice.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, HIT_DICE));
        actvCasterType.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, CASTER_TYPES));

        actvHitDice.setText(HIT_DICE[1], false);
        actvCasterType.setText(CASTER_TYPES[0], false);
    }

    private void setupRecyclerView() {
        RecyclerView rvFeatures = findViewById(R.id.rvFeatures);

        featureAdapter = new FeatureAdapter(new FeatureAdapter.OnFeatureClickListener() {
            @Override
            public void onEdit(CustomFeatureEntity feature, int index) {
                Intent intent = new Intent(ClassCreateActivity.this, FeatureEditorActivity.class);
                intent.putExtra(FeatureEditorActivity.EXTRA_FEATURE, feature);
                intent.putExtra(FeatureEditorActivity.EXTRA_FEATURE_INDEX, index);
                featureEditorLauncher.launch(intent);
            }

            @Override
            public void onDelete(int index) {
                viewModel.removeFeature(index);
            }
        });

        featureAdapter.setDeleteEnabled(true);

        rvFeatures.setLayoutManager(new LinearLayoutManager(this));
        rvFeatures.setAdapter(featureAdapter);
    }

    // ── OBSERVERS

    private void setupObservers() {
        viewModel.getFeatures().observe(this, features ->
                featureAdapter.submitList(new ArrayList<>(features)));

        viewModel.getEditData().observe(this, data -> {
            if (data == null) return;
            populateForm(data.characterClassEntity);
        });

        viewModel.getBaseClasses().observe(this, classes -> {
            List<CombinedClass> options = new ArrayList<>();
            options.add(NO_PARENT);
            options.addAll(classes);

            actvSubclass.setAdapter(buildSubclassAdapter(options));

            if (viewModel.isEditMode() && viewModel.getEditData().getValue() != null) {
                String parentKey = viewModel.getEditData().getValue()
                        .characterClassEntity.subclassOf;
                if (parentKey != null) {
                    options.stream()
                            .filter(c -> c != NO_PARENT && parentKey.equals(c.getKey()))
                            .findFirst()
                            .ifPresent(parent -> {
                                selectedParentClass = parent;
                                actvSubclass.setText(parent.getName(), false);
                            });
                } else {
                    actvSubclass.setText(NO_PARENT.getName(), false);
                }
            } else {
                actvSubclass.setText(NO_PARENT.getName(), false);
                selectedParentClass = null;
            }

            actvSubclass.setOnItemClickListener((parent, view, position, id) -> {
                CombinedClass selected = options.get(position);
                selectedParentClass = selected == NO_PARENT ? null : selected;
                actvSubclass.setText(selected.getName(), false);
            });
        });

        viewModel.getSelectedSavingThrows().observe(this, selected -> {
            int count = selected != null ? selected.size() : 0;
            btnSelectSavingThrows.setText(
                    count == 0 ? "Select Saving Throws" : count + " saving throws selected"
            );
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this,
                        viewModel.isEditMode() ? "Class updated!" : "Class saved!",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this,
                        "A class with this name already exists.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateForm(CustomCharacterClassEntity entity) {
        etClassName.setText(entity.name);
        etDescription.setText(entity.description != null ? entity.description : "");
        if (entity.hitDice != null) actvHitDice.setText(entity.hitDice, false);
        if (entity.casterType != null) actvCasterType.setText(entity.casterType, false);
    }

    // ── LISTENERS

    private void setupListeners() {
        btnSelectSavingThrows.setOnClickListener(v -> showSavingThrowsDialog());

        findViewById(R.id.btnAddFeature).setOnClickListener(v ->
                featureEditorLauncher.launch(new Intent(this, FeatureEditorActivity.class)));

        MaterialButton btnSave = findViewById(R.id.btnSaveClass);
        btnSave.setText(viewModel.isEditMode() ? "Update Class" : "Save Class");
        btnSave.setOnClickListener(v -> saveClass());
    }

    // ── SAVE

    private void saveClass() {
        String name = etClassName.getText() != null
                ? etClassName.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etClassName.setError("Class name is required");
            return;
        }

        String hitDice = actvHitDice.getText().toString().trim();
        if (hitDice.isEmpty()) {
            actvHitDice.setError("Hit dice is required");
            return;
        }

        CustomCharacterClassEntity entity = new CustomCharacterClassEntity();
        entity.name = name;
        entity.hitDice = hitDice;
        entity.description = etDescription.getText() != null
                ? etDescription.getText().toString().trim() : "";
        entity.casterType = actvCasterType.getText().toString().trim();
        entity.subclassOf = selectedParentClass != null
                ? selectedParentClass.getKey() : null;

        Set<String> selectedThrows = viewModel.getSelectedSavingThrows().getValue();
        entity.savingThrows = new ArrayList<>(
                selectedThrows != null ? selectedThrows : new HashSet<>()
        );

        viewModel.saveClass(entity);
    }

    private void showSavingThrowsDialog() {
        Open5eDatabase db = Open5eDatabase.getInstance(this);

        db.abilityDao().getAll().observe(this, abilities -> {
            if (abilities == null) return;

            Set<String> alreadySelected = viewModel.getSelectedSavingThrows().getValue();

            String[] names = new String[abilities.size()];
            boolean[] checkedItems = new boolean[abilities.size()];

            for (int i = 0; i < abilities.size(); i++) {
                AbilityEntity ability = abilities.get(i);
                names[i] = ability.name;
                checkedItems[i] = alreadySelected != null && alreadySelected.contains(ability.key);
            }

            new AlertDialog.Builder(this)
                    .setTitle("Select Saving Throws")
                    .setMultiChoiceItems(names, checkedItems, (dialog, which, isChecked) ->
                            checkedItems[which] = isChecked)
                    .setPositiveButton("OK", (dialog, which) -> {
                        Set<String> result = new HashSet<>();
                        for (int i = 0; i < abilities.size(); i++) {
                            if (checkedItems[i]) {
                                result.add(abilities.get(i).key);
                            }
                        }
                        viewModel.setSelectedSavingThrows(result);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
    // ── HELPERS

    private ArrayAdapter<CombinedClass> buildSubclassAdapter(List<CombinedClass> options) {
        return new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
    }
}