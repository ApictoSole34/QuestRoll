package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;

public class GameSystemStepFragment extends Fragment {

    private WizardViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_game_system, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        // Force game system to 5e-2014 (the only supported version)
        viewModel.gameSystem = "5e-2014";

        Button nextButton = view.findViewById(R.id.next_button);
        nextButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.next_action));
    }
}