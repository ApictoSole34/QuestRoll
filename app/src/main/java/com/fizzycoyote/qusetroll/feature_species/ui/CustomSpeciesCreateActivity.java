package com.fizzycoyote.qusetroll.feature_species.ui;

import static android.content.Intent.getIntent;

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
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomCastingOption;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesEntity;
import com.fizzycoyote.qusetroll.feature_species.viewmodel.CustomSpeciesCreateViewModel;
import com.fizzycoyote.qusetroll.feature_spell.adapter.CastingOptionAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomSpeciesCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_species_id";

    private CustomSpeciesCreateViewModel viewModel;
    private CastingOptionAdapter traitsAdapter;

    private TextInputEditText etName, etDesc;
    private CheckBox cbIsSubspecies;
    private LinearLayout layoutParent;
    private AutoCompleteTextView actvParent;
    private List<String> parentKeys = new ArrayList<>();
    private List<String> parentNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_species_create);

        UserContentDatabase customDb = UserContentDatabase.getInstance(this);
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        long editId = getIntent().getLongExtra(EXTRA_EDIT_ID, CustomSpeciesCreateViewModel.NO_ID);

        viewModel = new ViewModelProvider(this,
                new CustomSpeciesCreateViewModel.Factory(
                        customDb.customSpeciesDao(),
                        open5eDb.speciesDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomSpeciesCreateViewModel.class);

        initViews();
        setupObservers();
        loadParentOptions(customDb, open5eDb);

        setTitle(viewModel.isEditMode() ? "Edit Species" : "Create Species");
        ((MaterialButton) findViewById(R.id.btnSave))
                .setText(viewModel.isEditMode() ? "Update Species" : "Save Species");

        cbIsSubspecies.setOnCheckedChangeListener((v, checked) ->
                layoutParent.setVisibility(checked ? View.VISIBLE : View.GONE));

        findViewById(R.id.btnAddTrait).setOnClickListener(v -> showAddTraitDialog());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        cbIsSubspecies = findViewById(R.id.cbIsSubspecies);
        layoutParent = findViewById(R.id.layoutParent);
        actvParent = findViewById(R.id.actvParent);

        RecyclerView rvTraits = findViewById(R.id.rvTraits);
        traitsAdapter = new CastingOptionAdapter(pos -> viewModel.removeTrait(pos));
        rvTraits.setLayoutManager(new LinearLayoutManager(this));
        rvTraits.setAdapter(traitsAdapter);
        rvTraits.setNestedScrollingEnabled(false);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, s -> {
            if (s == null) return;
            etName.setText(s.name);
            etDesc.setText(s.desc);
            cbIsSubspecies.setChecked(s.isSubspecies);
            layoutParent.setVisibility(s.isSubspecies ? View.VISIBLE : View.GONE);
            if (s.isSubspecies && s.subspeciesOfName != null) {
                actvParent.setText(s.subspeciesOfName, false);
            }
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
                        ? "Species updated!" : "Species saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A species with this name already exists.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadParentOptions(UserContentDatabase customDb, Open5eDatabase open5eDb) {
        runOnUiThread(() -> {
            open5eDb.speciesDao()
                    .getFilteredSpecies("", 0, 1, "")
                    .observe(this, open5eSpecies -> {
                        parentKeys.clear();
                        parentNames.clear();
                        parentKeys.add("");
                        parentNames.add("None");

                        if (open5eSpecies != null) {
                            for (SpeciesEntity s : open5eSpecies) {
                                parentKeys.add(s.key);
                                parentNames.add(s.name + " (SRD)");
                            }
                        }

                        customDb.customSpeciesDao().getAll().observe(this, customSpecies -> {
                            int open5eCount = open5eSpecies != null ? open5eSpecies.size() : 0;
                            while (parentKeys.size() > 1 + open5eCount) {
                                parentKeys.remove(parentKeys.size() - 1);
                                parentNames.remove(parentNames.size() - 1);
                            }
                            if (customSpecies != null) {
                                for (CustomSpeciesEntity cs : customSpecies) {
                                    parentKeys.add("custom_" + cs.id);
                                    parentNames.add(cs.name + " (Custom)");
                                }
                            }
                            actvParent.setAdapter(new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1, parentNames));
                        });
                    });
        });
    }

    private void showAddTraitDialog() {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_creature_action, null);
        EditText etName = dv.findViewById(R.id.etActionName);
        EditText etDesc = dv.findViewById(R.id.etActionDesc);
        dv.findViewById(R.id.spinnerActionType).setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle("Add Trait")
                .setView(dv)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    CustomCreatureAction t = new CustomCreatureAction();
                    t.name = name;
                    t.desc = etDesc.getText().toString().trim();
                    viewModel.addTrait(t);
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void save() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) { etName.setError("Required"); return; }

        CustomSpeciesEntity e = new CustomSpeciesEntity();
        e.name = name;
        e.desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
        e.isSubspecies = cbIsSubspecies.isChecked();

        if (e.isSubspecies) {
            String parentName = actvParent.getText().toString().trim();
            int idx = parentNames.indexOf(parentName);
            if (idx >= 0) {
                e.subspeciesOfKey = parentKeys.get(idx);
                e.subspeciesOfName = parentName
                        .replace(" (SRD)", "")
                        .replace(" (Custom)", "")
                        .trim();
            }
        }

        viewModel.save(e);
    }
}