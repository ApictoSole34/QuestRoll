package com.fizzycoyote.qusetroll.feature_class.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.res.ResourcesCompat;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_table_data.CustomTableData;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassWithDetails;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.gained_at.GainedAt;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data.TableData;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.FeatureAdapter;
import com.fizzycoyote.qusetroll.feature_class.ui.wizard.ClassWizardActivity;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassDetailViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class ClassDetailActivity extends BaseActivity {

    private TextView className, hitDiceTextView, tvHitPoints, tvSavingThrows;
    private RecyclerView featuresRecycler;
    private ClassDetailViewModel viewModel;
    private LinearLayout detailsContainer;
    private Markwon markwon;

    private static final Map<String, String> COLUMN_TITLES = new HashMap<String, String>() {{
        put("PROFICIENCY_BONUS", "Prof Bonus");
        put("MARTIAL_ARTS", "Martial Arts");
        put("KI_POINTS", "Ki Points");
        put("UNARMORED_MOVEMENT", "Movement");
        put("CANTRIPS_KNOWN", "Cantrips");
        put("SPELLS_KNOWN", "Spells Known");
        put("SPELL_SLOTS", "Spell Slots");
        put("CLASS_FEATURE", "Class Features");
        put("RAGES", "Rages");
        put("RAGE_DAMAGE", "Rage Damage");
        put("SLOTS_1ST", "1st");
        put("SLOTS_2ND", "2nd");
        put("SLOTS_3RD", "3rd");
        put("SLOTS_4TH", "4th");
        put("SLOTS_5TH", "5th");
        put("SLOTS_6TH", "6th");
        put("SLOTS_7TH", "7th");
        put("SLOTS_8TH", "8th");
        put("SLOTS_9TH", "9th");
    }};

    private static final List<String> PRIORITY_COLUMNS = Arrays.asList(
            "Prof Bonus", "Features", "Martial Arts", "Ki Points", "Movement",
            "Rages", "Rage Damage", "Cantrips", "Spells Known",
            "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(HtmlPlugin.create())
                .build();

        String classKey = getIntent().getStringExtra("CLASS_KEY");
        initViews();
        setupViewModel(classKey);
        observeData();

        if (viewModel.isCustom()) {
            setupCustomClassMenu(classKey);
        }
    }

    private void initViews() {
        className = findViewById(R.id.tv_class_name);
        hitDiceTextView = findViewById(R.id.tv_hit_dice);
        featuresRecycler = findViewById(R.id.recycler_features);
        tvHitPoints = findViewById(R.id.tv_hit_points);
        featuresRecycler.setLayoutManager(new LinearLayoutManager(this));

        className.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
        className.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        hitDiceTextView.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        hitDiceTextView.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        tvHitPoints.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        tvHitPoints.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
    }

    private void setupViewModel(String classKey) {
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        ClassDetailViewModel.Factory factory = new ClassDetailViewModel.Factory(
                open5eDb.characterClassDao(),
                open5eDb.featureDao(),
                open5eDb.hitPointsDao(),
                open5eDb.savingThrowDao(),
                customDb.customCharacterClassDao(),
                classKey
        );

        viewModel = new ViewModelProvider(this, factory).get(ClassDetailViewModel.class);
    }

    private void observeData() {
        if (viewModel.isCustom()) {
            observeCustomData();
        } else {
            observeOpen5eData();
        }
    }

    // ------ class menu -----
    private void setupCustomClassMenu(String classKey) {
        Button btnManage = findViewById(R.id.btnManage);
        btnManage.setVisibility(View.VISIBLE);
        btnManage.setOnClickListener(v -> showManageMenu(v, classKey));
    }

    private void showManageMenu(View anchor, String classKey) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit Class");
        popup.getMenu().add(0, 2, 1, "Delete Class");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                openEditClass(classKey);
                return true;
            } else if (item.getItemId() == 2) {
                confirmDeleteClass(classKey);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void openEditClass(String classKey) {
        long classId = Long.parseLong(classKey.replace("custom_", ""));
        Intent intent = new Intent(this, ClassWizardActivity.class);
        intent.putExtra("edit_class_id", classId);
        startActivity(intent);
    }

    private void confirmDeleteClass(String classKey) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete this class?")
                .setPositiveButton("Delete", (dialog, which) -> deleteClass(classKey))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteClass(String classKey) {
        long classId = Long.parseLong(classKey.replace("custom_", ""));
        UserContentDatabase.getInstance(this)
                .getQueryExecutor()
                .execute(() -> {
                    UserContentDatabase.getInstance(this)
                            .customCharacterClassDao()
                            .deleteClass(classId);
                    runOnUiThread(this::finish);
                });
    }

    // --- Open5e ---
    private void observeOpen5eData() {
        viewModel.getClassWithDetails().observe(this, classWithDetails -> {
            if (classWithDetails != null) {
                updateOpen5eClassInfo(classWithDetails);
            }
        });
    }

    private void updateOpen5eClassInfo(CharacterClassWithDetails classWithDetails) {
        CharacterClassEntity entity = classWithDetails.characterClass;
        className.setText(entity.name);

        boolean isSubclass = entity.subclassOfKey != null;
        if (isSubclass) {
            findViewById(R.id.hit_points_section).setVisibility(View.GONE);
        } else {
            findViewById(R.id.hit_points_section).setVisibility(View.VISIBLE);
            if (classWithDetails.hitPoints != null) {
                HitPointsEntity hp = classWithDetails.hitPoints;
                hitDiceTextView.setText("Hit Dice: " + hp.hitDice);
                tvHitPoints.setText("HP at 1st: " + hp.at1stLevel +
                        "\nHP at Higher: " + hp.atHigherLevels);
            }
        }

        processClassTable(classWithDetails.features);
        updateOpen5eFeatures(classWithDetails.features);
    }

    private void updateOpen5eFeatures(List<FeatureEntity> features) {
        FeatureAdapter adapter = buildReadOnlyFeatureAdapter();
        featuresRecycler.setAdapter(adapter);
        adapter.submitList(convertToCustomFeatures(features));
    }

    private List<CustomFeatureEntity> convertToCustomFeatures(List<FeatureEntity> features) {
        return features.stream()
                .filter(f -> !"CLASS_TABLE_DATA".equals(f.featureType)
                        && !"PROFICIENCY_BONUS".equals(f.featureType))
                .map(f -> {
                    CustomFeatureEntity customFeature = new CustomFeatureEntity();
                    customFeature.name = f.name;
                    customFeature.description = f.desc;
                    customFeature.type = f.featureType;
                    return customFeature;
                })
                .collect(Collectors.toList());
    }

    // --- Custom ---
    private void observeCustomData() {
        viewModel.getCustomClass().observe(this, data -> {
            if (data != null) {
                updateCustomClassInfo(data);
            }
        });
    }

    private int dp(int v) {
        return dpToPx(v);
    }

    private void updateCustomClassInfo(CustomCharacterClassWithFeatures data) {
        CustomCharacterClassEntity entity = data.characterClassEntity;
        if (entity == null) return;

        className.setText(entity.name);

        LinearLayout mainContainer = findViewById(R.id.traits_container);
        if (mainContainer != null && detailsContainer == null) {
            ViewGroup parent = (ViewGroup) mainContainer.getParent();
            int index = parent.indexOfChild(mainContainer);
            detailsContainer = new LinearLayout(this);
            detailsContainer.setOrientation(LinearLayout.VERTICAL);
            detailsContainer.setPadding(0, 0, 0, dp(16));
            parent.addView(detailsContainer, index);
        }

        if (detailsContainer != null) {
            detailsContainer.removeAllViews();
            addDetailRow(detailsContainer, "Game System", entity.gameSystem);
            addDetailRow(detailsContainer, "Hit Dice", entity.hitDice);
            addDetailRow(detailsContainer, "Caster Type", entity.casterType);
            addDetailRow(detailsContainer, "Spellcasting Ability", entity.spellcastingAbility);
            addDetailRow(detailsContainer, "Starting Gold", entity.startingGoldDice);
            addDetailRow(detailsContainer, "Skill Choices", String.valueOf(entity.skillChoicesCount));
            if (entity.skillOptionsJson != null && !entity.skillOptionsJson.isEmpty()) {
                try {
                    Type listType = new TypeToken<List<String>>(){}.getType();
                    List<String> opts = new Gson().fromJson(entity.skillOptionsJson, listType);
                    addDetailRow(detailsContainer, "Skill Options", TextUtils.join(", ", opts));
                } catch (Exception e) {}
            }
            addDetailRow(detailsContainer, "Equipment Description", entity.equipmentDescription);
        }

        boolean isSubclass = entity.subclassOf != null;
        if (isSubclass) {
            findViewById(R.id.hit_points_section).setVisibility(View.GONE);
        } else {
            findViewById(R.id.hit_points_section).setVisibility(View.VISIBLE);
            hitDiceTextView.setText("Hit Dice: " + entity.hitDice);
            tvHitPoints.setText("HP at 1st: 1" + entity.hitDice + " + Constitution modifier");
        }

        processCustomClassTable(data.features);
        updateCustomFeatures(data.features);
    }

    private void addDetailRow(LinearLayout container, String label, String value) {
        if (value == null || value.isEmpty()) return;

        TextView labelView = new TextView(this);
        labelView.setText(label + ":");
        labelView.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_semibold));
        labelView.setTextColor(getResources().getColor(R.color.threads_gold, null));
        labelView.setPadding(0, dp(8), 0, dp(2));
        container.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        valueView.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        markwon.setMarkdown(valueView, value);
        valueView.setPadding(0, 0, 0, dp(4));
        container.addView(valueView);
    }

    private void processCustomClassTable(List<CustomFeatureEntity> features) {
        if (features == null || features.isEmpty()) {
            findViewById(R.id.class_table_section).setVisibility(View.GONE);
            return;
        }

        Map<Integer, Map<String, String>> levelData = new HashMap<>();
        Set<String> availableColumns = new HashSet<>();

        for (CustomFeatureEntity feature : features) {
            if (feature.customTableData != null && !feature.customTableData.isEmpty()) {
                String columnTitle = feature.name;
                availableColumns.add(columnTitle);
                for (CustomTableData tableData : feature.customTableData) {
                    levelData.computeIfAbsent(tableData.level, k -> new HashMap<>())
                            .put(columnTitle, tableData.columnValue);
                }
            }

            if ("CLASS_LEVEL_FEATURE".equals(feature.type) &&
                    feature.customGainedAt != null && !feature.customGainedAt.isEmpty()) {
                for (CustomGainedAt gainedAt : feature.customGainedAt) {
                    if (gainedAt.level > 0) {
                        Map<String, String> row = levelData
                                .computeIfAbsent(gainedAt.level, k -> new HashMap<>());
                        String current = row.getOrDefault("Features", "");
                        String featureInfo = feature.name;
                        if (gainedAt.details != null && !gainedAt.details.isEmpty()) {
                            featureInfo += " (" + gainedAt.details + ")";
                        }
                        row.put("Features",
                                current.isEmpty() ? featureInfo : current + ", " + featureInfo);
                        availableColumns.add("Features");
                    }
                }
            }
        }

        if (!levelData.isEmpty()) {
            List<String> columnHeaders = new ArrayList<>();
            columnHeaders.add("Level");
            if (availableColumns.contains("Features")) columnHeaders.add("Features");
            for (String col : availableColumns) {
                if (!columnHeaders.contains(col)) columnHeaders.add(col);
            }
            setupFullClassTable(columnHeaders, levelData);
            findViewById(R.id.class_table_section).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.class_table_section).setVisibility(View.GONE);
        }
    }

    private void updateCustomFeatures(List<CustomFeatureEntity> features) {
        FeatureAdapter adapter = buildReadOnlyFeatureAdapter();
        featuresRecycler.setAdapter(adapter);
        adapter.submitList(features != null ? new ArrayList<>(features) : new ArrayList<>());
    }

    // --- Class table (Open5e) ---
    private void processClassTable(List<FeatureEntity> features) {
        Map<Integer, Map<String, String>> levelData = new HashMap<>();
        Set<String> availableColumns = new HashSet<>();

        for (FeatureEntity feature : features) {
            if (feature.tableData != null && !feature.tableData.isEmpty()
                    && feature.featureType != null) {

                String columnTitle;
                if ("CLASS_FEATURE".equals(feature.featureType) &&
                        ("Rages".equals(feature.name) || "Rage Damage".equals(feature.name))) {
                    columnTitle = feature.name;
                } else if ("SPELL_SLOTS".equals(feature.featureType)) {
                    columnTitle = feature.name;
                } else if (COLUMN_TITLES.containsKey(feature.featureType)) {
                    columnTitle = COLUMN_TITLES.get(feature.featureType);
                } else {
                    continue;
                }

                availableColumns.add(columnTitle);
                for (TableData tableData : feature.tableData) {
                    levelData.computeIfAbsent(tableData.level, k -> new HashMap<>())
                            .put(columnTitle, tableData.columnValue);
                }
            }

            if (feature.gainedAt != null && !feature.gainedAt.isEmpty()) {
                for (GainedAt gainedAt : feature.gainedAt) {
                    if (gainedAt.level > 0) {
                        Map<String, String> row = levelData
                                .computeIfAbsent(gainedAt.level, k -> new HashMap<>());
                        String current = row.getOrDefault("Features", "");
                        String featureInfo = feature.name;
                        if (gainedAt.detail != null && !gainedAt.detail.isEmpty()) {
                            featureInfo += " (" + gainedAt.detail + ")";
                        }
                        row.put("Features",
                                current.isEmpty() ? featureInfo : current + ", " + featureInfo);
                        availableColumns.add("Features");
                    }
                }
            }
        }

        if (!levelData.isEmpty()) {
            List<String> columnHeaders = new ArrayList<>();
            columnHeaders.add("Level");
            for (String priorityCol : PRIORITY_COLUMNS) {
                if (availableColumns.contains(priorityCol)) columnHeaders.add(priorityCol);
            }
            for (String column : availableColumns) {
                if (!columnHeaders.contains(column)) columnHeaders.add(column);
            }
            setupFullClassTable(columnHeaders, levelData);
            findViewById(R.id.class_table_section).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.class_table_section).setVisibility(View.GONE);
        }
    }

    private void setupFullClassTable(List<String> columnHeaders,
                                     Map<Integer, Map<String, String>> levelData) {
        TableLayout fullTable = findViewById(R.id.full_class_table);
        fullTable.removeAllViews();
        fullTable.setStretchAllColumns(true);

        TableRow headerRow = new TableRow(this);
        for (int i = 0; i < columnHeaders.size(); i++) {
            headerRow.addView(createHeaderCell(columnHeaders.get(i), i == 0));
        }
        fullTable.addView(headerRow);

        for (int level = 1; level <= 20; level++) {
            TableRow dataRow = new TableRow(this);
            for (int i = 0; i < columnHeaders.size(); i++) {
                dataRow.addView(createDataCell(columnHeaders.get(i), level, levelData, i == 0));
            }
            fullTable.addView(dataRow);
        }
    }

    private TextView createHeaderCell(String header, boolean isFirstColumn) {
        TextView textView = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                getHeaderWidth(header), TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1));
        textView.setLayoutParams(params);
        textView.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        textView.setText(header);
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_semibold));
        textView.setBackgroundColor(Color.parseColor(isFirstColumn ? "#808080" : "#9E9E9E"));
        textView.setTextColor(getResources().getColor(android.R.color.white));
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(12);
        textView.setSingleLine(false);
        textView.setMaxLines(2);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        return textView;
    }

    private TextView createDataCell(String columnName, int level,
                                    Map<Integer, Map<String, String>> levelData,
                                    boolean isFirstColumn) {
        TextView textView = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                getHeaderWidth(columnName), TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1));
        textView.setLayoutParams(params);
        textView.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER);
        textView.setSingleLine(false);
        textView.setMaxLines(2);
        textView.setEllipsize(TextUtils.TruncateAt.END);

        if (isFirstColumn) {
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_semibold));
            textView.setBackgroundColor(Color.parseColor("#F0F0F0"));
        } else {
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            textView.setBackgroundResource(android.R.drawable.edit_text);
        }

        String value;
        if (columnName.equals("Level")) {
            value = String.valueOf(level);
        } else {
            Map<String, String> levelRow = levelData.get(level);
            value = (levelRow != null && levelRow.containsKey(columnName))
                    ? levelRow.get(columnName) : "-";
        }
        textView.setText(value);
        return textView;
    }

    private int getHeaderWidth(String columnName) {
        switch (columnName) {
            case "Level":        return dpToPx(50);
            case "Prof Bonus":   return dpToPx(70);
            case "Features":     return dpToPx(180);
            case "Cantrips":
            case "Spells Known": return dpToPx(90);
            case "Martial Arts":
            case "Ki Points":
            case "Movement":
            case "Rages":
            case "Rage Damage":  return dpToPx(80);
            case "1st": case "2nd": case "3rd": case "4th": case "5th":
            case "6th": case "7th": case "8th": case "9th": return dpToPx(45);
            default:             return dpToPx(100);
        }
    }

    private FeatureAdapter buildReadOnlyFeatureAdapter() {
        return new FeatureAdapter(new FeatureAdapter.OnFeatureClickListener() {
            @Override public void onEdit(CustomFeatureEntity feature, int index) {}
            @Override public void onDelete(int index) {}
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}