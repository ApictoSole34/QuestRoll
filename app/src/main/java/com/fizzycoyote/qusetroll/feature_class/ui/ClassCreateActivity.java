package com.fizzycoyote.qusetroll.feature_class.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.FeatureAdapter;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.LanguageKeyAdapter;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.SkillOptionAdapter;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;
import com.fizzycoyote.qusetroll.feature_class.repository.ClassRepository;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassCreateViewModel;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassCreateViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class ClassCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_CLASS_ID = "edit_class_id";

    private static final String[] HIT_DICE = {"d6", "d8", "d10", "d12"};
    private static final String[] CASTER_TYPES = {"NONE", "FULL", "HALF", "THIRD", "WARLOCK"};
    private static final String[] SPELLCASTING_ABILITIES = {"NONE", "INT", "WIS", "CHA"};

    private static final CombinedClass NO_PARENT =
            new CombinedClass("", "None (base class)", false, null, null);

    private ClassCreateViewModel viewModel;
    private FeatureAdapter featureAdapter;
    private SkillOptionAdapter skillOptionAdapter;
    private LanguageKeyAdapter languageKeyAdapter;

    private TextInputEditText etClassName;
    private TextInputEditText etDescription;
    private AutoCompleteTextView actvHitDice;
    private AutoCompleteTextView actvCasterType;
    private AutoCompleteTextView actvSubclass;
    private MaterialButton btnSelectSavingThrows;

    private Spinner spinnerGameSystem;
    private Spinner spinnerSpellcastingAbility;
    private EditText etStartingGoldDice;
    private EditText etSkillChoicesCount;
    private EditText etEquipmentDescription;
    private RecyclerView rvSkillOptions;
    private Button btnAddSkillOption;

    private EditText etLanguageChoices;
    private RecyclerView rvLanguageKeys;
    private Button btnAddLanguage;

    private List<String> gameSystemKeys = new ArrayList<>();
    private List<String> gameSystemNames = new ArrayList<>();
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
        setupRecyclers();
        setupObservers();
        setupListeners();
        loadGameSystems();

        setTitle(viewModel.isEditMode() ? "Edit Class" : "Create Class");
    }

    private void setupViewModel() {
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        ClassRepository repository = new ClassRepository(
                open5eDb.characterClassDao(),
                customDb.customCharacterClassDao(),
                Executors.newSingleThreadExecutor()
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

        spinnerGameSystem = findViewById(R.id.spinnerGameSystem);
        spinnerSpellcastingAbility = findViewById(R.id.spinnerSpellcastingAbility);
        etStartingGoldDice = findViewById(R.id.etStartingGoldDice);
        etSkillChoicesCount = findViewById(R.id.etSkillChoicesCount);
        etEquipmentDescription = findViewById(R.id.etEquipmentDescription);
        rvSkillOptions = findViewById(R.id.rvSkillOptions);
        btnAddSkillOption = findViewById(R.id.btnAddSkillOption);

        etLanguageChoices = findViewById(R.id.etLanguageChoices);
        rvLanguageKeys = findViewById(R.id.rvLanguageKeys);
        btnAddLanguage = findViewById(R.id.btnAddLanguage);
    }

    private void setupStaticDropdowns() {
        actvHitDice.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, HIT_DICE));
        actvCasterType.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, CASTER_TYPES));
        actvHitDice.setText(HIT_DICE[1], false);
        actvCasterType.setText(CASTER_TYPES[0], false);
    }

    private void setupRecyclers() {
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
        rvFeatures.setNestedScrollingEnabled(false);

        skillOptionAdapter = new SkillOptionAdapter(position -> viewModel.removeSkillOption(position));
        rvSkillOptions.setLayoutManager(new LinearLayoutManager(this));
        rvSkillOptions.setAdapter(skillOptionAdapter);
        rvSkillOptions.setNestedScrollingEnabled(false);

        languageKeyAdapter = new LanguageKeyAdapter(position -> viewModel.removeLanguageKey(position));
        rvLanguageKeys.setLayoutManager(new LinearLayoutManager(this));
        rvLanguageKeys.setAdapter(languageKeyAdapter);
        rvLanguageKeys.setNestedScrollingEnabled(false);
    }

    private void setupObservers() {
        viewModel.getFeatures().observe(this, features ->
                featureAdapter.submitList(new ArrayList<>(features)));

        viewModel.getSkillOptions().observe(this, options ->
                skillOptionAdapter.submitList(options));

        viewModel.getLanguageKeys().observe(this, keys ->
                languageKeyAdapter.submitList(keys));

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

        etStartingGoldDice.setText(entity.startingGoldDice);
        etSkillChoicesCount.setText(String.valueOf(entity.skillChoicesCount));
        etEquipmentDescription.setText(entity.equipmentDescription);
        etLanguageChoices.setText(String.valueOf(entity.languageChoices));

        int pos = gameSystemKeys.indexOf(entity.gameSystem);
        if (pos >= 0) spinnerGameSystem.setSelection(pos);

        List<String> abilities = Arrays.asList(SPELLCASTING_ABILITIES);
        int saPos = abilities.indexOf(entity.spellcastingAbility);
        if (saPos >= 0) spinnerSpellcastingAbility.setSelection(saPos);
    }

    private void setupListeners() {
        btnSelectSavingThrows.setOnClickListener(v -> showSavingThrowsDialog());
        btnAddSkillOption.setOnClickListener(v -> showAddSkillOptionDialog());
        btnAddLanguage.setOnClickListener(v -> showAddLanguageDialog());

        findViewById(R.id.btnAddFeature).setOnClickListener(v ->
                featureEditorLauncher.launch(new Intent(this, FeatureEditorActivity.class)));

        MaterialButton btnSave = findViewById(R.id.btnSaveClass);
        btnSave.setText(viewModel.isEditMode() ? "Update Class" : "Save Class");
        btnSave.setOnClickListener(v -> saveClass());
    }

    private void loadGameSystems() {
        new Thread(() -> {
            List<GameSystemEntity> systems = Open5eDatabase.getInstance(this)
                    .gameSystemDao().getAllGameSystems();
            if (systems == null || systems.isEmpty()) {
                GameSystemEntity fallback = new GameSystemEntity();
                fallback.key = "5e-2014";
                fallback.name = "D&D 5e (2014 Rules)";
                systems = List.of(fallback);
            }
            gameSystemKeys.clear();
            gameSystemNames.clear();
            for (GameSystemEntity gs : systems) {
                gameSystemKeys.add(gs.key);
                gameSystemNames.add(gs.name);
            }
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, gameSystemNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGameSystem.setAdapter(adapter);

                ArrayAdapter<String> saAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, SPELLCASTING_ABILITIES);
                saAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSpellcastingAbility.setAdapter(saAdapter);
            });
        }).start();
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

    private void showAddSkillOptionDialog() {
        new Thread(() -> {
            List<SkillEntity> standardSkills = Open5eDatabase.getInstance(this)
                    .skillDao().getAllSync();
            List<CustomSkillEntity> customSkills = UserContentDatabase.getInstance(this)
                    .customSkillDao().getAllSync();
            List<Object> all = new ArrayList<>();
            all.addAll(standardSkills);
            all.addAll(customSkills);
            runOnUiThread(() -> showSearchableListDialog("Select Skill", all, selected -> {
                String key = (selected instanceof SkillEntity) ?
                        ((SkillEntity) selected).key :
                        "custom_" + ((CustomSkillEntity) selected).id;
                viewModel.addSkillOption(key);
            }));
        }).start();
    }

    private void showAddLanguageDialog() {
        new Thread(() -> {
            List<LanguageEntity> standardLangs = Open5eDatabase.getInstance(this)
                    .languageDao().getAllSync();
            List<CustomLanguageEntity> customLangs = UserContentDatabase.getInstance(this)
                    .customLanguageDao().getAll();
            List<Object> all = new ArrayList<>();
            all.addAll(standardLangs);
            all.addAll(customLangs);
            runOnUiThread(() -> showSearchableListDialog("Select Language", all, selected -> {
                String key = (selected instanceof LanguageEntity) ?
                        ((LanguageEntity) selected).key :
                        "custom_" + ((CustomLanguageEntity) selected).id;
                viewModel.addLanguageKey(key);
            }));
        }).start();
    }

    private <T> void showSearchableListDialog(String title, List<T> items, java.util.function.Consumer<T> onSelect) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_searchable_list, null);
        EditText searchInput = dialogView.findViewById(R.id.search_input);
        android.widget.ListView listView = dialogView.findViewById(R.id.list_view);

        List<T> filteredItems = new ArrayList<>(items);
        ArrayAdapter<T> adapter = new ArrayAdapter<T>(this, android.R.layout.simple_list_item_1, filteredItems) {
            @androidx.annotation.NonNull
            @Override
            public View getView(int position, View convertView, @androidx.annotation.NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                T item = getItem(position);
                String display = "";
                if (item instanceof SkillEntity) display = ((SkillEntity) item).name;
                else if (item instanceof CustomSkillEntity) display = ((CustomSkillEntity) item).name;
                else if (item instanceof LanguageEntity) display = ((LanguageEntity) item).name;
                else if (item instanceof CustomLanguageEntity) display = ((CustomLanguageEntity) item).name;
                tv.setText(display);
                return tv;
            }
        };
        listView.setAdapter(adapter);

        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String query = s.toString().toLowerCase();
                filteredItems.clear();
                for (T item : items) {
                    String name = "";
                    if (item instanceof SkillEntity) name = ((SkillEntity) item).name;
                    else if (item instanceof CustomSkillEntity) name = ((CustomSkillEntity) item).name;
                    else if (item instanceof LanguageEntity) name = ((LanguageEntity) item).name;
                    else if (item instanceof CustomLanguageEntity) name = ((CustomLanguageEntity) item).name;
                    if (name.toLowerCase().contains(query)) filteredItems.add(item);
                }
                adapter.notifyDataSetChanged();
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            T selected = filteredItems.get(position);
            onSelect.accept(selected);
            builder.create().dismiss();
        });
        builder.setView(dialogView).setNegativeButton("Cancel", null).show();
    }

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

        if (spinnerGameSystem.getSelectedItemPosition() >= 0) {
            entity.gameSystem = gameSystemKeys.get(spinnerGameSystem.getSelectedItemPosition());
        }
        entity.spellcastingAbility = (String) spinnerSpellcastingAbility.getSelectedItem();
        entity.startingGoldDice = etStartingGoldDice.getText().toString().trim();
        try {
            entity.skillChoicesCount = Integer.parseInt(etSkillChoicesCount.getText().toString());
        } catch (NumberFormatException e) {
            entity.skillChoicesCount = 0;
        }
        entity.equipmentDescription = etEquipmentDescription.getText().toString().trim();

        int languageChoices = 0;
        try {
            languageChoices = Integer.parseInt(etLanguageChoices.getText().toString());
        } catch (NumberFormatException e) {
            languageChoices = 0;
        }

        viewModel.saveClass(entity, languageChoices);
    }

    private ArrayAdapter<CombinedClass> buildSubclassAdapter(List<CombinedClass> options) {
        return new ArrayAdapter<CombinedClass>(this, android.R.layout.simple_list_item_1, options) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setText(options.get(position).getName());
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setText(options.get(position).getName());
                return tv;
            }
        };
    }
}