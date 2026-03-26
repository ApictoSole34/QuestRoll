package com.fizzycoyote.qusetroll.feature_item.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomCastingOption;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;
import com.fizzycoyote.qusetroll.feature_item.view_model.CustomItemCreateViewModel;
import com.fizzycoyote.qusetroll.feature_spell.adapter.CastingOptionAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomItemCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_item_id";

    private CustomItemCreateViewModel viewModel;
    private TextInputEditText etName, etDesc, etWeight, etCost;
    private AutoCompleteTextView actvCategory, actvRarity;
    private CheckBox cbMagic, cbAttunement;
    private TextInputEditText etAttunementDetail;

    private LinearLayout weaponSection;
    private TextInputEditText etDamageDice, etRange, etLongRange;
    private AutoCompleteTextView actvDamageType;
    private CheckBox cbSimple, cbImprovised;
    private CastingOptionAdapter propertiesAdapter;

    private LinearLayout armorSection;
    private TextInputEditText etAcBase, etAcDisplay;
    private CheckBox cbStealthDisadvantage;
    private TextInputEditText etStrengthRequired;

    private static final String[] CATEGORIES = {
            "Weapon", "Armor", "Shield", "Potion", "Ring", "Wand", "Staff", "Rod",
            "Spellcasting Focus", "Adventuring Gear", "Scroll", "Ammunition", "Art",
            "Equipment Pack", "Gem", "Jewelry", "Land Vehicle", "Mount", "Poison",
            "Service", "Tools", "Trade Good", "Waterborne Vehicle", "Wondrous Item"
    };
    private static final String[] RARITIES = {
            "Common", "Uncommon", "Rare", "Very Rare", "Legendary", "Artifact"
    };
    private static final String[] DAMAGE_TYPES = {
            "acid", "bludgeoning", "cold", "fire", "force",
            "lightning", "necrotic", "piercing", "poison",
            "psychic", "radiant", "slashing", "thunder"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_item_create);

        long editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new CustomItemCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customItemDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomItemCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(viewModel.isEditMode() ? "Edit Item" : "Create Item");
        ((MaterialButton) findViewById(R.id.btnSave)).setText(viewModel.isEditMode() ? "Update Item" : "Save Item");

        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnAddProperty).setOnClickListener(v -> showAddPropertyDialog());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        actvCategory = findViewById(R.id.actvCategory);
        actvRarity = findViewById(R.id.actvRarity);
        cbMagic = findViewById(R.id.cbMagic);
        cbAttunement = findViewById(R.id.cbAttunement);
        etAttunementDetail = findViewById(R.id.etAttunementDetail);
        etWeight = findViewById(R.id.etWeight);
        etCost = findViewById(R.id.etCost);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, CATEGORIES);
        actvCategory.setAdapter(catAdapter);

        ArrayAdapter<String> rarityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, RARITIES);
        actvRarity.setAdapter(rarityAdapter);

        weaponSection = findViewById(R.id.weapon_section);
        etDamageDice = findViewById(R.id.etDamageDice);
        etRange = findViewById(R.id.etRange);
        etLongRange = findViewById(R.id.etLongRange);
        actvDamageType = findViewById(R.id.actvDamageType);
        cbSimple = findViewById(R.id.cbSimple);
        cbImprovised = findViewById(R.id.cbImprovised);

        ArrayAdapter<String> damageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, DAMAGE_TYPES);
        actvDamageType.setAdapter(damageAdapter);

        RecyclerView rvProperties = findViewById(R.id.rvProperties);
        propertiesAdapter = new CastingOptionAdapter(pos -> {
            List<CustomCreatureAction> list = new ArrayList<>(viewModel.getProperties().getValue());
            if (pos >= 0 && pos < list.size()) {
                list.remove(pos);
                viewModel.setProperties(list);
            }
        });
        rvProperties.setLayoutManager(new LinearLayoutManager(this));
        rvProperties.setAdapter(propertiesAdapter);

        armorSection = findViewById(R.id.armor_section);
        etAcBase = findViewById(R.id.etAcBase);
        etAcDisplay = findViewById(R.id.etAcDisplay);
        cbStealthDisadvantage = findViewById(R.id.cbStealthDisadvantage);
        etStrengthRequired = findViewById(R.id.etStrengthRequired);

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
                    }
                } catch (Exception e) {}
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
                } catch (Exception e) {}
            }
        });

        viewModel.getProperties().observe(this, props -> {
            List<CustomCastingOption> opts = new ArrayList<>();
            for (CustomCreatureAction p : props) {
                CustomCastingOption o = new CustomCastingOption();
                o.type = p.name;
                o.desc = p.desc;
                opts.add(o);
            }
            propertiesAdapter.submitList(opts);
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

    private void showAddPropertyDialog() {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_creature_action, null);
        EditText etPropName = dv.findViewById(R.id.etActionName);
        EditText etPropDesc = dv.findViewById(R.id.etActionDesc);
        dv.findViewById(R.id.spinnerActionType).setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle("Add Property")
                .setView(dv)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etPropName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    CustomCreatureAction action = new CustomCreatureAction();
                    action.name = name;
                    action.desc = etPropDesc.getText().toString().trim();
                    List<CustomCreatureAction> list = new ArrayList<>(viewModel.getProperties().getValue());
                    list.add(action);
                    viewModel.setProperties(list);
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
            List<CustomCreatureAction> props = viewModel.getProperties().getValue();
            if (props != null) {
                for (CustomCreatureAction action : props) {
                    ItemDto.WeaponEmbedDto.WeaponPropertyDto wp = new ItemDto.WeaponEmbedDto.WeaponPropertyDto();
                    wp.detail = action.desc;
                    ItemDto.WeaponEmbedDto.WeaponPropertyDto.PropertyDetailDto detail = new ItemDto.WeaponEmbedDto.WeaponPropertyDto.PropertyDetailDto();
                    detail.name = action.name;
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