package com.murkfeatherstudio.questroll.feature_spell.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomCastingOption;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.spell_school.SpellSchoolEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomSpellCreateBinding;
import com.murkfeatherstudio.questroll.databinding.DialogCastingOptionBinding;
import com.murkfeatherstudio.questroll.databinding.DialogCustomSchoolBinding;
import com.murkfeatherstudio.questroll.feature_spell.adapter.CastingOptionAdapter;
import com.murkfeatherstudio.questroll.feature_spell.view_model.CustomSpellCreateViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomSpellCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_SPELL_ID = "edit_spell_id";

    private static final String[] CASTING_TIMES = {
            "action", "bonus action", "reaction", "1 minute",
            "10 minutes", "1 hour", "8 hours", "24 hours"
    };

    private CustomSpellCreateViewModel viewModel;
    private CastingOptionAdapter castingOptionAdapter;
    private ActivityCustomSpellCreateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomSpellCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewModel();
        setupDropdowns();
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
                        db.customDamageTypeDao(),
                        Open5eDatabase.getInstance(this).damageTypeDao(),
                        Open5eDatabase.getInstance(this).abilityDao(),
                        db.customAbilityDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomSpellCreateViewModel.class);
    }

    private void setupDropdowns() {
        List<String> levels = new ArrayList<>();
        levels.add("Cantrip (0)");
        for (int i = 1; i <= 9; i++) levels.add("Level " + i);
        binding.spinnerLevel.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levels));

        binding.actvCastingTime.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, CASTING_TIMES));
    }

    private void setupCastingOptionsRecycler() {
        castingOptionAdapter = new CastingOptionAdapter(
                position -> viewModel.removeCastingOption(position)
        );
        binding.rvCastingOptions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCastingOptions.setAdapter(castingOptionAdapter);
        binding.rvCastingOptions.setNestedScrollingEnabled(false);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, this::populateForm);

        viewModel.getCastingOptions().observe(this, options ->
                castingOptionAdapter.submitList(new ArrayList<>(options)));

        viewModel.getCustomSchools().observe(this, customSchools -> {
            List<String> schoolNames = new ArrayList<>();

            Open5eDatabase.getInstance(this).getQueryExecutor().execute(() -> {
                List<SpellSchoolEntity> open5eSchools =
                        Open5eDatabase.getInstance(this).spellSchoolDao().getAllSchools();

                runOnUiThread(() -> {
                    for (SpellSchoolEntity s : open5eSchools) {
                        schoolNames.add(s.name);
                    }
                    if (customSchools != null) {
                        for (CustomSpellSchoolEntity s : customSchools) {
                            if (!schoolNames.contains(s.name)) schoolNames.add(s.name);
                        }
                    }
                    Collections.sort(schoolNames);
                    binding.actvSchool.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, schoolNames));
                });
            });
        });

        viewModel.getCombinedSavingThrowNames().observe(this, savingThrowNames -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, savingThrowNames);
            binding.actvSavingThrow.setAdapter(adapter);
        });

        viewModel.getCombinedDamageTypeNames().observe(this, damageTypeNames -> {
            binding.chipGroupDamageTypes.removeAllViews();
            if (damageTypeNames != null) {
                for (String type : damageTypeNames) {
                    Chip chip = new Chip(this);
                    chip.setText(type.substring(0, 1).toUpperCase() + type.substring(1));
                    chip.setTag(type);
                    chip.setCheckable(true);
                    binding.chipGroupDamageTypes.addView(chip);
                }
            }
            CustomSpellEntity currentSpell = viewModel.getEditData().getValue();
            if (currentSpell != null && currentSpell.damageTypes != null) {
                for (int i = 0; i < binding.chipGroupDamageTypes.getChildCount(); i++) {
                    Chip chip = (Chip) binding.chipGroupDamageTypes.getChildAt(i);
                    chip.setChecked(currentSpell.damageTypes.contains(chip.getTag()));
                }
            }
        });

        binding.cbMaterial.setOnCheckedChangeListener((v, checked) ->
                binding.tilMaterialSpecified.setVisibility(checked ? View.VISIBLE : View.GONE));
    }

    private void setupListeners() {
        binding.btnAddCastingOption.setOnClickListener(v ->
                showAddCastingOptionDialog());

        binding.btnAddCustomSchool.setOnClickListener(v ->
                showAddCustomSchoolDialog());

        binding.btnSaveSpell.setText(viewModel.isEditMode() ? "Update Spell" : "Save Spell");
        binding.btnSaveSpell.setOnClickListener(v -> saveSpell());
    }

    private void populateForm(CustomSpellEntity spell) {
        if (spell == null) return;
        binding.etSpellName.setText(spell.name);
        binding.etSpellDesc.setText(spell.desc);
        binding.etRangeText.setText(spell.rangeText);
        binding.etDuration.setText(spell.duration);
        binding.etMaterialSpecified.setText(spell.materialSpecified);
        binding.etDamageRoll.setText(spell.damageRoll);
        binding.etHigherLevel.setText(spell.higherLevel);
        binding.actvSchool.setText(spell.schoolName, false);
        binding.actvCastingTime.setText(spell.castingTime, false);
        binding.actvSavingThrow.setText(spell.savingThrowAbility, false);
        binding.spinnerLevel.setSelection(spell.level);
        binding.cbVerbal.setChecked(spell.verbal);
        binding.cbSomatic.setChecked(spell.somatic);
        binding.cbMaterial.setChecked(spell.material);
        binding.cbRitual.setChecked(spell.ritual);
        binding.cbConcentration.setChecked(spell.concentration);
        binding.cbAttackRoll.setChecked(spell.attackRoll);

        binding.tilMaterialSpecified.setVisibility(spell.material ? View.VISIBLE : View.GONE);
    }

    private void saveSpell() {
        String name = binding.etSpellName.getText() != null
                ? binding.etSpellName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etSpellName.setError("Spell name is required");
            return;
        }

        CustomSpellEntity entity = new CustomSpellEntity();
        entity.name = name;
        entity.desc = getText(binding.etSpellDesc);
        entity.level = binding.spinnerLevel.getSelectedItemPosition();
        entity.schoolName = binding.actvSchool.getText().toString().trim();
        entity.castingTime = binding.actvCastingTime.getText().toString().trim();
        entity.rangeText = getText(binding.etRangeText);
        entity.duration = getText(binding.etDuration);
        entity.verbal = binding.cbVerbal.isChecked();
        entity.somatic = binding.cbSomatic.isChecked();
        entity.material = binding.cbMaterial.isChecked();
        entity.materialSpecified = getText(binding.etMaterialSpecified);
        entity.ritual = binding.cbRitual.isChecked();
        entity.concentration = binding.cbConcentration.isChecked();
        entity.savingThrowAbility = binding.actvSavingThrow.getText().toString().trim();
        entity.attackRoll = binding.cbAttackRoll.isChecked();
        entity.damageRoll = getText(binding.etDamageRoll);
        entity.higherLevel = getText(binding.etHigherLevel);

        List<String> selectedDamageTypes = new ArrayList<>();
        for (int i = 0; i < binding.chipGroupDamageTypes.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupDamageTypes.getChildAt(i);
            if (chip.isChecked()) selectedDamageTypes.add((String) chip.getTag());
        }
        entity.damageTypes = selectedDamageTypes;

        viewModel.saveSpell(entity);
    }

    private void showAddCastingOptionDialog() {
        DialogCastingOptionBinding dialogBinding = DialogCastingOptionBinding.inflate(getLayoutInflater());

        new AlertDialog.Builder(this)
                .setTitle("Add Scaling Option")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add", (d, w) -> {
                    CustomCastingOption option = new CustomCastingOption();
                    option.type = dialogBinding.etOptionType.getText().toString().trim();
                    option.damageRoll = dialogBinding.etOptionDamage.getText().toString().trim();
                    option.range = dialogBinding.etOptionRange.getText().toString().trim();
                    option.duration = dialogBinding.etOptionDuration.getText().toString().trim();
                    option.desc = dialogBinding.etOptionDesc.getText().toString().trim();
                    if (!option.type.isEmpty()) {
                        viewModel.addCastingOption(option);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddCustomSchoolDialog() {
        DialogCustomSchoolBinding dialogBinding = DialogCustomSchoolBinding.inflate(getLayoutInflater());

        new AlertDialog.Builder(this)
                .setTitle("Add Custom School")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add", (d, w) -> {
                    String schoolName = dialogBinding.etSchoolName.getText().toString().trim();
                    if (!schoolName.isEmpty()) {
                        viewModel.saveCustomSchool(
                                schoolName,
                                dialogBinding.etSchoolDesc.getText().toString().trim()
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
