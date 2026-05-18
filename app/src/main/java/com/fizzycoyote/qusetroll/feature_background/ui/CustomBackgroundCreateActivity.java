package com.fizzycoyote.qusetroll.feature_background.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterCreationDTO;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.feature_background.adapter.GenericItemAdapter;
import com.fizzycoyote.qusetroll.feature_background.view_model.CustomBackgroundCreateViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomBackgroundCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_background_id";

    private CustomBackgroundCreateViewModel viewModel;
    private TextInputEditText etName, etDesc;
    private Spinner spinnerGameSystem;
    private EditText etStartingGold;
    private List<GameSystemEntity> gameSystems = new ArrayList<>();

    private GenericItemAdapter<CharacterCreationDTO.InventoryItemDTO> equipmentAdapter;
    private GenericItemAdapter<String> languagesAdapter;
    private GenericItemAdapter<String> skillsAdapter;
    private GenericItemAdapter<String> toolsAdapter;
    private GenericItemAdapter<CharacterTraitEntity> featuresAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_background_create);

        long editId = getIntent().getLongExtra(EXTRA_EDIT_ID, CustomBackgroundCreateViewModel.NO_ID);
        viewModel = new ViewModelProvider(this,
                new CustomBackgroundCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customBackgroundDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomBackgroundCreateViewModel.class);

        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        spinnerGameSystem = findViewById(R.id.spinnerGameSystem);
        etStartingGold = findViewById(R.id.etStartingGold);

        loadGameSystems();
        setupRecyclerViews();

        viewModel.getEditData().observe(this, entity -> {
            if (entity != null) {
                etName.setText(entity.name);
                etDesc.setText(entity.desc);
                etStartingGold.setText(String.valueOf(entity.startingGold));
                selectGameSystem(entity.gameSystem);
                equipmentAdapter.setItems(viewModel.getEquipmentItems());
                languagesAdapter.setItems(viewModel.getLanguageItems());
                skillsAdapter.setItems(viewModel.getSkillItems());
                toolsAdapter.setItems(viewModel.getToolItems());
                featuresAdapter.setItems(viewModel.getFeatureItems());
            }
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, viewModel.isEditMode() ? "Updated" : "Saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Name already exists", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnAddEquipment).setOnClickListener(v -> showAddItemDialog("Equipment", equipmentAdapter, viewModel::addEquipmentItem));
        findViewById(R.id.btnAddLanguage).setOnClickListener(v -> showAddLanguageDialog());
        findViewById(R.id.btnAddSkill).setOnClickListener(v -> showAddSkillDialog());
        findViewById(R.id.btnAddTool).setOnClickListener(v -> showAddStringDialog("Tool", toolsAdapter, viewModel::addTool, viewModel::getToolItems));
        findViewById(R.id.btnAddFeature).setOnClickListener(v -> showAddFeatureDialog());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void loadGameSystems() {
        new Thread(() -> {
            List<GameSystemEntity> systems = Open5eDatabase.getInstance(this)
                    .gameSystemDao()
                    .getAllGameSystems();
            if (systems == null || systems.isEmpty()) {
                GameSystemEntity fallback2014 = new GameSystemEntity();
                fallback2014.key = "5e-2014";
                fallback2014.name = "D&D 5e (2014 Rules)";
                GameSystemEntity fallback2024 = new GameSystemEntity();
                fallback2024.key = "5e-2024";
                fallback2024.name = "D&D 5e (2024 Rules)";
                systems = List.of(fallback2014, fallback2024);
            }
            gameSystems.clear();
            gameSystems.addAll(systems);
            runOnUiThread(() -> setupSpinner());
        }).start();
    }

    private void setupSpinner() {
        List<String> systemNames = new ArrayList<>();
        for (GameSystemEntity gs : gameSystems) {
            systemNames.add(gs.name != null ? gs.name : gs.key);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, systemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGameSystem.setAdapter(adapter);
    }

    private void selectGameSystem(String systemKey) {
        if (systemKey == null) return;
        for (int i = 0; i < gameSystems.size(); i++) {
            if (gameSystems.get(i).key.equals(systemKey)) {
                spinnerGameSystem.setSelection(i);
                break;
            }
        }
    }

    private void setupRecyclerViews() {
        equipmentAdapter = new GenericItemAdapter<>(
                item -> {
                    viewModel.removeEquipmentItem(item);
                    equipmentAdapter.setItems(viewModel.getEquipmentItems());
                },
                item -> item.customName + " (x" + item.quantity + ", " + item.customWeight + " lb)"
        );
        languagesAdapter = new GenericItemAdapter<>(
                item -> {
                    viewModel.removeLanguage(item);
                    languagesAdapter.setItems(viewModel.getLanguageItems());
                },
                item -> item
        );
        skillsAdapter = new GenericItemAdapter<>(
                item -> {
                    viewModel.removeSkill(item);
                    skillsAdapter.setItems(viewModel.getSkillItems());
                },
                item -> item
        );
        toolsAdapter = new GenericItemAdapter<>(
                item -> {
                    viewModel.removeTool(item);
                    toolsAdapter.setItems(viewModel.getToolItems());
                },
                item -> item
        );
        featuresAdapter = new GenericItemAdapter<>(
                item -> {
                    viewModel.removeFeature(item);
                    featuresAdapter.setItems(viewModel.getFeatureItems());
                },
                item -> item.name
        );

        ((RecyclerView) findViewById(R.id.rvEquipment)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.rvEquipment)).setAdapter(equipmentAdapter);
        ((RecyclerView) findViewById(R.id.rvLanguages)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.rvLanguages)).setAdapter(languagesAdapter);
        ((RecyclerView) findViewById(R.id.rvSkillProficiencies)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.rvSkillProficiencies)).setAdapter(skillsAdapter);
        ((RecyclerView) findViewById(R.id.rvToolProficiencies)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.rvToolProficiencies)).setAdapter(toolsAdapter);
        ((RecyclerView) findViewById(R.id.rvFeatures)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.rvFeatures)).setAdapter(featuresAdapter);
    }

    private void showAddItemDialog(String title, GenericItemAdapter<CharacterCreationDTO.InventoryItemDTO> adapter,
                                   java.util.function.Consumer<CharacterCreationDTO.InventoryItemDTO> addCallback) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        EditText etName = view.findViewById(R.id.item_name);
        EditText etQty = view.findViewById(R.id.item_quantity);
        EditText etWeight = view.findViewById(R.id.item_weight);
        new AlertDialog.Builder(this)
                .setTitle("Add " + title)
                .setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    int qty = Integer.parseInt(etQty.getText().toString());
                    float weight = Float.parseFloat(etWeight.getText().toString());
                    CharacterCreationDTO.InventoryItemDTO item = new CharacterCreationDTO.InventoryItemDTO();
                    item.customName = name;
                    item.quantity = qty;
                    item.customWeight = weight;
                    addCallback.accept(item);
                    adapter.setItems(viewModel.getEquipmentItems());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddStringDialog(String title, GenericItemAdapter<String> adapter,
                                     java.util.function.Consumer<String> addCallback,
                                     java.util.function.Supplier<java.util.List<String>> listGetter) {
        EditText input = new EditText(this);
        input.setHint("Name");
        new AlertDialog.Builder(this)
                .setTitle("Add " + title)
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (!value.isEmpty()) {
                        addCallback.accept(value);
                        adapter.setItems(listGetter.get());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddLanguageDialog() {
        new Thread(() -> {
            List<LanguageEntity> standardLanguages = Open5eDatabase.getInstance(this)
                    .languageDao()
                    .getAllSync();

            List<CustomLanguageEntity> customLanguages = UserContentDatabase.getInstance(this)
                    .customLanguageDao()
                    .getAll();

            List<Object> allLanguages = new ArrayList<>();
            allLanguages.addAll(standardLanguages);
            allLanguages.addAll(customLanguages);

            runOnUiThread(() -> showSearchableListDialog("Select Language", allLanguages, selected -> {
                String langName = null;
                if (selected instanceof LanguageEntity) {
                    langName = ((LanguageEntity) selected).name;
                } else if (selected instanceof CustomLanguageEntity) {
                    langName = ((CustomLanguageEntity) selected).name;
                }
                if (langName != null && !langName.isEmpty()) {
                    viewModel.addLanguage(langName);
                    languagesAdapter.setItems(viewModel.getLanguageItems());
                }
            }));
        }).start();
    }

    private void showAddSkillDialog() {
        new Thread(() -> {
            List<SkillEntity> standardSkills = Open5eDatabase.getInstance(this)
                    .skillDao()
                    .getAllSync();

            List<CustomSkillEntity> customSkills = UserContentDatabase.getInstance(this)
                    .customSkillDao()
                    .getAllSync();

            List<Object> allSkills = new ArrayList<>();
            allSkills.addAll(standardSkills);
            allSkills.addAll(customSkills);

            runOnUiThread(() -> showSearchableListDialog("Select Skill", allSkills, selected -> {
                String skillName = null;
                if (selected instanceof SkillEntity) {
                    skillName = ((SkillEntity) selected).name;
                } else if (selected instanceof CustomSkillEntity) {
                    skillName = ((CustomSkillEntity) selected).name;
                }
                if (skillName != null && !skillName.isEmpty()) {
                    viewModel.addSkill(skillName);
                    skillsAdapter.setItems(viewModel.getSkillItems());
                }
            }));
        }).start();
    }

    private <T> void showSearchableListDialog(String title, List<T> items, java.util.function.Consumer<T> onSelect) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_searchable_list, null);
        EditText searchInput = dialogView.findViewById(R.id.search_input);
        ListView listView = dialogView.findViewById(R.id.list_view);

        List<T> filteredItems = new ArrayList<>(items);
        ArrayAdapter<T> adapter = new ArrayAdapter<T>(this, android.R.layout.simple_list_item_1, filteredItems) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                T item = getItem(position);
                String display;
                if (item instanceof LanguageEntity) {
                    display = ((LanguageEntity) item).name;
                } else if (item instanceof SkillEntity) {
                    display = ((SkillEntity) item).name;
                } else if (item instanceof CustomLanguageEntity) {
                    display = ((CustomLanguageEntity) item).name;
                } else if (item instanceof CustomSkillEntity) {
                    display = ((CustomSkillEntity) item).name;
                } else {
                    display = item.toString();
                }
                view.setText(display);
                return view;
            }
        };
        listView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                filteredItems.clear();
                for (T item : items) {
                    String name = "";
                    if (item instanceof LanguageEntity) name = ((LanguageEntity) item).name;
                    else if (item instanceof SkillEntity) name = ((SkillEntity) item).name;
                    else if (item instanceof CustomLanguageEntity) name = ((CustomLanguageEntity) item).name;
                    else if (item instanceof CustomSkillEntity) name = ((CustomSkillEntity) item).name;
                    else name = item.toString();
                    if (name.toLowerCase().contains(query)) {
                        filteredItems.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            T selected = filteredItems.get(position);
            onSelect.accept(selected);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAddFeatureDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_feature, null);
        EditText etName = view.findViewById(R.id.feature_name);
        EditText etDesc = view.findViewById(R.id.feature_desc);
        new AlertDialog.Builder(this)
                .setTitle("Add Feature")
                .setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    if (name.isEmpty()) return;
                    CharacterTraitEntity feature = new CharacterTraitEntity();
                    feature.name = name;
                    feature.description = desc;
                    viewModel.addFeature(feature);
                    featuresAdapter.setItems(viewModel.getFeatureItems());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        String desc = etDesc.getText().toString().trim();
        int selectedPos = spinnerGameSystem.getSelectedItemPosition();
        String gameSystem = selectedPos >= 0 && selectedPos < gameSystems.size() ?
                gameSystems.get(selectedPos).key : "5e-2014";
        int gold = Integer.parseInt(etStartingGold.getText().toString());
        viewModel.save(name, desc, gameSystem, gold);
    }
}