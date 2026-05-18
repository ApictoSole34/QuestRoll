package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;


public class IdentityStepFragment extends Fragment {
    private EditText nameInput;
    private WizardViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_identity, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        nameInput = view.findViewById(R.id.character_name_input);
        nameInput.setText(viewModel.characterName);

        Button nextButton = view.findViewById(R.id.next_button);
        Button backButton = view.findViewById(R.id.back_button);

        nextButton.setOnClickListener(v -> {
            viewModel.characterName = nameInput.getText().toString().trim();
            if (viewModel.characterName.isEmpty()) {
                nameInput.setError("Name is required");
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }
}