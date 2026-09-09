package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.databinding.ActivityCharacterWizardBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

/**
 * Activity for the multi-step character creation wizard.
 * Inherits from {@link BaseActivity} to support dynamic background generation.
 */
public class CharacterWizardActivity extends BaseActivity {

    private WizardViewModel viewModel;
    private ActivityCharacterWizardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCharacterWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WizardViewModel.class);

        // Initialize Calculator Drawer Width
        setDrawerWidth(false);

        long characterId = getIntent().getLongExtra("character_id", -1);
        boolean editMode = getIntent().getBooleanExtra("edit_mode", false);
        if (editMode && characterId != -1) {
            viewModel.setEditMode(characterId, this);
        }

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_wizard);
        if (navHost != null) {
            NavController navController = navHost.getNavController();
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                updateProgress(destination.getId());
            });
        }
    }

    private void updateProgress(int destinationId) {
        if (binding == null || binding.wizardProgressBar == null) return;

        int progress = 1;
        if (destinationId == R.id.gameSystemStepFragment) progress = 1;
        else if (destinationId == R.id.identityStepFragment) progress = 2;
        else if (destinationId == R.id.raceStepFragment) progress = 3;
        else if (destinationId == R.id.backgroundStepFragment) progress = 4;
        else if (destinationId == R.id.alignmentStepFragment) progress = 5;
        else if (destinationId == R.id.classStepFragment) progress = 6;
        else if (destinationId == R.id.subclassChoiceStepFragment) progress = 7;
        else if (destinationId == R.id.attributesStepFragment) progress = 8;
        else if (destinationId == R.id.languagesStepFragment) progress = 9;
        else if (destinationId == R.id.skillsStepFragment) progress = 10;
        else if (destinationId == R.id.spellsStepFragment) progress = 11;
        else if (destinationId == R.id.imageStepFragment) progress = 12;
        else if (destinationId == R.id.equipmentStepFragment) progress = 13;
        else if (destinationId == R.id.summaryStepFragment) progress = 14;

        binding.wizardProgressBar.setProgress(progress);
    }
}
