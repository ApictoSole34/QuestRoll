package com.murkfeatherstudio.questroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.databinding.ActivityClassWizardBinding;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class ClassWizardActivity extends BaseActivity {

    private ClassWizardViewModel viewModel;
    private int currentStep = 0;
    private List<Fragment> steps = new ArrayList<>();
    private ActivityClassWizardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClassWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ClassWizardViewModel.class);

        setupListeners();

        // Initialize Calculator Drawer Width
        setDrawerWidth(false);

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
            viewModel.editDataReady.observe(this, ready -> {
                if (ready != null && ready && steps.isEmpty()) {
                    setupSteps();
                    showStep(0);
                    setTitle("Edit Class");
                }
            });
            viewModel.loadClassForEdit(editClassId);
        } else {
            viewModel.setSubclassMode(isSubclass);
            setupSteps();
            showStep(0);
            setTitle("Create Class");
        }
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
        binding.btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                showStep(currentStep);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
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

        binding.btnSave.setOnClickListener(v -> saveClass());
    }

    private void showStep(int step) {
        saveCurrentStepData();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, steps.get(step))
                .commit();

        String stepText = "Step " + (step + 1) + " of " + steps.size();
        binding.tvStepIndicator.setText(stepText);

        binding.progressBar.setMax(steps.size());
        binding.progressBar.setProgress(step + 1);

        binding.btnBack.setVisibility(step > 0 ? View.VISIBLE : View.GONE);

        boolean isLastStep = (step == steps.size() - 1);
        binding.btnNext.setVisibility(isLastStep ? View.GONE : View.VISIBLE);
        binding.btnSave.setVisibility(isLastStep ? View.VISIBLE : View.GONE);
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