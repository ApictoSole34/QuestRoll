package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.ability.skill.SkillEntity;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;

import java.util.*;

public class SkillsStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private LinearLayout container;
    private Button nextButton, backButton;
    private List<SkillEntity> allSkills = new ArrayList<>();
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private int maxSelections = 0;
    private Set<String> alreadyKnown = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_skills, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        container = view.findViewById(R.id.skills_container);
        nextButton = view.findViewById(R.id.next_button);
        backButton = view.findViewById(R.id.back_button);

        alreadyKnown.addAll(viewModel.backgroundSkillProficiencies);
        maxSelections = viewModel.classSkillChoices;

        loadSkills();

        nextButton.setOnClickListener(v -> {
            viewModel.chosenSkillProficiencies.clear();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    viewModel.chosenSkillProficiencies.add(allSkills.get(i).key);
                }
            }
            if (viewModel.chosenSkillProficiencies.size() > maxSelections) {
                Toast.makeText(getContext(), "You can select only " + maxSelections + " skills", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void loadSkills() {
        new Thread(() -> {
            allSkills = Open5eDatabase.getInstance(requireContext())
                    .skillDao()
                    .getAllSync();
            requireActivity().runOnUiThread(() -> {
                container.removeAllViews();
                checkBoxes.clear();

                // Already known skills from background
                TextView knownHeader = new TextView(getContext());
                knownHeader.setText("Skills already known (from background):");
                knownHeader.setPadding(0, 16, 0, 8);
                knownHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                container.addView(knownHeader);

                if (alreadyKnown.isEmpty()) {
                    TextView none = new TextView(getContext());
                    none.setText("None");
                    container.addView(none);
                } else {
                    for (String skillName : alreadyKnown) {
                        TextView tv = new TextView(getContext());
                        tv.setText("• " + skillName);
                        tv.setPadding(32, 4, 0, 4);
                        container.addView(tv);
                    }
                }

                // Additional skill choices
                if (maxSelections > 0) {
                    TextView selectHeader = new TextView(getContext());
                    selectHeader.setText("Select skills from class (max " + maxSelections + "):");
                    selectHeader.setPadding(0, 24, 0, 8);
                    selectHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                    container.addView(selectHeader);

                    for (SkillEntity skill : allSkills) {
                        if (!viewModel.classSkillOptions.contains(skill.name)) continue;
                        if (alreadyKnown.contains(skill.name)) continue;
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(skill.name);
                        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked && getCheckedCount() > maxSelections) {
                                cb.setChecked(false);
                                Toast.makeText(getContext(), "You can select only " + maxSelections + " skills", Toast.LENGTH_SHORT).show();
                            }
                        });
                        container.addView(cb);
                        checkBoxes.add(cb);
                    }
                } else {
                    TextView info = new TextView(getContext());
                    info.setText("No additional skills to choose.");
                    info.setPadding(0, 16, 0, 0);
                    container.addView(info);
                }
            });
        }).start();
    }

    private int getCheckedCount() {
        int count = 0;
        for (CheckBox cb : checkBoxes) if (cb.isChecked()) count++;
        return count;
    }
}