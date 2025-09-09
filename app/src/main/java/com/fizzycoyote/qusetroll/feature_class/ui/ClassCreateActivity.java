package com.fizzycoyote.qusetroll.feature_class.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClassCreateActivity extends AppCompatActivity {
    private static final int REQUEST_EDIT_FEATURE = 1001;
    private ClassCreateViewModel viewModel;
    private FeatureAdapter adapter;

    private EditText etClassName;
    private Spinner spinnerHitDice;
    private EditText etDescription;
    private Spinner spinnerCasterType;
    private Spinner spinnerSubclass;
    private Button btnSelectSavingThrows;
    private Button btnAddFeature;
    private RecyclerView rvFeatures;
    private Button btnSaveClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_create);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);
        Executor executor = Executors.newSingleThreadExecutor();

        ClassRepository repository = new ClassRepository(
                open5eDb.characterClassDao(),
                customDb.customCharacterClassDao(),
                executor
        );

        ClassCreateViewModelFactory factory = new ClassCreateViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(ClassCreateViewModel.class);


        // Inicjalizacja widoków
        etClassName = findViewById(R.id.etClassName);
        spinnerHitDice = findViewById(R.id.spinnerHitDice);
        etDescription = findViewById(R.id.etDescription);
        spinnerCasterType = findViewById(R.id.spinnerCasterType);
        spinnerSubclass = findViewById(R.id.spinnerSubclass);
        btnSelectSavingThrows = findViewById(R.id.btnSelectSavingThrows);
        btnAddFeature = findViewById(R.id.btnAddFeature);
        rvFeatures = findViewById(R.id.rvFeatures);
        btnSaveClass = findViewById(R.id.btnSaveClass);

        viewModel = new ViewModelProvider(this).get(ClassCreateViewModel.class);
        setupRecyclerView();
        setupObservers();
        setupListeners();
    }



    private void setupRecyclerView() {
        adapter = new FeatureAdapter();
        rvFeatures.setLayoutManager(new LinearLayoutManager(this));
        rvFeatures.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getFeatures().observe(this, features ->
                adapter.submitList(new ArrayList<>(features)));

        viewModel.getBaseClasses().observe(this, classes -> {
            ArrayAdapter<CombinedClass> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    classes
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSubclass.setAdapter(adapter);
        });
    }

    private void setupListeners() {
        btnSelectSavingThrows.setOnClickListener(v -> showSavingThrowsDialog());

        btnAddFeature.setOnClickListener(v -> {
            CustomFeatureEntity newFeature = new CustomFeatureEntity();
            Intent intent = new Intent(this, FeatureEditorActivity.class);
            intent.putExtra("feature", newFeature);
            startActivityForResult(intent, REQUEST_EDIT_FEATURE);
        });

        btnSaveClass.setOnClickListener(v -> saveClass());
    }



    private void showSavingThrowsDialog() {
        Open5eDatabase db = Open5eDatabase.getInstance(this);
        AbilityDao abilityDao = db.abilityDao();

        // Pobierz dane z bazy w tle
        db.getQueryExecutor().execute(() -> {
            List<AbilityEntity> abilities = abilityDao.getAllAbilitiesSync();
            boolean[] checkedItems = new boolean[abilities.size()];

            // Sprawdź wcześniej wybrane opcje
            Set<String> selected = viewModel.getSelectedSavingThrows().getValue();
            if (selected != null) {
                for (int i = 0; i < abilities.size(); i++) {
                    if (selected.contains(abilities.get(i).key)) {
                        checkedItems[i] = true;
                    }
                }
            }

            String[] abilityNames = new String[abilities.size()];
            for (int i = 0; i < abilities.size(); i++) {
                abilityNames[i] = abilities.get(i).name;
            }

            // Pokaż dialog na głównym wątku
            runOnUiThread(() -> {
                new AlertDialog.Builder(ClassCreateActivity.this)
                        .setTitle("Select Saving Throws")
                        .setMultiChoiceItems(abilityNames, checkedItems, (dialog, which, isChecked) -> {})
                        .setPositiveButton("OK", (d, which) -> {
                            Set<String> selectedKeys = new HashSet<>();
                            ListView listView = ((AlertDialog) d).getListView();
                            for (int i = 0; i < abilities.size(); i++) {
                                if (listView.isItemChecked(i)) {
                                    selectedKeys.add(abilities.get(i).key);
                                }
                            }
                            viewModel.setSelectedSavingThrows(selectedKeys);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_FEATURE && resultCode == RESULT_OK) { // Użyj stałej
            CustomFeatureEntity feature = data.getParcelableExtra("feature");
            viewModel.addFeature(feature);
        }
    }

    private void saveClass() {
        CustomCharacterClassEntity entity = new CustomCharacterClassEntity();
        entity.name = etClassName.getText().toString();
        entity.hitDice = spinnerHitDice.getSelectedItem().toString();
        entity.description = etDescription.getText().toString();
        entity.casterType = spinnerCasterType.getSelectedItem().toString();
        Set<String> selectedThrows = viewModel.getSelectedSavingThrows().getValue();
        entity.savingThrows = new ArrayList<>(selectedThrows != null ? selectedThrows : new HashSet<>());

        CombinedClass parent = (CombinedClass) spinnerSubclass.getSelectedItem();
        if (parent != null) {
            entity.subclassOf = parent.getKey();
        }

        viewModel.saveClass(entity);
        finish();
    }
}
