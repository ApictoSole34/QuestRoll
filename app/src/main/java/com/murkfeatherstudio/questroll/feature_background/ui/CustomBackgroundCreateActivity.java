package com.murkfeatherstudio.questroll.feature_background.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item.CustomItemEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.skill.SkillEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.game_system.GameSystemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item_set.ItemSetEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomBackgroundCreateBinding;
import com.murkfeatherstudio.questroll.databinding.DialogAddFeatureBinding;
import com.murkfeatherstudio.questroll.databinding.DialogSearchableListBinding;
import com.murkfeatherstudio.questroll.feature_background.adapter.GenericItemAdapter;
import com.murkfeatherstudio.questroll.feature_background.view_model.CustomBackgroundCreateViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CustomBackgroundCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_background_id";
    private static final String DEFAULT_SYSTEM = "5e-2014";

    private CustomBackgroundCreateViewModel viewModel;
    private ActivityCustomBackgroundCreateBinding binding;
    private final List<GameSystemEntity> gameSystems = new ArrayList<>();

    private GenericItemAdapter<String> equipmentAdapter;
    private GenericItemAdapter<String> languagesAdapter;
    private GenericItemAdapter<String> skillsAdapter;
    private GenericItemAdapter<String> toolsAdapter;
    private GenericItemAdapter<CharacterTraitEntity> featuresAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomBackgroundCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViewModel();
        initViews();
        loadGameSystems();
        setupRecyclerViews();
        observeViewModel();
    }

    private void initViewModel() {
        long editId = getIntent().getLongExtra(EXTRA_EDIT_ID, CustomBackgroundCreateViewModel.NO_ID);
        viewModel = new ViewModelProvider(this,
                new CustomBackgroundCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customBackgroundDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomBackgroundCreateViewModel.class);
    }

    private void initViews() {
        binding.btnAddEquipment.setOnClickListener(v -> showAddItemDialog());
        binding.btnAddLanguage.setOnClickListener(v -> showAddLanguageDialog());
        binding.btnAddSkill.setOnClickListener(v -> showAddSkillDialog());
        binding.btnAddTool.setOnClickListener(v -> showAddStringDialog("Tool", toolsAdapter, viewModel::addTool, viewModel::getToolItems));
        binding.btnAddFeature.setOnClickListener(v -> showAddFeatureDialog());
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void observeViewModel() {
        viewModel.getEditData().observe(this, entity -> {
            if (entity != null) {
                binding.etName.setText(entity.name);
                binding.etDesc.setText(entity.desc);
                binding.etStartingGold.setText(String.valueOf(entity.startingGold));
                selectGameSystem(entity.gameSystem);
                refreshAdapters();
                binding.etEquipmentDescription.setText(viewModel.getEquipmentDescription());
                binding.etLanguagesDescription.setText(viewModel.getLanguagesDescription());
                binding.etLanguageChoices.setText(String.valueOf(viewModel.getLanguageChoices()));
            }
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, viewModel.isEditMode() ? "Updated" : "Saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Save failed. Name might already exist.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshAdapters() {
        equipmentAdapter.setItems(viewModel.getEquipmentItems());
        languagesAdapter.setItems(viewModel.getLanguageItems());
        skillsAdapter.setItems(viewModel.getSkillItems());
        toolsAdapter.setItems(viewModel.getToolItems());
        featuresAdapter.setItems(viewModel.getFeatureItems());
    }

    private void loadGameSystems() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<GameSystemEntity> systems = Open5eDatabase.getInstance(this)
                    .gameSystemDao()
                    .getAllGameSystems();
            if (systems == null || systems.isEmpty()) {
                systems = createFallbackSystems();
            }
            final List<GameSystemEntity> finalSystems = systems;
            AppExecutors.getInstance().mainThread().execute(() -> {
                gameSystems.clear();
                gameSystems.addAll(finalSystems);
                setupSpinner();
            });
        });
    }

    private List<GameSystemEntity> createFallbackSystems() {
        GameSystemEntity v2014 = new GameSystemEntity();
        v2014.key = DEFAULT_SYSTEM;
        v2014.name = "D&D 5e (2014 Rules)";
        GameSystemEntity v2024 = new GameSystemEntity();
        v2024.key = "5e-2024";
        v2024.name = "D&D 5e (2024 Rules)";
        return List.of(v2014, v2024);
    }

    private void setupSpinner() {
        List<String> names = gameSystems.stream()
                .map(gs -> gs.name != null ? gs.name : gs.key)
                .collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGameSystem.setAdapter(adapter);
    }

    private void selectGameSystem(String systemKey) {
        if (systemKey == null) return;
        for (int i = 0; i < gameSystems.size(); i++) {
            if (systemKey.equals(gameSystems.get(i).key)) {
                binding.spinnerGameSystem.setSelection(i);
                break;
            }
        }
    }

    private void setupRecyclerViews() {
        equipmentAdapter = new GenericItemAdapter<>(i -> {
            viewModel.removeEquipmentItem(i);
            equipmentAdapter.setItems(viewModel.getEquipmentItems());
        }, i -> i);
        
        languagesAdapter = new GenericItemAdapter<>(i -> {
            viewModel.removeLanguage(i);
            languagesAdapter.setItems(viewModel.getLanguageItems());
        }, i -> i);
        
        skillsAdapter = new GenericItemAdapter<>(i -> {
            viewModel.removeSkill(i);
            skillsAdapter.setItems(viewModel.getSkillItems());
        }, i -> i);
        
        toolsAdapter = new GenericItemAdapter<>(i -> {
            viewModel.removeTool(i);
            toolsAdapter.setItems(viewModel.getToolItems());
        }, i -> i);
        
        featuresAdapter = new GenericItemAdapter<>(i -> {
            viewModel.removeFeature(i);
            featuresAdapter.setItems(viewModel.getFeatureItems());
        }, i -> i.name);

        initRv(binding.rvEquipment, equipmentAdapter);
        initRv(binding.rvLanguages, languagesAdapter);
        initRv(binding.rvSkillProficiencies, skillsAdapter);
        initRv(binding.rvToolProficiencies, toolsAdapter);
        initRv(binding.rvFeatures, featuresAdapter);
    }

    private void initRv(RecyclerView rv, RecyclerView.Adapter<?> adapter) {
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    private void showAddItemDialog() {
        String sys = getCurrentGameSystemKey();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Object> all = new ArrayList<>();
            all.addAll(Open5eDatabase.getInstance(this).itemDao().getAllByGameSystem(sys));
            all.addAll(UserContentDatabase.getInstance(this).customItemDao().getAllSync());
            all.addAll(Open5eDatabase.getInstance(this).itemSetDao().getAllByGameSystem(sys));
            all.addAll(UserContentDatabase.getInstance(this).customItemSetDao().getAllSync());
            AppExecutors.getInstance().mainThread().execute(() -> showItemSelectionDialog(all));
        });
    }

    private void showItemSelectionDialog(List<Object> items) {
        DialogSearchableListBinding dialogBinding = DialogSearchableListBinding.inflate(getLayoutInflater());

        List<Object> filtered = new ArrayList<>(items);
        ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(this, android.R.layout.simple_list_item_1, filtered) {
            @NonNull @Override
            public View getView(int pos, View convert, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(pos, convert, parent);
                Object o = getItem(pos);
                tv.setText(getItemName(o));
                return tv;
            }
        };
        dialogBinding.listView.setAdapter(adapter);

        dialogBinding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().toLowerCase();
                filtered.clear();
                for (Object o : items) {
                    if (getItemName(o).toLowerCase().contains(q)) filtered.add(o);
                }
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Cancel", null)
                .show();

        dialogBinding.listView.setOnItemClickListener((p, v, pos, id) -> {
            Object selected = filtered.get(pos);
            dialog.dismiss();
            handleItemSelected(selected);
        });
    }

    private String getItemName(Object o) {
        if (o instanceof ItemEntity) return ((ItemEntity) o).name;
        if (o instanceof CustomItemEntity) return ((CustomItemEntity) o).name;
        if (o instanceof ItemSetEntity) return ((ItemSetEntity) o).name;
        if (o instanceof CustomItemSetEntity) return ((CustomItemSetEntity) o).name;
        if (o instanceof LanguageEntity) return ((LanguageEntity) o).name;
        if (o instanceof CustomLanguageEntity) return ((CustomLanguageEntity) o).name;
        if (o instanceof SkillEntity) return ((SkillEntity) o).name;
        if (o instanceof CustomSkillEntity) return ((CustomSkillEntity) o).name;
        return String.valueOf(o);
    }

    private void handleItemSelected(Object selected) {
        if (selected instanceof ItemEntity || selected instanceof CustomItemEntity) {
            viewModel.addEquipmentItem(getItemName(selected));
        } else if (selected instanceof ItemSetEntity) {
            showItemSetSelectionDialog((ItemSetEntity) selected);
        } else if (selected instanceof CustomItemSetEntity) {
            showCustomItemSetSelectionDialog((CustomItemSetEntity) selected);
        }
        equipmentAdapter.setItems(viewModel.getEquipmentItems());
    }

    private void showItemSetSelectionDialog(ItemSetEntity set) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<ItemEntity> items = Open5eDatabase.getInstance(this)
                    .itemDao().getByKeysAndGameSystemSync(set.itemKeys, getCurrentGameSystemKey());
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (items.isEmpty()) return;
                String[] names = items.stream().map(i -> i.name).toArray(String[]::new);
                boolean[] checked = new boolean[items.size()];
                new AlertDialog.Builder(this)
                        .setTitle("Select items from: " + set.name)
                        .setMultiChoiceItems(names, checked, (d, w, c) -> checked[w] = c)
                        .setPositiveButton("Add", (d, w) -> {
                            for (int i = 0; i < items.size(); i++) if (checked[i]) viewModel.addEquipmentItem(items.get(i).name);
                            equipmentAdapter.setItems(viewModel.getEquipmentItems());
                        })
                        .setNegativeButton("Cancel", null).show();
            });
        });
    }

    private void showCustomItemSetSelectionDialog(CustomItemSetEntity set) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (set.itemKeys == null) return;
            List<String> names = new ArrayList<>();
            for (String k : set.itemKeys) {
                if (k.startsWith("custom_")) {
                    CustomItemEntity ci = UserContentDatabase.getInstance(this).customItemDao().getByIdSync(Long.parseLong(k.replace("custom_", "")));
                    if (ci != null) names.add(ci.name);
                } else {
                    ItemEntity si = Open5eDatabase.getInstance(this).itemDao().getByKeySync(k);
                    if (si != null) names.add(si.name);
                }
            }
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (names.isEmpty()) return;
                String[] arr = names.toArray(new String[0]);
                boolean[] checked = new boolean[names.size()];
                new AlertDialog.Builder(this).setTitle("Select from: " + set.name)
                        .setMultiChoiceItems(arr, checked, (d, w, c) -> checked[w] = c)
                        .setPositiveButton("Add", (d, w) -> {
                            for (int i = 0; i < names.size(); i++) if (checked[i]) viewModel.addEquipmentItem(names.get(i));
                            equipmentAdapter.setItems(viewModel.getEquipmentItems());
                        })
                        .setNegativeButton("Cancel", null).show();
            });
        });
    }

    private void showAddLanguageDialog() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Object> all = new ArrayList<>();
            all.addAll(Open5eDatabase.getInstance(this).languageDao().getAllSync());
            all.addAll(UserContentDatabase.getInstance(this).customLanguageDao().getAll());
            AppExecutors.getInstance().mainThread().execute(() -> showSearchableListDialog("Select Language", all, selected -> {
                viewModel.addLanguage(getItemName(selected));
                languagesAdapter.setItems(viewModel.getLanguageItems());
            }));
        });
    }

    private void showAddSkillDialog() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Object> all = new ArrayList<>();
            all.addAll(Open5eDatabase.getInstance(this).skillDao().getAllSync());
            all.addAll(UserContentDatabase.getInstance(this).customSkillDao().getAllSync());
            AppExecutors.getInstance().mainThread().execute(() -> showSearchableListDialog("Select Skill", all, selected -> {
                viewModel.addSkill(getItemName(selected));
                skillsAdapter.setItems(viewModel.getSkillItems());
            }));
        });
    }

    private void showAddStringDialog(String title, GenericItemAdapter<String> adapter,
                                     java.util.function.Consumer<String> callback,
                                     java.util.function.Supplier<List<String>> getter) {
        EditText in = new EditText(this);
        new AlertDialog.Builder(this).setTitle("Add " + title).setView(in)
                .setPositiveButton("Add", (d, w) -> {
                    String val = in.getText().toString().trim();
                    if (!val.isEmpty()) {
                        callback.accept(val);
                        adapter.setItems(getter.get());
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private <T> void showSearchableListDialog(String title, List<T> items, java.util.function.Consumer<T> onSelect) {
        DialogSearchableListBinding dialogBinding = DialogSearchableListBinding.inflate(getLayoutInflater());
        List<T> filtered = new ArrayList<>(items);
        ArrayAdapter<T> adapter = new ArrayAdapter<T>(this, android.R.layout.simple_list_item_1, filtered) {
            @NonNull @Override
            public View getView(int pos, View convert, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(pos, convert, parent);
                tv.setText(getItemName(getItem(pos)));
                return tv;
            }
        };
        dialogBinding.listView.setAdapter(adapter);
        dialogBinding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().toLowerCase();
                filtered.clear();
                for (T i : items) if (getItemName(i).toLowerCase().contains(q)) filtered.add(i);
                adapter.notifyDataSetChanged();
            }
        });
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Cancel", null)
                .show();
        
        dialogBinding.listView.setOnItemClickListener((p, v, pos, id) -> {
            onSelect.accept(filtered.get(pos));
            dialog.dismiss();
        });
    }

    private void showAddFeatureDialog() {
        DialogAddFeatureBinding dialogBinding = DialogAddFeatureBinding.inflate(getLayoutInflater());
        new AlertDialog.Builder(this).setTitle("Add Feature").setView(dialogBinding.getRoot())
                .setPositiveButton("Add", (d, w) -> {
                    String name = dialogBinding.featureName.getText() != null ? dialogBinding.featureName.getText().toString().trim() : "";
                    if (!name.isEmpty()) {
                        CharacterTraitEntity feat = new CharacterTraitEntity();
                        feat.name = name;
                        feat.description = dialogBinding.featureDesc.getText() != null ? dialogBinding.featureDesc.getText().toString().trim() : "";
                        viewModel.addFeature(feat);
                        featuresAdapter.setItems(viewModel.getFeatureItems());
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private void save() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }
        int pos = binding.spinnerGameSystem.getSelectedItemPosition();
        String system = (pos >= 0 && pos < gameSystems.size()) ? gameSystems.get(pos).key : DEFAULT_SYSTEM;
        
        int gold = 0;
        try {
            String goldStr = binding.etStartingGold.getText() != null ? binding.etStartingGold.getText().toString() : "0";
            gold = Integer.parseInt(goldStr.isEmpty() ? "0" : goldStr);
        } catch (NumberFormatException ignored) {}
        
        int choices = 0;
        try {
            String choicesStr = binding.etLanguageChoices.getText() != null ? binding.etLanguageChoices.getText().toString() : "0";
            choices = Integer.parseInt(choicesStr.isEmpty() ? "0" : choicesStr);
        } catch (NumberFormatException ignored) {}

        viewModel.save(name, binding.etDesc.getText().toString().trim(), system, gold,
                binding.etEquipmentDescription.getText().toString().trim(),
                binding.etLanguagesDescription.getText().toString().trim(), choices);
    }

    private String getCurrentGameSystemKey() {
        int pos = binding.spinnerGameSystem.getSelectedItemPosition();
        return (pos >= 0 && pos < gameSystems.size()) ? gameSystems.get(pos).key : DEFAULT_SYSTEM;
    }
}
