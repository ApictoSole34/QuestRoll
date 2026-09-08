package com.murkfeatherstudio.questroll.feature_species.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.game_system.GameSystemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesEntity;
import com.murkfeatherstudio.questroll.feature_species.adapter.AbilityBonusAdapter;
import com.murkfeatherstudio.questroll.feature_species.adapter.LanguageKeyAdapter;
import com.murkfeatherstudio.questroll.feature_species.adapter.OtherTraitAdapter;
import com.murkfeatherstudio.questroll.feature_species.viewmodel.CustomSpeciesCreateViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomSpeciesCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_species_id";

    private CustomSpeciesCreateViewModel viewModel;
    private TextInputEditText etName, etDesc;
    private CheckBox cbIsSubspecies;
    private LinearLayout layoutParent;
    private AutoCompleteTextView actvParent;
    private Spinner spinnerGameSystem;
    private EditText etSpeed, etSize, etLanguageChoices;
    private RecyclerView rvAbilityBonuses, rvLanguages, rvOtherTraits;
    private Button btnAddAbilityBonus, btnAddLanguage, btnAddTrait, btnSave;

    private AbilityBonusAdapter abilityBonusAdapter;
    private LanguageKeyAdapter languageKeyAdapter;
    private OtherTraitAdapter otherTraitAdapter;

    private List<GameSystemEntity> gameSystems = new ArrayList<>();
    private List<String> parentKeys = new ArrayList<>();
    private List<String> parentNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_species_create);

        UserContentDatabase customDb = UserContentDatabase.getInstance(this);
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        long editId = getIntent().getLongExtra(EXTRA_EDIT_ID, CustomSpeciesCreateViewModel.NO_ID);

        viewModel = new ViewModelProvider(this,
                new CustomSpeciesCreateViewModel.Factory(
                        customDb.customSpeciesDao(),
                        open5eDb.speciesDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomSpeciesCreateViewModel.class);

        initViews();
        setupObservers();
        loadGameSystems();
        loadParentOptions(customDb, open5eDb);

        cbIsSubspecies.setOnCheckedChangeListener((v, checked) ->
                layoutParent.setVisibility(checked ? View.VISIBLE : View.GONE));

        btnAddAbilityBonus.setOnClickListener(v -> showAddAbilityBonusDialog());
        btnAddLanguage.setOnClickListener(v -> showAddLanguageDialog());
        btnAddTrait.setOnClickListener(v -> showAddTraitDialog());
        btnSave.setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        cbIsSubspecies = findViewById(R.id.cbIsSubspecies);
        layoutParent = findViewById(R.id.layoutParent);
        actvParent = findViewById(R.id.actvParent);
        spinnerGameSystem = findViewById(R.id.spinnerGameSystem);
        etSpeed = findViewById(R.id.etSpeed);
        etSize = findViewById(R.id.etSize);
        etLanguageChoices = findViewById(R.id.etLanguageChoices);
        rvAbilityBonuses = findViewById(R.id.rvAbilityBonuses);
        rvLanguages = findViewById(R.id.rvLanguages);
        rvOtherTraits = findViewById(R.id.rvOtherTraits);
        btnAddAbilityBonus = findViewById(R.id.btnAddAbilityBonus);
        btnAddLanguage = findViewById(R.id.btnAddLanguage);
        btnAddTrait = findViewById(R.id.btnAddTrait);
        btnSave = findViewById(R.id.btnSave);

        rvAbilityBonuses.setLayoutManager(new LinearLayoutManager(this));
        abilityBonusAdapter = new AbilityBonusAdapter(position -> viewModel.removeAbilityBonus(position));
        rvAbilityBonuses.setAdapter(abilityBonusAdapter);

        rvLanguages.setLayoutManager(new LinearLayoutManager(this));
        languageKeyAdapter = new LanguageKeyAdapter(position -> viewModel.removeLanguageKey(position));
        rvLanguages.setAdapter(languageKeyAdapter);

        rvOtherTraits.setLayoutManager(new LinearLayoutManager(this));
        otherTraitAdapter = new OtherTraitAdapter(position -> viewModel.removeOtherTrait(position));
        rvOtherTraits.setAdapter(otherTraitAdapter);
    }

    private void loadGameSystems() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<GameSystemEntity> systems = Open5eDatabase.getInstance(this)
                    .gameSystemDao().getAllGameSystems();
            if (systems == null || systems.isEmpty()) {
                GameSystemEntity fallback = new GameSystemEntity();
                fallback.key = "5e-2014";
                fallback.name = "D&D 5e (2014 Rules)";
                systems = List.of(fallback);
            }
            final List<GameSystemEntity> finalSystems = systems;
            AppExecutors.getInstance().mainThread().execute(() -> {
                gameSystems = finalSystems;
                List<String> names = new ArrayList<>();
                for (GameSystemEntity gs : gameSystems) names.add(gs.name);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGameSystem.setAdapter(adapter);
            });
        });
    }

    private void loadParentOptions(UserContentDatabase customDb, Open5eDatabase open5eDb) {
        AppExecutors.getInstance().mainThread().execute(() -> {
            open5eDb.speciesDao()
                    .getFilteredSpecies("", 0, 1, "")
                    .observe(this, open5eSpecies -> {
                        parentKeys.clear();
                        parentNames.clear();
                        parentKeys.add("");
                        parentNames.add("None");

                        if (open5eSpecies != null) {
                            for (SpeciesEntity s : open5eSpecies) {
                                parentKeys.add(s.key);
                                parentNames.add(s.name + " (SRD)");
                            }
                        }

                        customDb.customSpeciesDao().getAll().observe(this, customSpecies -> {
                            int open5eCount = open5eSpecies != null ? open5eSpecies.size() : 0;
                            while (parentKeys.size() > 1 + open5eCount) {
                                parentKeys.remove(parentKeys.size() - 1);
                                parentNames.remove(parentNames.size() - 1);
                            }
                            if (customSpecies != null) {
                                for (CustomSpeciesEntity cs : customSpecies) {
                                    parentKeys.add("custom_" + cs.id);
                                    parentNames.add(cs.name + " (Custom)");
                                }
                            }
                            actvParent.setAdapter(new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1, parentNames));
                        });
                    });
        });
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, s -> {
            if (s == null) return;
            etName.setText(s.name);
            etDesc.setText(s.desc);
            cbIsSubspecies.setChecked(s.isSubspecies);
            layoutParent.setVisibility(s.isSubspecies ? View.VISIBLE : View.GONE);
            if (s.isSubspecies && s.subspeciesOfName != null) {
                actvParent.setText(s.subspeciesOfName, false);
            }
            etSpeed.setText(s.speed);
            etSize.setText(s.size);
            etLanguageChoices.setText(String.valueOf(s.languageChoices));
            for (int i = 0; i < gameSystems.size(); i++) {
                if (gameSystems.get(i).key.equals(s.gameSystem)) {
                    spinnerGameSystem.setSelection(i);
                    break;
                }
            }
        });

        viewModel.getAbilityBonuses().observe(this, bonuses -> abilityBonusAdapter.submitList(bonuses));
        viewModel.getLanguageKeys().observe(this, keys -> languageKeyAdapter.submitList(keys));
        viewModel.getOtherTraits().observe(this, traits -> otherTraitAdapter.submitList(traits));

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, viewModel.isEditMode() ? "Updated" : "Saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Name already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddAbilityBonusDialog() {
        String[] abilities = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ability_bonus, null);
        Spinner spinnerAbility = view.findViewById(R.id.spinner_ability);
        EditText etBonus = view.findViewById(R.id.et_bonus);

        ArrayAdapter<String> abilityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, abilities);
        abilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAbility.setAdapter(abilityAdapter);

        builder.setTitle("Add Ability Bonus")
                .setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String ability = (String) spinnerAbility.getSelectedItem();
                    String bonusText = etBonus.getText().toString().trim();
                    if (bonusText.isEmpty()) {
                        Toast.makeText(this, "Enter bonus value", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int bonus;
                    try {
                        bonus = Integer.parseInt(bonusText.replace("+", ""));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (bonus == 0) {
                        Toast.makeText(this, "Bonus cannot be 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.addAbilityBonus(new CustomSpeciesCreateViewModel.AbilityBonus(ability, bonus));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddLanguageDialog() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<LanguageEntity> standardLangs = Open5eDatabase.getInstance(this)
                    .languageDao()
                    .getAllSync();

            List<CustomLanguageEntity> customLangs = UserContentDatabase.getInstance(this)
                    .customLanguageDao()
                    .getAll();

            List<Object> all = new ArrayList<>();
            all.addAll(standardLangs);
            all.addAll(customLangs);
            all.sort((a, b) -> {
                String nameA = (a instanceof LanguageEntity) ? ((LanguageEntity) a).name : ((CustomLanguageEntity) a).name;
                String nameB = (b instanceof LanguageEntity) ? ((LanguageEntity) b).name : ((CustomLanguageEntity) b).name;
                return nameA.compareTo(nameB);
            });
            AppExecutors.getInstance().mainThread().execute(() -> showSearchableListDialog("Select Language", all, selected -> {
                String key = (selected instanceof LanguageEntity) ?
                        ((LanguageEntity) selected).key :
                        "custom_" + ((CustomLanguageEntity) selected).id;
                viewModel.addLanguageKey(key);
            }));
        });
    }

    private void showAddTraitDialog() {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_creature_action, null);
        EditText etName = dv.findViewById(R.id.etActionName);
        EditText etDesc = dv.findViewById(R.id.etActionDesc);
        dv.findViewById(R.id.spinnerActionType).setVisibility(View.GONE);
        new AlertDialog.Builder(this)
                .setTitle("Add Trait")
                .setView(dv)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    CustomCreatureAction t = new CustomCreatureAction();
                    t.name = name;
                    t.desc = etDesc.getText().toString().trim();
                    viewModel.addOtherTrait(t);
                })
                .setNegativeButton("Cancel", null).show();
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
                android.widget.TextView tv = (android.widget.TextView) super.getView(position, convertView, parent);
                T item = getItem(position);
                String display = "";
                if (item instanceof LanguageEntity) display = ((LanguageEntity) item).name;
                else if (item instanceof CustomLanguageEntity) display = ((CustomLanguageEntity) item).name;
                tv.setText(display);
                return tv;
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

    private String getCurrentGameSystemKey() {
        int pos = spinnerGameSystem.getSelectedItemPosition();
        return (pos >= 0 && pos < gameSystems.size()) ? gameSystems.get(pos).key : "5e-2014";
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        CustomSpeciesEntity e = new CustomSpeciesEntity();
        e.name = name;
        e.desc = etDesc.getText().toString().trim();
        e.isSubspecies = cbIsSubspecies.isChecked();
        if (e.isSubspecies) {
            String parentName = actvParent.getText().toString().trim();
            int idx = parentNames.indexOf(parentName);
            if (idx >= 0) {
                e.subspeciesOfKey = parentKeys.get(idx);
                e.subspeciesOfName = parentName.replace(" (SRD)", "").replace(" (Custom)", "").trim();
            }
        }
        e.speed = etSpeed.getText().toString().trim();
        e.size = etSize.getText().toString().trim();
        e.languageChoices = Integer.parseInt(etLanguageChoices.getText().toString());
        e.gameSystem = getCurrentGameSystemKey();
        viewModel.save(e);
    }
}
