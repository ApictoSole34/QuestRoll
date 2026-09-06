package com.fizzycoyote.qusetroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassWizardViewModel;

public class ClassWizardSummaryFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private TextView tvSummary;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_wizard_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ClassWizardViewModel.class);

        tvSummary = view.findViewById(R.id.tv_summary);
        tvSummary.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));

        generateSummary();
    }

    private void generateSummary() {
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

        tvSummary.setText(sb.toString());
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void saveData() {
        // Nic nie zapisujemy – wszystko już jest w ViewModel
    }
}