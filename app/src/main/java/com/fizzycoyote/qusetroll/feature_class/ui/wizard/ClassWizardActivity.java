package com.fizzycoyote.qusetroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class ClassWizardActivity extends BaseActivity {

    private ClassWizardViewModel viewModel;
    private int currentStep = 0;
    private List<Fragment> steps = new ArrayList<>();

    private TextView tvStepIndicator;
    private ProgressBar progressBar;
    private Button btnBack, btnNext, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_wizard);

        viewModel = new ViewModelProvider(this).get(ClassWizardViewModel.class);

        initViews();
        setupListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentStep > 0) {
                    currentStep--;
                    showStep(currentStep);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        long editClassId = getIntent().getLongExtra("edit_class_id", -1);
        boolean isSubclass = getIntent().getBooleanExtra("is_subclass", false);

        if (editClassId != -1) {
            // Tryb edycji: nie budujemy kroków dopóki dane nie wczytają się z bazy,
            // inaczej fragmenty łapią puste pola, zanim wątek skończy ładowanie.
            viewModel.editDataReady.observe(this, ready -> {
                if (ready != null && ready && steps.isEmpty()) {
                    setupSteps();
                    showStep(0);
                    setTitle("Edit Class");
                }
            });
            viewModel.loadClassForEdit(editClassId);
        } else {
            // Musi być ustawione przed setupSteps()/showStep(0), bo
            // ClassWizardSubclassInfoFragment odczytuje viewModel.isSubclass
            // już w onViewCreated() pierwszego pokazania tego kroku.
            viewModel.setSubclassMode(isSubclass);
            setupSteps();
            showStep(0);
            setTitle("Create Class");
        }
    }

    private void initViews() {
        tvStepIndicator = findViewById(R.id.tv_step_indicator);
        progressBar = findViewById(R.id.progress_bar);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupSteps() {
        steps.add(new ClassWizardBasicInfoFragment());
        steps.add(new ClassWizardGameSystemFragment());
        steps.add(new ClassWizardSubclassInfoFragment());
        steps.add(new ClassWizardProficienciesFragment());
        steps.add(new ClassWizardFeaturesFragment());
        steps.add(new ClassWizardProgressionFragment());
        steps.add(new ClassWizardSpellsFragment());
        steps.add(new ClassWizardSummaryFragment());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                showStep(currentStep);
            }
        });

        btnNext.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof ClassWizardStep) {
                if (!((ClassWizardStep) currentFragment).validate()) {
                    return;
                }
            }

            if (currentStep < steps.size() - 1) {
                currentStep++;
                showStep(currentStep);
            } else {
                saveClass();
            }
        });

        btnSave.setOnClickListener(v -> saveClass());
    }

    private void showStep(int step) {
        saveCurrentStepData();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, steps.get(step))
                .commit();

        String stepText = "Step " + (step + 1) + " of " + steps.size();
        tvStepIndicator.setText(stepText);

        progressBar.setMax(steps.size());
        progressBar.setProgress(step + 1);

        btnBack.setVisibility(step > 0 ? View.VISIBLE : View.GONE);

        boolean isLastStep = (step == steps.size() - 1);
        btnNext.setVisibility(isLastStep ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(isLastStep ? View.VISIBLE : View.GONE);
    }

    private void saveCurrentStepData() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ClassWizardStep) {
            ((ClassWizardStep) currentFragment).saveData();
        }
    }

    private void saveClass() {
        saveCurrentStepData();

        viewModel.saveClass(result -> {
            if (result) {
                Toast.makeText(this,
                        viewModel.isEditMode() ? "Class updated!" : "Class created!",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "A class with this name already exists.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface ClassWizardStep {
        boolean validate();
        void saveData();
    }
}