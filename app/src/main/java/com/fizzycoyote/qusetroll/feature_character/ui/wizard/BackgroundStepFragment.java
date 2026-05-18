package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundEntity;
import com.fizzycoyote.qusetroll.feature_character.utils.BenefitParser;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackgroundStepFragment extends Fragment {
    private Spinner backgroundSpinner;
    private TextView descriptionText;
    private WizardViewModel viewModel;
    private List<BackgroundEntity> backgroundList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_background, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        backgroundSpinner = view.findViewById(R.id.background_spinner);
        descriptionText = view.findViewById(R.id.background_description);

        Button nextButton = view.findViewById(R.id.next_button);
        Button backButton = view.findViewById(R.id.back_button);

        loadBackgrounds();

        backgroundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (backgroundList != null && position < backgroundList.size()) {
                    BackgroundEntity selected = backgroundList.get(position);
                    viewModel.backgroundKey = selected.key;

                    if (selected.benefitsJson != null && !selected.benefitsJson.isEmpty()) {
                        BenefitParser.ParsedBenefits benefits = BenefitParser.parseBenefits(selected.benefitsJson);
                        // Store equipment description (for display) and gold
                        viewModel.backgroundEquipmentDescription = benefits.equipmentDescription;
                        viewModel.startingGold = benefits.gold;

                        // Process languages
                        viewModel.backgroundFixedLanguages.clear();
                        viewModel.backgroundFixedLanguages.addAll(benefits.fixedLanguages);
                        viewModel.backgroundLanguageChoices = benefits.languageChoices;

                        viewModel.backgroundSkillProficiencies = benefits.skillProficiencies;
                        viewModel.characterTraits.addAll(parseFeaturesFromBackground(selected));
                    } else {
                        viewModel.backgroundEquipmentDescription = "";
                        viewModel.startingGold = 0;
                        viewModel.backgroundFixedLanguages.clear();
                        viewModel.backgroundLanguageChoices = 0;
                    }
                    showDescription();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        nextButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.next_action));
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadBackgrounds() {
        new Thread(() -> {
            backgroundList = Open5eDatabase.getInstance(requireContext())
                    .backgroundDao()
                    .getByGameSystem(viewModel.gameSystem);
            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<BackgroundEntity> adapter = new ArrayAdapter<BackgroundEntity>(requireContext(),
                        android.R.layout.simple_spinner_item, backgroundList) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        BackgroundEntity item = getItem(position);
                        view.setText(item != null ? item.name : "");
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                        BackgroundEntity item = getItem(position);
                        view.setText(item != null ? item.name : "");
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                backgroundSpinner.setAdapter(adapter);

                if (viewModel.backgroundKey != null) {
                    for (int i = 0; i < backgroundList.size(); i++) {
                        if (backgroundList.get(i).key.equals(viewModel.backgroundKey)) {
                            backgroundSpinner.setSelection(i);
                            break;
                        }
                    }
                }
                showDescription();
            });
        }).start();
    }

    private List<CharacterTraitEntity> parseFeaturesFromBackground(BackgroundEntity background) {
        List<CharacterTraitEntity> traits = new ArrayList<>();
        if (background.benefitsJson == null) return traits;
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String, Object>> benefits = gson.fromJson(background.benefitsJson, listType);
            int order = 0;
            for (Map<String, Object> benefit : benefits) {
                String type = (String) benefit.get("type");
                if ("feature".equals(type)) {
                    String name = (String) benefit.get("name");
                    String desc = (String) benefit.get("desc");
                    if (name == null || desc == null) continue;
                    CharacterTraitEntity t = new CharacterTraitEntity();
                    t.sourceType = "BACKGROUND";
                    t.sourceKey = background.key;
                    t.name = name;
                    t.description = desc;
                    t.levelRequirement = 1;
                    t.displayOrder = order++;
                    traits.add(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return traits;
    }

    private void showDescription() {
        if (backgroundList != null && viewModel.backgroundKey != null) {
            for (BackgroundEntity bg : backgroundList) {
                if (bg.key.equals(viewModel.backgroundKey)) {
                    descriptionText.setText(bg.desc != null ? bg.desc : "No description");
                    return;
                }
            }
        }
        descriptionText.setText("");
    }
}