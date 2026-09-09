package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardGameSystemBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

public class GameSystemStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private FragmentWizardGameSystemBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardGameSystemBinding.inflate(inflater, container, false);
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

        // Force game system to 5e-2014 (the only supported version)
        viewModel.gameSystem = "5e-2014";

        binding.nextButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.next_action));
    }
}