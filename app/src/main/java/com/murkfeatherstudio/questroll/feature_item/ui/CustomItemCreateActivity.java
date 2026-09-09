package com.murkfeatherstudio.questroll.feature_item.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item.CustomItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemDto;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomItemCreateBinding;
import com.murkfeatherstudio.questroll.feature_item.view_model.CustomItemCreateViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class CustomItemCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_item_id";

    private CustomItemCreateViewModel viewModel;
    private ActivityCustomItemCreateBinding binding;

    private TextInputEditText etName, etDesc, etWeight, etCost;
    private AutoCompleteTextView actvCategory, actvRarity;
    private CheckBox cbMagic, cbAttunement;
    private TextInputEditText etAttunementDetail;

    private Spinner spinnerWeaponProperty;
    private ChipGroup chipGroupSelectedProperties;

    private LinearLayout weaponSection;
    private TextInputEditText etDamageDice, etRange, etLongRange;
    private AutoCompleteTextView actvDamageType;
    private CheckBox cbSimple, cbImprovised;

    private LinearLayout armorSection;
    private TextInputEditText etAcBase, etAcDisplay;
    private CheckBox cbStealthDisadvantage;
    private TextInputEditText etStrengthRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomItemCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomItemCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customItemDao(),
                        editId,
                        Executors.newSingleThreadExecutor(),
                        Open5eDatabase.getInstance(this).itemCategoryDao(),
                        UserContentDatabase.getInstance(this).customItemCategoryDao(),
                        Open5eDatabase.getInstance(this).itemRarityDao(),
                        UserContentDatabase.getInstance(this).customItemRarityDao(),
                        Open5eDatabase.getInstance(this).damageTypeDao(),
                        UserContentDatabase.getInstance(this).customDamageTypeDao(),
                        Open5eDatabase.getInstance(this).weaponPropertyDao(),
                        UserContentDatabase.getInstance(this).customWeaponPropertyDao()
                )).get(CustomItemCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(viewModel.isEditMode() ? "Edit Item" : "Create Item");
        binding.btnSave.setText(viewModel.isEditMode() ? "Update Item" : "Save Item");
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = binding.etName;
        etDesc = binding.etDesc;
        actvCategory = binding.actvCategory;
        actvRarity = binding.actvRarity;
        cbMagic = binding.cbMagic;
        cbAttunement = binding.cbAttunement;
        etAttunementDetail = binding.etAttunementDetail;
        etWeight = binding.etWeight;
        etCost = binding.etCost;

        viewModel.getAllCategoryNames().observe(this, categoryNames -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, categoryNames);
            actvCategory.setAdapter(adapter);
        });

        viewModel.getCombinedRarityNames().observe(this, rarityNames -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, rarityNames);
            actvRarity.setAdapter(adapter);
        });

        viewModel.getCombinedDamageTypeNames().observe(this, damageTypeNames -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, damageTypeNames);
            actvDamageType.setAdapter(adapter);
        });

        weaponSection = binding.weaponSection;
        etDamageDice = binding.etDamageDice;
        etRange = binding.etRange;
        etLongRange = binding.etLongRange;
        actvDamageType = binding.actvDamageType;
        cbSimple = binding.cbSimple;
        cbImprovised = binding.cbImprovised;

        spinnerWeaponProperty = binding.spinnerWeaponProperty;
        chipGroupSelectedProperties = binding.chipGroupSelectedProperties;

        viewModel.getAllWeaponPropertyNames().observe(this, propertyNames -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, propertyNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerWeaponProperty.setAdapter(adapter);
        });

        /**
         * JAVADOC: chipGroupSelectedProperties is a dynamic container. We use addView() 
         * to insert Material Chips programmatically because the selection of weapon 
         * properties is dynamic and determined by user interaction at runtime. 
         * These chips do not exist in the static XML layout.
         */
        viewModel.getSelectedWeaponProperties().observe(this, selectedSet -> {
            chipGroupSelectedProperties.removeAllViews();
            if (selectedSet != null) {
                for (String propName : selectedSet) {
                    Chip chip = new Chip(this);
                    chip.setText(propName);
                    chip.setCloseIconVisible(true);
                    chip.setOnCloseIconClickListener(v -> viewModel.removeWeaponProperty(propName));
                    chipGroupSelectedProperties.addView(chip);
                }
            }
        });

        binding.btnAddSelectedProperty.setOnClickListener(v -> {
            String selected = (String) spinnerWeaponProperty.getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                viewModel.addWeaponProperty(selected);
            }
        });

        armorSection = binding.armorSection;
        etAcBase = binding.etAcBase;
        etAcDisplay = binding.etAcDisplay;
        cbStealthDisadvantage = binding.cbStealthDisadvantage;
        etStrengthRequired = binding.etStrengthRequired;

        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            String category = (String) parent.getItemAtPosition(position);
            updateSectionsVisibility(category);
        });
        updateSectionsVisibility(actvCategory.getText().toString());
    }

    private void updateSectionsVisibility(String category) {
        weaponSection.setVisibility(category.equals("Weapon") ? View.VISIBLE : View.GONE);
        armorSection.setVisibility(category.equals("Armor") ? View.VISIBLE : View.GONE);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, item -> {
            if (item == null) return;
            etName.setText(item.name);
            etDesc.setText(item.desc);
            actvCategory.setText(item.categoryName, false);
            actvRarity.setText(item.rarityName, false);
            cbMagic.setChecked(item.isMagicItem);
            cbAttunement.setChecked(item.requiresAttunement);
            etAttunementDetail.setText(item.attunementDetail);
            etWeight.setText(String.valueOf(item.weight));
            etCost.setText(String.valueOf(item.cost));

            if (item.categoryName != null && item.categoryName.equalsIgnoreCase("Weapon") && item.weaponJson != null) {
                try {
                    ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                    if (weapon != null) {
                        etDamageDice.setText(weapon.damageDice);
                        actvDamageType.setText(weapon.damageType != null ? weapon.damageType.name : "", false);
                        etRange.setText(String.valueOf((int) weapon.range));
                        etLongRange.setText(String.valueOf((int) weapon.longRange));
                        cbSimple.setChecked(weapon.isSimple);
                        cbImprovised.setChecked(weapon.isImprovised);
                        if (weapon.properties != null) {
                            for (ItemDto.WeaponEmbedDto.WeaponPropertyDto wp : weapon.properties) {
                                if (wp.property != null && wp.property.name != null) {
                                    viewModel.addWeaponProperty(wp.property.name);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (item.categoryName != null && item.categoryName.equalsIgnoreCase("Armor") && item.armorJson != null) {
                try {
                    ItemDto.ArmorEmbedDto armor = new Gson().fromJson(item.armorJson, ItemDto.ArmorEmbedDto.class);
                    if (armor != null) {
                        etAcBase.setText(String.valueOf(armor.acBase));
                        etAcDisplay.setText(armor.acDisplay);
                        cbStealthDisadvantage.setChecked(armor.grantsStealthDisadvantage);
                        etStrengthRequired.setText(armor.strengthScoreRequired != null ? String.valueOf(armor.strengthScoreRequired) : "");
                    }
                } catch (Exception ignored) {}
            }
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, viewModel.isEditMode() ? "Item updated!" : "Item saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "An item with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }

        CustomItemEntity item = new CustomItemEntity();
        item.name = name;
        item.desc = etDesc.getText().toString().trim();
        item.categoryName = actvCategory.getText().toString().trim();
        item.categoryKey = item.categoryName.toLowerCase().replace(" ", "_");
        item.rarityName = actvRarity.getText().toString().trim();
        item.rarityKey = item.rarityName.toLowerCase().replace(" ", "_");
        item.isMagicItem = cbMagic.isChecked();
        item.requiresAttunement = cbAttunement.isChecked();
        item.attunementDetail = etAttunementDetail.getText().toString().trim();
        try { item.weight = Float.parseFloat(etWeight.getText().toString().trim()); } catch (Exception e) { item.weight = 0; }
        try { item.cost = Float.parseFloat(etCost.getText().toString().trim()); } catch (Exception e) { item.cost = 0; }
        item.weightUnit = "lb";

        if (item.categoryName.equalsIgnoreCase("Weapon")) {
            ItemDto.WeaponEmbedDto weapon = new ItemDto.WeaponEmbedDto();
            weapon.damageDice = etDamageDice.getText().toString().trim();
            weapon.range = parseFloat(etRange.getText().toString().trim());
            weapon.longRange = parseFloat(etLongRange.getText().toString().trim());
            weapon.isSimple = cbSimple.isChecked();
            weapon.isImprovised = cbImprovised.isChecked();
            ItemDto.WeaponEmbedDto.DamageTypeDto dt = new ItemDto.WeaponEmbedDto.DamageTypeDto();
            dt.name = actvDamageType.getText().toString().trim();
            dt.key = dt.name.toLowerCase().replace(" ", "_");
            weapon.damageType = dt;

            List<ItemDto.WeaponEmbedDto.WeaponPropertyDto> propList = new ArrayList<>();
            Set<String> selectedProps = viewModel.getSelectedWeaponProperties().getValue();
            if (selectedProps != null) {
                for (String propName : selectedProps) {
                    ItemDto.WeaponEmbedDto.WeaponPropertyDto wp = new ItemDto.WeaponEmbedDto.WeaponPropertyDto();
                    wp.detail = "";
                    ItemDto.WeaponEmbedDto.WeaponPropertyDto.PropertyDetailDto detail = new ItemDto.WeaponEmbedDto.WeaponPropertyDto.PropertyDetailDto();
                    detail.name = propName;
                    wp.property = detail;
                    propList.add(wp);
                }
            }
            weapon.properties = propList;
            item.weaponJson = new Gson().toJson(weapon);
        } else {
            item.weaponJson = null;
        }

        if (item.categoryName.equalsIgnoreCase("Armor")) {
            ItemDto.ArmorEmbedDto armor = new ItemDto.ArmorEmbedDto();
            armor.acBase = (int) parseFloat(etAcBase.getText().toString().trim());
            armor.acDisplay = etAcDisplay.getText().toString().trim();
            armor.grantsStealthDisadvantage = cbStealthDisadvantage.isChecked();
            String strReq = etStrengthRequired.getText().toString().trim();
            armor.strengthScoreRequired = strReq.isEmpty() ? null : (int) parseFloat(strReq);
            item.armorJson = new Gson().toJson(armor);
        } else {
            item.armorJson = null;
        }

        viewModel.save(item);
    }

    private float parseFloat(String s) {
        try { return Float.parseFloat(s); } catch (Exception e) { return 0f; }
    }
}