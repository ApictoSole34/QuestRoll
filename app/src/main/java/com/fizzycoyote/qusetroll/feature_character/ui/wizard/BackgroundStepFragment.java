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
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterCreationDTO;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundEntity;
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
    private List<Object> combinedList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_background, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
                Object selected = combinedList.get(position);
                if (selected instanceof BackgroundEntity) {
                    handleOpen5eBackground((BackgroundEntity) selected);
                } else if (selected instanceof CustomBackgroundEntity) {
                    handleCustomBackground((CustomBackgroundEntity) selected);
                }
                showDescription(selected);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        nextButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.next_action));
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadBackgrounds() {
        new Thread(() -> {
            if (!isAdded()) return;

            List<BackgroundEntity> open5eList = Open5eDatabase.getInstance(requireContext())
                    .backgroundDao()
                    .getByGameSystem(viewModel.gameSystem);

            List<CustomBackgroundEntity> customList = UserContentDatabase.getInstance(requireContext())
                    .customBackgroundDao()
                    .getByGameSystemSync(viewModel.gameSystem);

            combinedList.clear();
            combinedList.addAll(open5eList);
            combinedList.addAll(customList);
            combinedList.sort((a, b) -> {
                String nameA = (a instanceof BackgroundEntity) ? ((BackgroundEntity) a).name : ((CustomBackgroundEntity) a).name;
                String nameB = (b instanceof BackgroundEntity) ? ((BackgroundEntity) b).name : ((CustomBackgroundEntity) b).name;
                return nameA.compareTo(nameB);
            });

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(requireContext(),
                        android.R.layout.simple_spinner_item, combinedList) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getView(position, convertView, parent);
                        Object item = getItem(position);
                        if (item instanceof BackgroundEntity) {
                            tv.setText(((BackgroundEntity) item).name);
                        } else if (item instanceof CustomBackgroundEntity) {
                            tv.setText(((CustomBackgroundEntity) item).name);
                        }
                        return tv;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                        Object item = getItem(position);
                        if (item instanceof BackgroundEntity) {
                            tv.setText(((BackgroundEntity) item).name);
                        } else if (item instanceof CustomBackgroundEntity) {
                            tv.setText(((CustomBackgroundEntity) item).name);
                        }
                        return tv;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                backgroundSpinner.setAdapter(adapter);

                if (viewModel.backgroundKey != null) {
                    for (int i = 0; i < combinedList.size(); i++) {
                        Object obj = combinedList.get(i);
                        String key = (obj instanceof BackgroundEntity) ? ((BackgroundEntity) obj).key
                                : "custom_" + ((CustomBackgroundEntity) obj).id;
                        if (key.equals(viewModel.backgroundKey)) {
                            backgroundSpinner.setSelection(i);
                            break;
                        }
                    }
                }
                if (!combinedList.isEmpty() && backgroundSpinner.getSelectedItem() == null) {
                    backgroundSpinner.setSelection(0);
                }
                showDescription(backgroundSpinner.getSelectedItem());
            });
        }).start();
    }

    private void handleOpen5eBackground(BackgroundEntity selected) {
        viewModel.backgroundKey = selected.key;
        if (selected.benefitsJson != null && !selected.benefitsJson.isEmpty()) {
            BenefitParser.ParsedBenefits benefits = BenefitParser.parseBenefits(selected.benefitsJson);
            viewModel.backgroundEquipmentDescription = benefits.equipmentDescription;
            viewModel.startingGold = benefits.gold;
            viewModel.backgroundFixedLanguages.clear();
            viewModel.backgroundFixedLanguages.addAll(benefits.fixedLanguages);
            viewModel.backgroundLanguageChoices = benefits.languageChoices;
            viewModel.backgroundSkillProficiencies = benefits.skillProficiencies;

            viewModel.characterTraits.removeIf(t -> "BACKGROUND".equals(t.sourceType));
            viewModel.characterTraits.addAll(parseFeaturesFromBackground(selected));
        } else {
            clearBackgroundData();
        }
    }

    private void handleCustomBackground(CustomBackgroundEntity custom) {
        viewModel.backgroundKey = "custom_" + custom.id;
        viewModel.backgroundEquipmentDescription = "Custom background equipment";
        viewModel.startingGold = custom.startingGold;
        viewModel.backgroundGold = custom.startingGold;

        if (custom.equipmentJson != null && !custom.equipmentJson.isEmpty()) {
            Type itemType = new TypeToken<List<CharacterCreationDTO.InventoryItemDTO>>() {}.getType();
            List<CharacterCreationDTO.InventoryItemDTO> items = new Gson().fromJson(custom.equipmentJson, itemType);
            viewModel.backgroundEquipment.clear();
            viewModel.backgroundEquipment.addAll(items);
        } else {
            viewModel.backgroundEquipment.clear();
        }

        if (custom.languagesJson != null && !custom.languagesJson.isEmpty()) {
            Type langType = new TypeToken<List<String>>() {}.getType();
            List<String> langs = new Gson().fromJson(custom.languagesJson, langType);
            viewModel.backgroundFixedLanguages.clear();
            viewModel.backgroundFixedLanguages.addAll(langs);
        } else {
            viewModel.backgroundFixedLanguages.clear();
        }
        viewModel.backgroundLanguageChoices = 0;

        if (custom.skillProficienciesJson != null && !custom.skillProficienciesJson.isEmpty()) {
            Type skillType = new TypeToken<List<String>>() {}.getType();
            List<String> skills = new Gson().fromJson(custom.skillProficienciesJson, skillType);
            viewModel.backgroundSkillProficiencies.clear();
            viewModel.backgroundSkillProficiencies.addAll(skills);
        } else {
            viewModel.backgroundSkillProficiencies.clear();
        }

        viewModel.characterTraits.removeIf(t -> "BACKGROUND".equals(t.sourceType));
        if (custom.featuresJson != null && !custom.featuresJson.isEmpty()) {
            Type featureType = new TypeToken<List<CharacterTraitEntity>>() {}.getType();
            List<CharacterTraitEntity> features = new Gson().fromJson(custom.featuresJson, featureType);
            for (CharacterTraitEntity t : features) {
                t.sourceType = "BACKGROUND";
                t.sourceKey = custom.key;
                t.levelRequirement = 1;
            }
            viewModel.characterTraits.addAll(features);
        }
    }

    private void clearBackgroundData() {
        viewModel.backgroundEquipmentDescription = "";
        viewModel.startingGold = 0;
        viewModel.backgroundFixedLanguages.clear();
        viewModel.backgroundLanguageChoices = 0;
        viewModel.backgroundSkillProficiencies.clear();
        viewModel.characterTraits.removeIf(t -> "BACKGROUND".equals(t.sourceType));
    }

    private List<CharacterTraitEntity> parseFeaturesFromBackground(BackgroundEntity background) {
        List<CharacterTraitEntity> traits = new ArrayList<>();
        if (background.benefitsJson == null) return traits;
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> benefits = gson.fromJson(background.benefitsJson, listType);
            int order = 0;
            for (Map<String, Object> benefit : benefits) {
                if ("feature".equals(benefit.get("type"))) {
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

    private void showDescription(Object selected) {
        if (selected instanceof BackgroundEntity) {
            String desc = ((BackgroundEntity) selected).desc;
            descriptionText.setText(desc != null ? desc : "No description");
        } else if (selected instanceof CustomBackgroundEntity) {
            String desc = ((CustomBackgroundEntity) selected).desc;
            descriptionText.setText(desc != null ? desc : "No description");
        } else {
            descriptionText.setText("");
        }
    }
}