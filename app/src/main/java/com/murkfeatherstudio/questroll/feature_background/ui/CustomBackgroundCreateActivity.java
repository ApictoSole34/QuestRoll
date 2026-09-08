package com.murkfeatherstudio.questroll.feature_background.ui;

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
    private EditText etName, etDesc;
    private Spinner spinnerGameSystem;
    private EditText etStartingGold;
    private EditText etEquipmentDescription, etLanguagesDescription, etLanguageChoices;
    private final List<GameSystemEntity> gameSystems = new ArrayList<>();

    private GenericItemAdapter<String> equipmentAdapter;
    private GenericItemAdapter<String> languagesAdapter;
    private GenericItemAdapter<String> skillsAdapter;
    private GenericItemAdapter<String> toolsAdapter;
    private GenericItemAdapter<CharacterTraitEntity> featuresAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_background_create);

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
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        spinnerGameSystem = findViewById(R.id.spinnerGameSystem);
        etStartingGold = findViewById(R.id.etStartingGold);
        etEquipmentDescription = findViewById(R.id.etEquipmentDescription);
        etLanguagesDescription = findViewById(R.id.etLanguagesDescription);
        etLanguageChoices = findViewById(R.id.etLanguageChoices);

        findViewById(R.id.btnAddEquipment).setOnClickListener(v -> showAddItemDialog());
        findViewById(R.id.btnAddLanguage).setOnClickListener(v -> showAddLanguageDialog());
        findViewById(R.id.btnAddSkill).setOnClickListener(v -> showAddSkillDialog());
        findViewById(R.id.btnAddTool).setOnClickListener(v -> showAddStringDialog("Tool", toolsAdapter, viewModel::addTool, viewModel::getToolItems));
        findViewById(R.id.btnAddFeature).setOnClickListener(v -> showAddFeatureDialog());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void observeViewModel() {
        viewModel.getEditData().observe(this, entity -> {
            if (entity != null) {
                etName.setText(entity.name);
                etDesc.setText(entity.desc);
                etStartingGold.setText(String.valueOf(entity.startingGold));
                selectGameSystem(entity.gameSystem);
                refreshAdapters();
                etEquipmentDescription.setText(viewModel.getEquipmentDescription());
                etLanguagesDescription.setText(viewModel.getLanguagesDescription());
                etLanguageChoices.setText(String.valueOf(viewModel.getLanguageChoices()));
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
        spinnerGameSystem.setAdapter(adapter);
    }

    private void selectGameSystem(String systemKey) {
        if (systemKey == null) return;
        for (int i = 0; i < gameSystems.size(); i++) {
            if (systemKey.equals(gameSystems.get(i).key)) {
                spinnerGameSystem.setSelection(i);
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

        initRv(R.id.rvEquipment, equipmentAdapter);
        initRv(R.id.rvLanguages, languagesAdapter);
        initRv(R.id.rvSkillProficiencies, skillsAdapter);
        initRv(R.id.rvToolProficiencies, toolsAdapter);
        initRv(R.id.rvFeatures, featuresAdapter);
    }

    private void initRv(int id, RecyclerView.Adapter<?> adapter) {
        RecyclerView rv = findViewById(id);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_searchable_list, null);
        EditText search = view.findViewById(R.id.search_input);
        ListView list = view.findViewById(R.id.list_view);

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
        list.setAdapter(adapter);

        search.addTextChangedListener(new TextWatcher() {
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

        builder.setView(view).setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        list.setOnItemClickListener((p, v, pos, id) -> {
            Object selected = filtered.get(pos);
            dialog.dismiss();
            handleItemSelected(selected);
        });
        dialog.show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_searchable_list, null);
        EditText search = view.findViewById(R.id.search_input);
        ListView list = view.findViewById(R.id.list_view);
        List<T> filtered = new ArrayList<>(items);
        ArrayAdapter<T> adapter = new ArrayAdapter<T>(this, android.R.layout.simple_list_item_1, filtered) {
            @NonNull @Override
            public View getView(int pos, View convert, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(pos, convert, parent);
                tv.setText(getItemName(getItem(pos)));
                return tv;
            }
        };
        list.setAdapter(adapter);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().toLowerCase();
                filtered.clear();
                for (T i : items) if (getItemName(i).toLowerCase().contains(q)) filtered.add(i);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setView(view).setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        list.setOnItemClickListener((p, v, pos, id) -> {
            onSelect.accept(filtered.get(pos));
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showAddFeatureDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_feature, null);
        EditText fName = view.findViewById(R.id.feature_name);
        EditText fDesc = view.findViewById(R.id.feature_desc);
        new AlertDialog.Builder(this).setTitle("Add Feature").setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String name = fName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        CharacterTraitEntity feat = new CharacterTraitEntity();
                        feat.name = name;
                        feat.description = fDesc.getText().toString().trim();
                        viewModel.addFeature(feat);
                        featuresAdapter.setItems(viewModel.getFeatureItems());
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        int pos = spinnerGameSystem.getSelectedItemPosition();
        String system = (pos >= 0 && pos < gameSystems.size()) ? gameSystems.get(pos).key : DEFAULT_SYSTEM;
        
        int gold = 0;
        try { gold = Integer.parseInt(etStartingGold.getText().toString()); } catch (NumberFormatException ignored) {}
        
        int choices = 0;
        try { choices = Integer.parseInt(etLanguageChoices.getText().toString()); } catch (NumberFormatException ignored) {}

        viewModel.save(name, etDesc.getText().toString().trim(), system, gold,
                etEquipmentDescription.getText().toString().trim(),
                etLanguagesDescription.getText().toString().trim(), choices);
    }

    private String getCurrentGameSystemKey() {
        int pos = spinnerGameSystem.getSelectedItemPosition();
        return (pos >= 0 && pos < gameSystems.size()) ? gameSystems.get(pos).key : DEFAULT_SYSTEM;
    }
}
