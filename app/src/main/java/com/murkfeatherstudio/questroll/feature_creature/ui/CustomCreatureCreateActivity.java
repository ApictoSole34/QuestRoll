package com.murkfeatherstudio.questroll.feature_creature.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomCastingOption;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomCreatureCreateBinding;
import com.murkfeatherstudio.questroll.databinding.DialogCreatureActionBinding;
import com.murkfeatherstudio.questroll.feature_creature.viewmodel.CustomCreatureCreateViewModel;
import com.murkfeatherstudio.questroll.feature_spell.adapter.CastingOptionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomCreatureCreateActivity extends BaseActivity {

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
    private ActivityCustomCreatureCreateBinding binding;
    private CastingOptionAdapter actionsAdapter;  // reuse adapter — same layout
    private CastingOptionAdapter traitsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomCreatureCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        setupDropdowns();
        setupActionsRecycler();
        setupObservers();

        setTitle(viewModel.isEditMode() ? "Edit Creature" : "Create Creature");
        binding.btnSave.setText(viewModel.isEditMode() ? "Update Creature" : "Save Creature");

        binding.btnAddAction.setOnClickListener(v -> showAddActionDialog(false));
        binding.btnAddTrait.setOnClickListener(v -> showAddActionDialog(true));
        binding.btnSave.setOnClickListener(v -> saveCreature());
    }

    private void setupDropdowns() {
        binding.actvSize.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, SIZES));
        binding.actvAlignment.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, ALIGNMENTS));
    }

    private void setupActionsRecycler() {
        actionsAdapter = new CastingOptionAdapter(pos -> viewModel.removeAction(pos));
        binding.rvActions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvActions.setAdapter(actionsAdapter);
        binding.rvActions.setNestedScrollingEnabled(false);

        traitsAdapter = new CastingOptionAdapter(pos -> viewModel.removeTrait(pos));
        binding.rvTraits.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTraits.setAdapter(traitsAdapter);
        binding.rvTraits.setNestedScrollingEnabled(false);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, this::populateForm);

        viewModel.getAllTypeNames().observe(this, types ->
                binding.actvType.setAdapter(new ArrayAdapter<>(this,
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
        binding.etName.setText(c.name);
        binding.etCrText.setText(c.crText);
        binding.etHp.setText(String.valueOf(c.hitPoints));
        binding.etHitDice.setText(c.hitDice);
        binding.etArmorClass.setText(String.valueOf(c.armorClass));
        binding.etArmorDetail.setText(c.armorDetail);
        binding.etLanguages.setText(c.languages);
        binding.etSpeedWalk.setText(String.valueOf(c.speedWalk));
        binding.etSpeedFly.setText(String.valueOf(c.speedFly));
        binding.etSpeedSwim.setText(String.valueOf(c.speedSwim));
        binding.etSpeedBurrow.setText(String.valueOf(c.speedBurrow));
        binding.etSpeedClimb.setText(String.valueOf(c.speedClimb));
        binding.etStr.setText(String.valueOf(c.str));
        binding.etDex.setText(String.valueOf(c.dex));
        binding.etCon.setText(String.valueOf(c.con));
        binding.etInt.setText(String.valueOf(c.intScore));
        binding.etWis.setText(String.valueOf(c.wis));
        binding.etCha.setText(String.valueOf(c.cha));
        binding.etDmgImmunities.setText(c.damageImmunities);
        binding.etDmgResistances.setText(c.damageResistances);
        binding.etDmgVulnerabilities.setText(c.damageVulnerabilities);
        binding.etCondImmunities.setText(c.conditionImmunities);
        binding.etDarkvision.setText(c.darkvisionRange > 0 ? String.valueOf(c.darkvisionRange) : "");
        binding.etBlindsight.setText(c.blindsightRange > 0 ? String.valueOf(c.blindsightRange) : "");
        binding.etTremorsense.setText(c.tremorsenseRange > 0 ? String.valueOf(c.tremorsenseRange) : "");
        binding.etTruesight.setText(c.truesightRange > 0 ? String.valueOf(c.truesightRange) : "");
        binding.actvType.setText(c.typeName, false);
        binding.actvSize.setText(c.sizeName, false);
        binding.actvAlignment.setText(c.alignment, false);
        binding.cbHover.setChecked(c.speedHover);
    }

    private void saveCreature() {
        String name = getStr(binding.etName);
        if (name.isEmpty()) { binding.etName.setError("Required"); return; }

        CustomCreatureEntity e = new CustomCreatureEntity();
        e.name = name;
        e.crText = getStr(binding.etCrText);
        e.crDecimal = parseCr(e.crText);
        e.typeName = binding.actvType.getText().toString().trim();
        e.sizeName = binding.actvSize.getText().toString().trim();
        e.alignment = binding.actvAlignment.getText().toString().trim();
        e.hitPoints = parseInt(binding.etHp, 0);
        e.hitDice = getStr(binding.etHitDice);
        e.armorClass = parseInt(binding.etArmorClass, 10);
        e.armorDetail = getStr(binding.etArmorDetail);
        e.languages = getStr(binding.etLanguages);
        e.speedWalk = parseInt(binding.etSpeedWalk, 30);
        e.speedFly = parseInt(binding.etSpeedFly, 0);
        e.speedSwim = parseInt(binding.etSpeedSwim, 0);
        e.speedBurrow = parseInt(binding.etSpeedBurrow, 0);
        e.speedClimb = parseInt(binding.etSpeedClimb, 0);
        e.speedHover = binding.cbHover.isChecked();
        e.str = parseInt(binding.etStr, 10); e.dex = parseInt(binding.etDex, 10); e.con = parseInt(binding.etCon, 10);
        e.intScore = parseInt(binding.etInt, 10); e.wis = parseInt(binding.etWis, 10); e.cha = parseInt(binding.etCha, 10);
        e.damageImmunities = getStr(binding.etDmgImmunities);
        e.damageResistances = getStr(binding.etDmgResistances);
        e.damageVulnerabilities = getStr(binding.etDmgVulnerabilities);
        e.conditionImmunities = getStr(binding.etCondImmunities);
        e.darkvisionRange = parseInt(binding.etDarkvision, 0);
        e.blindsightRange = parseInt(binding.etBlindsight, 0);
        e.tremorsenseRange = parseInt(binding.etTremorsense, 0);
        e.truesightRange = parseInt(binding.etTruesight, 0);

        viewModel.save(e);
    }

    private void showAddActionDialog(boolean isTrait) {
        DialogCreatureActionBinding dialogBinding = DialogCreatureActionBinding.inflate(getLayoutInflater());

        if (isTrait) dialogBinding.spinnerActionType.setVisibility(View.GONE);
        else dialogBinding.spinnerActionType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ACTION_TYPES));

        new AlertDialog.Builder(this)
                .setTitle(isTrait ? "Add Trait" : "Add Action")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add", (d, w) -> {
                    String n = dialogBinding.etActionName.getText().toString().trim();
                    if (n.isEmpty()) return;
                    CustomCreatureAction a = new CustomCreatureAction();
                    a.name = n;
                    a.desc = dialogBinding.etActionDesc.getText().toString().trim();
                    a.actionType = isTrait ? "TRAIT"
                            : ACTION_TYPES[dialogBinding.spinnerActionType.getSelectedItemPosition()];
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

    private int parseInt(EditText et, int def) {
        try { return Integer.parseInt(getStr(et)); } catch (Exception e) { return def; }
    }

    private String getStr(EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}