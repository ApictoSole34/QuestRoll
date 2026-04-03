package com.fizzycoyote.qusetroll.feature_creature.ui;

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
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomCastingOption;
import com.fizzycoyote.qusetroll.feature_creature.viewmodel.CustomCreatureCreateViewModel;
import com.fizzycoyote.qusetroll.feature_spell.adapter.CastingOptionAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomCreatureCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_creature_id";

    private static final String[] SIZES = {
            "Tiny", "Small", "Medium", "Large", "Huge", "Gargantuan"
    };
    private static final String[] ALIGNMENTS = {
            "", "lawful good", "neutral good", "chaotic good",
            "lawful neutral", "neutral", "chaotic neutral",
            "lawful evil", "neutral evil", "chaotic evil",
            "unaligned", "any alignment"
    };
    private static final String[] ACTION_TYPES = {
            "ACTION", "BONUS_ACTION", "REACTION", "LEGENDARY_ACTION"
    };

    private CustomCreatureCreateViewModel viewModel;
    private CastingOptionAdapter actionsAdapter;  // reuse adapter — same layout
    private CastingOptionAdapter traitsAdapter;

    private TextInputEditText etName, etCrText, etHp, etHitDice,
            etArmorClass, etArmorDetail, etLanguages,
            etSpeedWalk, etSpeedFly, etSpeedSwim, etSpeedBurrow, etSpeedClimb,
            etStr, etDex, etCon, etInt, etWis, etCha,
            etDmgImmunities, etDmgResistances, etDmgVulnerabilities, etCondImmunities,
            etDarkvision, etBlindsight, etTremorsense, etTruesight;
    private AutoCompleteTextView actvType, actvSize, actvAlignment;
    private CheckBox cbHover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_creature_create);

        UserContentDatabase db = UserContentDatabase.getInstance(this);
        long editId = getIntent().getLongExtra(EXTRA_EDIT_ID, CustomCreatureCreateViewModel.NO_ID);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new CustomCreatureCreateViewModel.Factory(
                        db.customCreatureDao(),
                        db.customCreatureTypeDao(),
                        open5eDb.creatureTypeDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )
        ).get(CustomCreatureCreateViewModel.class);

        initViews();
        setupDropdowns();
        setupActionsRecycler();
        setupObservers();

        setTitle(viewModel.isEditMode() ? "Edit Creature" : "Create Creature");
        ((MaterialButton) findViewById(R.id.btnSave))
                .setText(viewModel.isEditMode() ? "Update Creature" : "Save Creature");

        findViewById(R.id.btnAddAction).setOnClickListener(v -> showAddActionDialog(false));
        findViewById(R.id.btnAddTrait).setOnClickListener(v -> showAddActionDialog(true));
        findViewById(R.id.btnSave).setOnClickListener(v -> saveCreature());
    }

    private void initViews() {
        etName = find(R.id.etName);
        etCrText = find(R.id.etCrText);
        etHp = find(R.id.etHp);
        etHitDice = find(R.id.etHitDice);
        etArmorClass = find(R.id.etArmorClass);
        etArmorDetail = find(R.id.etArmorDetail);
        etLanguages = find(R.id.etLanguages);
        etSpeedWalk = find(R.id.etSpeedWalk);
        etSpeedFly = find(R.id.etSpeedFly);
        etSpeedSwim = find(R.id.etSpeedSwim);
        etSpeedBurrow = find(R.id.etSpeedBurrow);
        etSpeedClimb = find(R.id.etSpeedClimb);
        etStr = find(R.id.etStr); etDex = find(R.id.etDex); etCon = find(R.id.etCon);
        etInt = find(R.id.etInt); etWis = find(R.id.etWis); etCha = find(R.id.etCha);
        etDmgImmunities = find(R.id.etDmgImmunities);
        etDmgResistances = find(R.id.etDmgResistances);
        etDmgVulnerabilities = find(R.id.etDmgVulnerabilities);
        etCondImmunities = find(R.id.etCondImmunities);
        etDarkvision = find(R.id.etDarkvision);
        etBlindsight = find(R.id.etBlindsight);
        etTremorsense = find(R.id.etTremorsense);
        etTruesight = find(R.id.etTruesight);
        actvType = findViewById(R.id.actvType);
        actvSize = findViewById(R.id.actvSize);
        actvAlignment = findViewById(R.id.actvAlignment);
        cbHover = findViewById(R.id.cbHover);
    }

    private void setupDropdowns() {
        actvSize.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, SIZES));
        actvAlignment.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, ALIGNMENTS));
    }

    private void setupActionsRecycler() {
        RecyclerView rvActions = findViewById(R.id.rvActions);
        actionsAdapter = new CastingOptionAdapter(pos -> viewModel.removeAction(pos));
        rvActions.setLayoutManager(new LinearLayoutManager(this));
        rvActions.setAdapter(actionsAdapter);
        rvActions.setNestedScrollingEnabled(false);

        RecyclerView rvTraits = findViewById(R.id.rvTraits);
        traitsAdapter = new CastingOptionAdapter(pos -> viewModel.removeTrait(pos));
        rvTraits.setLayoutManager(new LinearLayoutManager(this));
        rvTraits.setAdapter(traitsAdapter);
        rvTraits.setNestedScrollingEnabled(false);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, this::populateForm);

        viewModel.getAllTypeNames().observe(this, types ->
                actvType.setAdapter(new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, types)));

        viewModel.getActions().observe(this, actions -> {
            List<CustomCastingOption> opts = new ArrayList<>();
            for (CustomCreatureAction a : actions) {
                CustomCastingOption o = new CustomCastingOption();
                o.type = a.actionType + ": " + a.name;
                o.desc = a.desc;
                opts.add(o);
            }
            actionsAdapter.submitList(opts);
        });

        viewModel.getTraits().observe(this, traits -> {
            List<CustomCastingOption> opts = new ArrayList<>();
            for (CustomCreatureAction t : traits) {
                CustomCastingOption o = new CustomCastingOption();
                o.type = t.name;
                o.desc = t.desc;
                opts.add(o);
            }
            traitsAdapter.submitList(opts);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, viewModel.isEditMode()
                        ? "Creature updated!" : "Creature saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A creature with this name already exists.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateForm(CustomCreatureEntity c) {
        if (c == null) return;
        setText(etName, c.name);
        setText(etCrText, c.crText);
        setText(etHp, String.valueOf(c.hitPoints));
        setText(etHitDice, c.hitDice);
        setText(etArmorClass, String.valueOf(c.armorClass));
        setText(etArmorDetail, c.armorDetail);
        setText(etLanguages, c.languages);
        setText(etSpeedWalk, String.valueOf(c.speedWalk));
        setText(etSpeedFly, String.valueOf(c.speedFly));
        setText(etSpeedSwim, String.valueOf(c.speedSwim));
        setText(etSpeedBurrow, String.valueOf(c.speedBurrow));
        setText(etSpeedClimb, String.valueOf(c.speedClimb));
        setText(etStr, String.valueOf(c.str));
        setText(etDex, String.valueOf(c.dex));
        setText(etCon, String.valueOf(c.con));
        setText(etInt, String.valueOf(c.intScore));
        setText(etWis, String.valueOf(c.wis));
        setText(etCha, String.valueOf(c.cha));
        setText(etDmgImmunities, c.damageImmunities);
        setText(etDmgResistances, c.damageResistances);
        setText(etDmgVulnerabilities, c.damageVulnerabilities);
        setText(etCondImmunities, c.conditionImmunities);
        setText(etDarkvision, c.darkvisionRange > 0 ? String.valueOf(c.darkvisionRange) : "");
        setText(etBlindsight, c.blindsightRange > 0 ? String.valueOf(c.blindsightRange) : "");
        setText(etTremorsense, c.tremorsenseRange > 0 ? String.valueOf(c.tremorsenseRange) : "");
        setText(etTruesight, c.truesightRange > 0 ? String.valueOf(c.truesightRange) : "");
        actvType.setText(c.typeName, false);
        actvSize.setText(c.sizeName, false);
        actvAlignment.setText(c.alignment, false);
        cbHover.setChecked(c.speedHover);
    }

    private void saveCreature() {
        String name = getStr(etName);
        if (name.isEmpty()) { etName.setError("Required"); return; }

        CustomCreatureEntity e = new CustomCreatureEntity();
        e.name = name;
        e.crText = getStr(etCrText);
        e.crDecimal = parseCr(e.crText);
        e.typeName = actvType.getText().toString().trim();
        e.sizeName = actvSize.getText().toString().trim();
        e.alignment = actvAlignment.getText().toString().trim();
        e.hitPoints = parseInt(etHp, 0);
        e.hitDice = getStr(etHitDice);
        e.armorClass = parseInt(etArmorClass, 10);
        e.armorDetail = getStr(etArmorDetail);
        e.languages = getStr(etLanguages);
        e.speedWalk = parseInt(etSpeedWalk, 30);
        e.speedFly = parseInt(etSpeedFly, 0);
        e.speedSwim = parseInt(etSpeedSwim, 0);
        e.speedBurrow = parseInt(etSpeedBurrow, 0);
        e.speedClimb = parseInt(etSpeedClimb, 0);
        e.speedHover = cbHover.isChecked();
        e.str = parseInt(etStr, 10); e.dex = parseInt(etDex, 10); e.con = parseInt(etCon, 10);
        e.intScore = parseInt(etInt, 10); e.wis = parseInt(etWis, 10); e.cha = parseInt(etCha, 10);
        e.damageImmunities = getStr(etDmgImmunities);
        e.damageResistances = getStr(etDmgResistances);
        e.damageVulnerabilities = getStr(etDmgVulnerabilities);
        e.conditionImmunities = getStr(etCondImmunities);
        e.darkvisionRange = parseInt(etDarkvision, 0);
        e.blindsightRange = parseInt(etBlindsight, 0);
        e.tremorsenseRange = parseInt(etTremorsense, 0);
        e.truesightRange = parseInt(etTruesight, 0);

        viewModel.save(e);
    }

    private void showAddActionDialog(boolean isTrait) {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_creature_action, null);
        EditText etActionName = dv.findViewById(R.id.etActionName);
        EditText etActionDesc = dv.findViewById(R.id.etActionDesc);
        Spinner spinnerType = dv.findViewById(R.id.spinnerActionType);

        if (isTrait) spinnerType.setVisibility(View.GONE);
        else spinnerType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ACTION_TYPES));

        new AlertDialog.Builder(this)
                .setTitle(isTrait ? "Add Trait" : "Add Action")
                .setView(dv)
                .setPositiveButton("Add", (d, w) -> {
                    String n = etActionName.getText().toString().trim();
                    if (n.isEmpty()) return;
                    CustomCreatureAction a = new CustomCreatureAction();
                    a.name = n;
                    a.desc = etActionDesc.getText().toString().trim();
                    a.actionType = isTrait ? "TRAIT"
                            : ACTION_TYPES[spinnerType.getSelectedItemPosition()];
                    if (isTrait) viewModel.addTrait(a);
                    else viewModel.addAction(a);
                })
                .setNegativeButton("Cancel", null).show();
    }

    private float parseCr(String cr) {
        if (cr == null || cr.isEmpty()) return 0f;
        if (cr.contains("/")) {
            String[] p = cr.split("/");
            try { return Float.parseFloat(p[0]) / Float.parseFloat(p[1]); }
            catch (Exception e) { return 0f; }
        }
        try { return Float.parseFloat(cr); } catch (Exception e) { return 0f; }
    }

    private int parseInt(TextInputEditText et, int def) {
        try { return Integer.parseInt(getStr(et)); } catch (Exception e) { return def; }
    }

    private String getStr(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setText(TextInputEditText et, String val) {
        if (val != null) et.setText(val);
    }

    private TextInputEditText find(int id) { return findViewById(id); }
}