package com.fizzycoyote.qusetroll.feature_item.ui.weapon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomCastingOption;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon.CustomWeaponEntity;
import com.fizzycoyote.qusetroll.feature_item.view_model.weapon.CustomWeaponCreateViewModel;
import com.fizzycoyote.qusetroll.feature_spell.adapter.CastingOptionAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomWeaponCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_weapon_id";

    private static final String[] DAMAGE_TYPES = {
            "acid", "bludgeoning", "cold", "fire", "force",
            "lightning", "necrotic", "piercing", "poison",
            "psychic", "radiant", "slashing", "thunder"
    };

    private CustomWeaponCreateViewModel viewModel;
    private CastingOptionAdapter propertiesAdapter;

    private TextInputEditText etName, etDamageDice, etRange, etLongRange, etNotes;
    private AutoCompleteTextView actvDamageType;
    private CheckBox cbIsSimple, cbIsImprovised;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_weapon_create);

        long editId = getIntent().getLongExtra(EXTRA_EDIT_ID, CustomWeaponCreateViewModel.NO_ID);
        viewModel = new ViewModelProvider(this,
                new CustomWeaponCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customWeaponDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomWeaponCreateViewModel.class);

        initViews();
        setupObservers();
        setTitle(viewModel.isEditMode() ? "Edit Weapon" : "Create Weapon");
        ((MaterialButton) findViewById(R.id.btnSave))
                .setText(viewModel.isEditMode() ? "Update Weapon" : "Save Weapon");

        findViewById(R.id.btnAddProperty).setOnClickListener(v -> showAddPropertyDialog());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDamageDice = findViewById(R.id.etDamageDice);
        etRange = findViewById(R.id.etRange);
        etLongRange = findViewById(R.id.etLongRange);
        etNotes = findViewById(R.id.etNotes);
        actvDamageType = findViewById(R.id.actvDamageType);
        cbIsSimple = findViewById(R.id.cbIsSimple);
        cbIsImprovised = findViewById(R.id.cbIsImprovised);

        actvDamageType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, DAMAGE_TYPES));

        RecyclerView rv = findViewById(R.id.rvProperties);
        propertiesAdapter = new CastingOptionAdapter(pos -> viewModel.removeProperty(pos));
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(propertiesAdapter);
        rv.setNestedScrollingEnabled(false);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, w -> {
            if (w == null) return;
            etName.setText(w.name);
            etDamageDice.setText(w.damageDice);
            actvDamageType.setText(w.damageTypeName, false);
            etRange.setText(w.range > 0 ? String.valueOf((int) w.range) : "");
            etLongRange.setText(w.longRange > 0 ? String.valueOf((int) w.longRange) : "");
            etNotes.setText(w.notes);
            cbIsSimple.setChecked(w.isSimple);
            cbIsImprovised.setChecked(w.isImprovised);
        });

        viewModel.getProperties().observe(this, props -> {
            List<CustomCastingOption> opts = new ArrayList<>();
            for (CustomCreatureAction p : props) {
                CustomCastingOption o = new CustomCastingOption();
                o.type = p.name; o.desc = p.desc;
                opts.add(o);
            }
            propertiesAdapter.submitList(opts);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, viewModel.isEditMode()
                        ? "Weapon updated!" : "Weapon saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A weapon with this name already exists.",
                        Toast.LENGTH_SHORT).show();
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
                    viewModel.addProperty(name, etPropDesc.getText().toString().trim());
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void save() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) { etName.setError("Required"); return; }

        CustomWeaponEntity e = new CustomWeaponEntity();
        e.name = name;
        e.damageDice = etDamageDice.getText() != null ? etDamageDice.getText().toString().trim() : "";
        e.damageTypeName = actvDamageType.getText().toString().trim();
        e.isSimple = cbIsSimple.isChecked();
        e.isImprovised = cbIsImprovised.isChecked();
        e.notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        try { e.range = Float.parseFloat(etRange.getText().toString().trim()); }
        catch (Exception ex) { e.range = 0f; }
        try { e.longRange = Float.parseFloat(etLongRange.getText().toString().trim()); }
        catch (Exception ex) { e.longRange = 0f; }

        viewModel.save(e);
    }
}