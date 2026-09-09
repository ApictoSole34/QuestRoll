package com.murkfeatherstudio.questroll.feature_species.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.murkfeatherstudio.questroll.databinding.ActivityCustomSpeciesCreateBinding;
import com.murkfeatherstudio.questroll.databinding.DialogAbilityBonusBinding;
import com.murkfeatherstudio.questroll.databinding.DialogCreatureActionBinding;
import com.murkfeatherstudio.questroll.databinding.DialogSearchableListBinding;
import com.murkfeatherstudio.questroll.feature_species.adapter.AbilityBonusAdapter;
import com.murkfeatherstudio.questroll.feature_species.adapter.LanguageKeyAdapter;
import com.murkfeatherstudio.questroll.feature_species.adapter.OtherTraitAdapter;
import com.murkfeatherstudio.questroll.feature_species.viewmodel.CustomSpeciesCreateViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomSpeciesCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_species_id";

    private CustomSpeciesCreateViewModel viewModel;
    private ActivityCustomSpeciesCreateBinding binding;

    private AbilityBonusAdapter abilityBonusAdapter;
    private LanguageKeyAdapter languageKeyAdapter;
    private OtherTraitAdapter otherTraitAdapter;

    private List<GameSystemEntity> gameSystems = new ArrayList<>();
    private List<String> parentKeys = new ArrayList<>();
    private List<String> parentNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomSpeciesCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        binding.cbIsSubspecies.setOnCheckedChangeListener((v, checked) ->
                binding.layoutParent.setVisibility(checked ? View.VISIBLE : View.GONE));

        binding.btnAddAbilityBonus.setOnClickListener(v -> showAddAbilityBonusDialog());
        binding.btnAddLanguage.setOnClickListener(v -> showAddLanguageDialog());
        binding.btnAddTrait.setOnClickListener(v -> showAddTraitDialog());
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void initViews() {
        binding.rvAbilityBonuses.setLayoutManager(new LinearLayoutManager(this));
        abilityBonusAdapter = new AbilityBonusAdapter(position -> viewModel.removeAbilityBonus(position));
        binding.rvAbilityBonuses.setAdapter(abilityBonusAdapter);

        binding.rvLanguages.setLayoutManager(new LinearLayoutManager(this));
        languageKeyAdapter = new LanguageKeyAdapter(position -> viewModel.removeLanguageKey(position));
        binding.rvLanguages.setAdapter(languageKeyAdapter);

        binding.rvOtherTraits.setLayoutManager(new LinearLayoutManager(this));
        otherTraitAdapter = new OtherTraitAdapter(position -> viewModel.removeOtherTrait(position));
        binding.rvOtherTraits.setAdapter(otherTraitAdapter);
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
                binding.spinnerGameSystem.setAdapter(adapter);
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
                            binding.actvParent.setAdapter(new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1, parentNames));
                        });
                    });
        });
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, s -> {
            if (s == null) return;
            binding.etName.setText(s.name);
            binding.etDesc.setText(s.desc);
            binding.cbIsSubspecies.setChecked(s.isSubspecies);
            binding.layoutParent.setVisibility(s.isSubspecies ? View.VISIBLE : View.GONE);
            if (s.isSubspecies && s.subspeciesOfName != null) {
                binding.actvParent.setText(s.subspeciesOfName, false);
            }
            binding.etSpeed.setText(s.speed);
            binding.etSize.setText(s.size);
            binding.etLanguageChoices.setText(String.valueOf(s.languageChoices));
            for (int i = 0; i < gameSystems.size(); i++) {
                if (gameSystems.get(i).key.equals(s.gameSystem)) {
                    binding.spinnerGameSystem.setSelection(i);
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

        DialogAbilityBonusBinding dialogBinding = DialogAbilityBonusBinding.inflate(getLayoutInflater());
        ArrayAdapter<String> abilityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, abilities);
        abilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerAbility.setAdapter(abilityAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add Ability Bonus")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add", (d, w) -> {
                    String ability = (String) dialogBinding.spinnerAbility.getSelectedItem();
                    String bonusText = dialogBinding.etBonus.getText().toString().trim();
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
        DialogCreatureActionBinding dialogBinding = DialogCreatureActionBinding.inflate(getLayoutInflater());
        dialogBinding.spinnerActionType.setVisibility(View.GONE);
        
        new AlertDialog.Builder(this)
                .setTitle("Add Trait")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add", (d, w) -> {
                    String name = dialogBinding.etActionName.getText() != null ? dialogBinding.etActionName.getText().toString().trim() : "";
                    if (name.isEmpty()) return;
                    CustomCreatureAction t = new CustomCreatureAction();
                    t.name = name;
                    t.desc = dialogBinding.etActionDesc.getText() != null ? dialogBinding.etActionDesc.getText().toString().trim() : "";
                    viewModel.addOtherTrait(t);
                })
                .setNegativeButton("Cancel", null).show();
    }

    private <T> void showSearchableListDialog(String title, List<T> items, java.util.function.Consumer<T> onSelect) {
        DialogSearchableListBinding dialogBinding = DialogSearchableListBinding.inflate(getLayoutInflater());

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
        dialogBinding.listView.setAdapter(adapter);

        dialogBinding.searchInput.addTextChangedListener(new TextWatcher() {
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
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Cancel", null)
                .show();

        dialogBinding.listView.setOnItemClickListener((parent, view, position, id) -> {
            T selected = filteredItems.get(position);
            onSelect.accept(selected);
            dialog.dismiss();
        });
    }

    private String getCurrentGameSystemKey() {
        int pos = binding.spinnerGameSystem.getSelectedItemPosition();
        return (pos >= 0 && pos < gameSystems.size()) ? gameSystems.get(pos).key : "5e-2014";
    }

    private void save() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }
        CustomSpeciesEntity e = new CustomSpeciesEntity();
        e.name = name;
        e.desc = binding.etDesc.getText() != null ? binding.etDesc.getText().toString().trim() : "";
        e.isSubspecies = binding.cbIsSubspecies.isChecked();
        if (e.isSubspecies) {
            String parentName = binding.actvParent.getText().toString().trim();
            int idx = parentNames.indexOf(parentName);
            if (idx >= 0) {
                e.subspeciesOfKey = parentKeys.get(idx);
                e.subspeciesOfName = parentName.replace(" (SRD)", "").replace(" (Custom)", "").trim();
            }
        }
        e.speed = binding.etSpeed.getText() != null ? binding.etSpeed.getText().toString().trim() : "";
        e.size = binding.etSize.getText() != null ? binding.etSize.getText().toString().trim() : "";
        String choicesStr = binding.etLanguageChoices.getText() != null ? binding.etLanguageChoices.getText().toString() : "0";
        e.languageChoices = Integer.parseInt(choicesStr.isEmpty() ? "0" : choicesStr);
        e.gameSystem = getCurrentGameSystemKey();
        viewModel.save(e);
    }
}
