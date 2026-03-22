package com.fizzycoyote.qusetroll.feature_background.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundDto;
import com.fizzycoyote.qusetroll.feature_background.view_model.CustomBackgroundCreateViewModel;
import com.fizzycoyote.qusetroll.feature_spell.adapter.CastingOptionAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomBackgroundCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_background_id";

    private static final String[] BENEFIT_TYPES = {
            "feature", "skill_proficiency", "tool_proficiency", "language",
            "equipment", "ability_score", "adventures_and_advancement",
            "connection_and_memento", "other"
    };

    private CustomBackgroundCreateViewModel viewModel;
    private CastingOptionAdapter benefitsAdapter;
    private TextInputEditText etName, etDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_background_create);

        long editId = getIntent().getLongExtra(EXTRA_EDIT_ID,
                CustomBackgroundCreateViewModel.NO_ID);
        viewModel = new ViewModelProvider(this,
                new CustomBackgroundCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customBackgroundDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomBackgroundCreateViewModel.class);

        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);

        RecyclerView rv = findViewById(R.id.rvBenefits);
        benefitsAdapter = new CastingOptionAdapter(pos -> viewModel.removeBenefit(pos));
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(benefitsAdapter);
        rv.setNestedScrollingEnabled(false);

        viewModel.getEditData().observe(this, b -> {
            if (b == null) return;
            etName.setText(b.name);
            etDesc.setText(b.desc);
        });

        viewModel.getBenefits().observe(this, benefits -> {
            List<CustomCastingOption> opts = new ArrayList<>();
            for (BackgroundDto.BenefitDto b : benefits) {
                CustomCastingOption o = new CustomCastingOption();
                o.type = b.name;
                o.desc = b.desc;
                opts.add(o);
            }
            benefitsAdapter.submitList(opts);
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, viewModel.isEditMode()
                        ? "Background updated!" : "Background saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A background with this name already exists.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        setTitle(viewModel.isEditMode() ? "Edit Background" : "Create Background");
        ((MaterialButton) findViewById(R.id.btnSave))
                .setText(viewModel.isEditMode() ? "Update Background" : "Save Background");

        findViewById(R.id.btnAddBenefit).setOnClickListener(v -> showAddBenefitDialog());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void showAddBenefitDialog() {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_background_benefit, null);
        EditText etBenefitName = dv.findViewById(R.id.etBenefitName);
        EditText etBenefitDesc = dv.findViewById(R.id.etBenefitDesc);
        Spinner spinnerType = dv.findViewById(R.id.spinnerBenefitType);
        spinnerType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, BENEFIT_TYPES));

        new AlertDialog.Builder(this)
                .setTitle("Add Benefit")
                .setView(dv)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etBenefitName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    viewModel.addBenefit(
                            name,
                            etBenefitDesc.getText().toString().trim(),
                            BENEFIT_TYPES[spinnerType.getSelectedItemPosition()]
                    );
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void save() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) { etName.setError("Required"); return; }
        String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
        viewModel.save(name, desc);
    }
}