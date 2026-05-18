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
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LanguagesStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private LinearLayout container;
    private Button nextButton, backButton;
    private List<LanguageEntity> allLanguages = new ArrayList<>();
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private int maxSelections = 0;
    private Set<String> alreadyKnown = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_languages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        container = view.findViewById(R.id.languages_container);
        nextButton = view.findViewById(R.id.next_button);
        backButton = view.findViewById(R.id.back_button);

        alreadyKnown.addAll(viewModel.racialFixedLanguages);
        alreadyKnown.addAll(viewModel.backgroundFixedLanguages);
        maxSelections = viewModel.backgroundLanguageChoices + viewModel.bonusLanguagesFromInt;

        loadLanguages();

        nextButton.setOnClickListener(v -> {
            viewModel.chosenBonusLanguages.clear();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    viewModel.chosenBonusLanguages.add(allLanguages.get(i).key);
                }
            }
            if (viewModel.chosenBonusLanguages.size() > maxSelections) {
                Toast.makeText(getContext(), "You can select only " + maxSelections + " languages", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void loadLanguages() {
        new Thread(() -> {
            allLanguages = Open5eDatabase.getInstance(requireContext())
                    .languageDao()
                    .getAllSync();
            requireActivity().runOnUiThread(() -> {
                container.removeAllViews();
                checkBoxes.clear();

                // Already known languages
                TextView knownHeader = new TextView(getContext());
                knownHeader.setText("Known languages (from race and background):");
                knownHeader.setPadding(0, 16, 0, 8);
                knownHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                container.addView(knownHeader);

                if (alreadyKnown.isEmpty()) {
                    TextView none = new TextView(getContext());
                    none.setText("None");
                    container.addView(none);
                } else {
                    for (String langName : alreadyKnown) {
                        TextView tv = new TextView(getContext());
                        tv.setText("• " + langName);
                        tv.setPadding(32, 4, 0, 4);
                        container.addView(tv);
                    }
                }

                // Source information
                TextView sourceInfo = new TextView(getContext());
                sourceInfo.setText(String.format(
                        "From background you can choose: %d language(s)\nFrom Intelligence: +%d language(s)\nTotal to choose: %d",
                        viewModel.backgroundLanguageChoices, viewModel.bonusLanguagesFromInt, maxSelections));
                sourceInfo.setPadding(0, 16, 0, 8);
                sourceInfo.setTextColor(0xFF666666);
                container.addView(sourceInfo);

                // Additional languages to choose
                if (maxSelections > 0) {
                    TextView selectHeader = new TextView(getContext());
                    selectHeader.setText("Select additional languages (max " + maxSelections + "):");
                    selectHeader.setPadding(0, 24, 0, 8);
                    selectHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                    container.addView(selectHeader);

                    for (LanguageEntity lang : allLanguages) {
                        if (alreadyKnown.contains(lang.name)) continue;
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(lang.name);
                        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked && getCheckedCount() > maxSelections) {
                                cb.setChecked(false);
                                Toast.makeText(getContext(), "You can select only " + maxSelections + " languages", Toast.LENGTH_SHORT).show();
                            }
                        });
                        container.addView(cb);
                        checkBoxes.add(cb);
                    }
                } else {
                    TextView info = new TextView(getContext());
                    info.setText("No additional languages to choose.");
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