package com.murkfeatherstudio.questroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.databinding.FragmentClassWizardSummaryBinding;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

public class ClassWizardSummaryFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private FragmentClassWizardSummaryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentClassWizardSummaryBinding.inflate(inflater, container, false);
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
        viewModel = new ViewModelProvider(requireActivity()).get(ClassWizardViewModel.class);

        binding.tvSummary.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));

        generateSummary();
    }

    private void generateSummary() {
        if (binding == null) return;
        StringBuilder sb = new StringBuilder();

        // Basic Info
        sb.append("=== BASIC INFO ===\n");
        sb.append("Name: ").append(viewModel.className).append("\n");
        sb.append("Hit Dice: ").append(viewModel.hitDice).append("\n");
        sb.append("Caster Type: ").append(viewModel.casterType).append("\n");
        if (!"NONE".equals(viewModel.spellcastingAbility)) {
            sb.append("Spellcasting Ability: ").append(viewModel.spellcastingAbility).append("\n");
        }
        sb.append("\n");

        // Proficiencies
        sb.append("=== PROFICIENCIES ===\n");
        sb.append("Saving Throws: ");
        if (viewModel.savingThrows.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", viewModel.savingThrows));
        }
        sb.append("\n");

        sb.append("Skill Choices: ").append(viewModel.skillChoicesCount).append("\n");
        sb.append("Skill Options: ");
        if (viewModel.skillOptions.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", viewModel.skillOptions));
        }
        sb.append("\n\n");

        // Progression
        sb.append("=== PROGRESSION ===\n");
        for (ClassWizardViewModel.ClassProgressionRow row : viewModel.progression) {
            sb.append("Level ").append(row.level)
                    .append(" | Prof +").append(row.proficiencyBonus);
            if (row.cantripsKnown > 0) {
                sb.append(" | Cantrips ").append(row.cantripsKnown);
            }
            for (int lvl = 1; lvl <= 9; lvl++) {
                int slots = row.getSlotForLevel(lvl);
                if (slots > 0) {
                    sb.append(" | ").append(lvl).append(": ").append(slots);
                }
            }
            sb.append("\n");
        }
        sb.append("\n");

        // Features
        sb.append("=== FEATURES ===\n");
        if (viewModel.features.isEmpty()) {
            sb.append("No features added yet.\n");
        } else {
            for (int i = 0; i < viewModel.features.size(); i++) {
                sb.append(i + 1).append(". ")
                        .append(viewModel.features.get(i).name).append("\n");
            }
        }
        sb.append("\n");

        // Spells
        sb.append("=== SPELLS ===\n");
        sb.append(viewModel.spellKeys.size()).append(" spell(s) attached to this class\n\n");

        // Equipment
        sb.append("=== EQUIPMENT ===\n");
        sb.append("Starting Gold: ").append(viewModel.startingGoldDice).append("\n");
        sb.append("Equipment Description: ")
                .append(viewModel.equipmentDescription.isEmpty() ? "none" : viewModel.equipmentDescription)
                .append("\n");

        binding.tvSummary.setText(sb.toString());
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void saveData() {
        // Nothing to save – everything is already in the ViewModel
    }
}
