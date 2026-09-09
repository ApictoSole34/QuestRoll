package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardIdentityBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;


public class IdentityStepFragment extends Fragment {
    private FragmentWizardIdentityBinding binding;
    private WizardViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardIdentityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        
        binding.characterNameInput.setText(viewModel.characterName);
        binding.characterNameInput.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));

        binding.nextButton.setOnClickListener(v -> {
            viewModel.characterName = binding.characterNameInput.getText().toString().trim();
            if (viewModel.characterName.isEmpty()) {
                binding.characterNameInput.setError("Name is required");
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }
}