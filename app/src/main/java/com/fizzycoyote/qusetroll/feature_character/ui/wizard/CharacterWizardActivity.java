package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;

public class CharacterWizardActivity extends AppCompatActivity {

    private WizardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_wizard);

        viewModel = new ViewModelProvider(this).get(WizardViewModel.class);

        // Check if editing an existing character
        long characterId = getIntent().getLongExtra("character_id", -1);
        boolean editMode = getIntent().getBooleanExtra("edit_mode", false);
        if (editMode && characterId != -1) {
            viewModel.setEditMode(characterId, this);
        }

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_wizard);
        NavController navController = navHost.getNavController();
        // Optionally set up action bar
    }
}