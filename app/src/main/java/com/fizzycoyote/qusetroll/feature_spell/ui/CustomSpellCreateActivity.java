package com.fizzycoyote.qusetroll.feature_spell.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomCastingOption;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.fizzycoyote.qusetroll.feature_spell.adapter.CastingOptionAdapter;
import com.fizzycoyote.qusetroll.feature_spell.view_model.CustomSpellCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomSpellCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_SPELL_ID = "edit_spell_id";

    private static final String[] DAMAGE_TYPES = {
            "acid", "bludgeoning", "cold", "fire", "force",
            "lightning", "necrotic", "piercing", "poison",
            "psychic", "radiant", "slashing", "thunder"
    };

    private static final String[] SAVING_THROWS = {
            "", "strength", "dexterity", "constitution",
            "intelligence", "wisdom", "charisma"
    };

    private static final String[] CASTING_TIMES = {
            "action", "bonus action", "reaction", "1 minute",
            "10 minutes", "1 hour", "8 hours", "24 hours"
    };

    private CustomSpellCreateViewModel viewModel;
    private CastingOptionAdapter castingOptionAdapter;

    private TextInputEditText etName, etDesc, etRangeText, etDuration,
            etMaterialSpecified, etDamageRoll, etHigherLevel;
    private AutoCompleteTextView actvSchool, actvCastingTime, actvSavingThrow;
    private Spinner spinnerLevel;
    private CheckBox cbVerbal, cbSomatic, cbMaterial, cbRitual, cbConcentration, cbAttackRoll;
    private ChipGroup chipGroupDamageTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_spell_create);

        setupViewModel();
        initViews();
        setupDropdowns();
        setupDamageTypeChips();
        setupCastingOptionsRecycler();
        setupObservers();
        setupListeners();

        setTitle(viewModel.isEditMode() ? "Edit Spell" : "Create Spell");
    }

    private void setupViewModel() {
        UserContentDatabase db = UserContentDatabase.getInstance(this);
        long editId = getIntent().getLongExtra(EXTRA_EDIT_SPELL_ID, CustomSpellCreateViewModel.NO_ID);

        viewModel = new ViewModelProvider(this,
                new CustomSpellCreateViewModel.Factory(
                        db.customSpellDao(),
                        db.customSpellSchoolDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomSpellCreateViewModel.class);
    }

    private void initViews() {
        etName = findViewById(R.id.etSpellName);
        etDesc = findViewById(R.id.etSpellDesc);
        etRangeText = findViewById(R.id.etRangeText);
        etDuration = findViewById(R.id.etDuration);
        etMaterialSpecified = findViewById(R.id.etMaterialSpecified);
        etDamageRoll = findViewById(R.id.etDamageRoll);
        etHigherLevel = findViewById(R.id.etHigherLevel);
        actvSchool = findViewById(R.id.actvSchool);
        actvCastingTime = findViewById(R.id.actvCastingTime);
        actvSavingThrow = findViewById(R.id.actvSavingThrow);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        cbVerbal = findViewById(R.id.cbVerbal);
        cbSomatic = findViewById(R.id.cbSomatic);
        cbMaterial = findViewById(R.id.cbMaterial);
        cbRitual = findViewById(R.id.cbRitual);
        cbConcentration = findViewById(R.id.cbConcentration);
        cbAttackRoll = findViewById(R.id.cbAttackRoll);
        chipGroupDamageTypes = findViewById(R.id.chipGroupDamageTypes);
    }

    private void setupDropdowns() {
        List<String> levels = new ArrayList<>();
        levels.add("Cantrip (0)");
        for (int i = 1; i <= 9; i++) levels.add("Level " + i);
        spinnerLevel.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levels));

        actvCastingTime.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, CASTING_TIMES));

        actvSavingThrow.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, SAVING_THROWS));
    }

    private void setupDamageTypeChips() {
        for (String type : DAMAGE_TYPES) {
            Chip chip = new Chip(this);
            chip.setText(type.substring(0, 1).toUpperCase() + type.substring(1));
            chip.setTag(type);
            chip.setCheckable(true);
            chipGroupDamageTypes.addView(chip);
        }
    }

    private void setupCastingOptionsRecycler() {
        RecyclerView rv = findViewById(R.id.rvCastingOptions);
        castingOptionAdapter = new CastingOptionAdapter(
                position -> viewModel.removeCastingOption(position)
        );
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(castingOptionAdapter);
        rv.setNestedScrollingEnabled(false);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, this::populateForm);

        viewModel.getCastingOptions().observe(this, options ->
                castingOptionAdapter.submitList(new ArrayList<>(options)));

        viewModel.getCustomSchools().observe(this, customSchools -> {
            List<String> schoolNames = new ArrayList<>();
            schoolNames.addAll(Arrays.asList(
                    "Abjuration", "Conjuration", "Divination", "Enchantment",
                    "Evocation", "Illusion", "Necromancy", "Transmutation"
            ));
            if (customSchools != null) {
                for (CustomSpellSchoolEntity s : customSchools) {
                    if (!schoolNames.contains(s.name)) schoolNames.add(s.name);
                }
            }
            actvSchool.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, schoolNames));
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this,
                        viewModel.isEditMode() ? "Spell updated!" : "Spell saved!",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A spell with this name already exists.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        cbMaterial.setOnCheckedChangeListener((v, checked) ->
                findViewById(R.id.tilMaterialSpecified)
                        .setVisibility(checked ? View.VISIBLE : View.GONE));
    }

    private void setupListeners() {
        findViewById(R.id.btnAddCastingOption).setOnClickListener(v ->
                showAddCastingOptionDialog());

        findViewById(R.id.btnAddCustomSchool).setOnClickListener(v ->
                showAddCustomSchoolDialog());

        MaterialButton btnSave = findViewById(R.id.btnSaveSpell);
        btnSave.setText(viewModel.isEditMode() ? "Update Spell" : "Save Spell");
        btnSave.setOnClickListener(v -> saveSpell());
    }

    private void populateForm(CustomSpellEntity spell) {
        if (spell == null) return;
        etName.setText(spell.name);
        etDesc.setText(spell.desc);
        etRangeText.setText(spell.rangeText);
        etDuration.setText(spell.duration);
        etMaterialSpecified.setText(spell.materialSpecified);
        etDamageRoll.setText(spell.damageRoll);
        etHigherLevel.setText(spell.higherLevel);
        actvSchool.setText(spell.schoolName, false);
        actvCastingTime.setText(spell.castingTime, false);
        actvSavingThrow.setText(spell.savingThrowAbility, false);
        spinnerLevel.setSelection(spell.level);
        cbVerbal.setChecked(spell.verbal);
        cbSomatic.setChecked(spell.somatic);
        cbMaterial.setChecked(spell.material);
        cbRitual.setChecked(spell.ritual);
        cbConcentration.setChecked(spell.concentration);
        cbAttackRoll.setChecked(spell.attackRoll);

        if (spell.damageTypes != null) {
            for (int i = 0; i < chipGroupDamageTypes.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupDamageTypes.getChildAt(i);
                chip.setChecked(spell.damageTypes.contains(chip.getTag()));
            }
        }

        findViewById(R.id.tilMaterialSpecified)
                .setVisibility(spell.material ? View.VISIBLE : View.GONE);
    }

    private void saveSpell() {
        String name = etName.getText() != null
                ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            etName.setError("Spell name is required");
            return;
        }

        CustomSpellEntity entity = new CustomSpellEntity();
        entity.name = name;
        entity.desc = getText(etDesc);
        entity.level = spinnerLevel.getSelectedItemPosition();
        entity.schoolName = actvSchool.getText().toString().trim();
        entity.castingTime = actvCastingTime.getText().toString().trim();
        entity.rangeText = getText(etRangeText);
        entity.duration = getText(etDuration);
        entity.verbal = cbVerbal.isChecked();
        entity.somatic = cbSomatic.isChecked();
        entity.material = cbMaterial.isChecked();
        entity.materialSpecified = getText(etMaterialSpecified);
        entity.ritual = cbRitual.isChecked();
        entity.concentration = cbConcentration.isChecked();
        entity.savingThrowAbility = actvSavingThrow.getText().toString().trim();
        entity.attackRoll = cbAttackRoll.isChecked();
        entity.damageRoll = getText(etDamageRoll);
        entity.higherLevel = getText(etHigherLevel);

        List<String> selectedDamageTypes = new ArrayList<>();
        for (int i = 0; i < chipGroupDamageTypes.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDamageTypes.getChildAt(i);
            if (chip.isChecked()) selectedDamageTypes.add((String) chip.getTag());
        }
        entity.damageTypes = selectedDamageTypes;

        viewModel.saveSpell(entity);
    }

    private void showAddCastingOptionDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_casting_option, null);

        EditText etType = dialogView.findViewById(R.id.etOptionType);
        EditText etDamage = dialogView.findViewById(R.id.etOptionDamage);
        EditText etRange = dialogView.findViewById(R.id.etOptionRange);
        EditText etDuration = dialogView.findViewById(R.id.etOptionDuration);
        EditText etDesc = dialogView.findViewById(R.id.etOptionDesc);

        new AlertDialog.Builder(this)
                .setTitle("Add Scaling Option")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    CustomCastingOption option = new CustomCastingOption();
                    option.type = etType.getText().toString().trim();
                    option.damageRoll = etDamage.getText().toString().trim();
                    option.range = etRange.getText().toString().trim();
                    option.duration = etDuration.getText().toString().trim();
                    option.desc = etDesc.getText().toString().trim();
                    if (!option.type.isEmpty()) {
                        viewModel.addCastingOption(option);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddCustomSchoolDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_custom_school, null);

        EditText etSchoolName = dialogView.findViewById(R.id.etSchoolName);
        EditText etSchoolDesc = dialogView.findViewById(R.id.etSchoolDesc);

        new AlertDialog.Builder(this)
                .setTitle("Add Custom School")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String schoolName = etSchoolName.getText().toString().trim();
                    if (!schoolName.isEmpty()) {
                        viewModel.saveCustomSchool(
                                schoolName,
                                etSchoolDesc.getText().toString().trim()
                        );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}