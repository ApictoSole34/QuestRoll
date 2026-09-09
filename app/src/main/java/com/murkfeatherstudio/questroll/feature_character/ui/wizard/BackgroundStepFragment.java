package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardBackgroundBinding;
import com.murkfeatherstudio.questroll.feature_character.utils.BenefitParser;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackgroundStepFragment extends Fragment {

    private FragmentWizardBackgroundBinding binding;
    private WizardViewModel viewModel;
    private List<Object> combinedList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardBackgroundBinding.inflate(inflater, container, false);
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

        binding.backgroundDescription.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.backgroundDescription.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        loadBackgrounds();

        binding.backgroundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        binding.nextButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.next_action));
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadBackgrounds() {
        AppExecutors.getInstance().diskIO().execute(() -> {
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
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;

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
                        tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
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
                        tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return tv;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.backgroundSpinner.setAdapter(adapter);

                if (viewModel.backgroundKey != null) {
                    for (int i = 0; i < combinedList.size(); i++) {
                        Object obj = combinedList.get(i);
                        String key = (obj instanceof BackgroundEntity) ? ((BackgroundEntity) obj).key
                                : "custom_" + ((CustomBackgroundEntity) obj).id;
                        if (key.equals(viewModel.backgroundKey)) {
                            binding.backgroundSpinner.setSelection(i);
                            break;
                        }
                    }
                }
                if (!combinedList.isEmpty() && binding.backgroundSpinner.getSelectedItem() == null) {
                    binding.backgroundSpinner.setSelection(0);
                }
                showDescription(binding.backgroundSpinner.getSelectedItem());
            });
        });
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
        viewModel.backgroundEquipmentDescription = custom.equipmentDescription != null ? custom.equipmentDescription : "Custom background equipment";
        viewModel.backgroundLanguagesDescription = custom.languagesDescription;
        viewModel.startingGold = custom.startingGold;
        viewModel.backgroundGold = custom.startingGold;

        if (custom.equipmentJson != null && !custom.equipmentJson.isEmpty()) {
            try {
                Type itemType = new TypeToken<List<String>>() {}.getType();
                List<String> items = new Gson().fromJson(custom.equipmentJson, itemType);
                viewModel.backgroundFixedItems.clear();
                viewModel.backgroundFixedItems.addAll(items);
            } catch (Exception e) {
                viewModel.backgroundFixedItems.clear();
            }
        } else {
            viewModel.backgroundFixedItems.clear();
        }

        if (custom.languagesJson != null && !custom.languagesJson.isEmpty()) {
            try {
                Type langType = new TypeToken<List<String>>(){}.getType();
                List<String> langs = new Gson().fromJson(custom.languagesJson, langType);
                viewModel.backgroundFixedLanguages.clear();
                viewModel.backgroundFixedLanguages.addAll(langs);
            } catch (Exception e) {
                viewModel.backgroundFixedLanguages.clear();
            }
        } else {
            viewModel.backgroundFixedLanguages.clear();
        }
        viewModel.backgroundLanguageChoices = custom.languageChoices;

        if (custom.skillProficienciesJson != null && !custom.skillProficienciesJson.isEmpty()) {
            try {
                Type skillType = new TypeToken<List<String>>(){}.getType();
                List<String> skills = new Gson().fromJson(custom.skillProficienciesJson, skillType);
                viewModel.backgroundSkillProficiencies.clear();
                viewModel.backgroundSkillProficiencies.addAll(skills);
            } catch (Exception e) {
                viewModel.backgroundSkillProficiencies.clear();
            }
        } else {
            viewModel.backgroundSkillProficiencies.clear();
        }

        viewModel.characterTraits.removeIf(t -> "BACKGROUND".equals(t.sourceType));
        if (custom.featuresJson != null && !custom.featuresJson.isEmpty()) {
            try {
                Type featureType = new TypeToken<List<CharacterTraitEntity>>(){}.getType();
                List<CharacterTraitEntity> features = new Gson().fromJson(custom.featuresJson, featureType);
                for (CharacterTraitEntity t : features) {
                    t.sourceType = "BACKGROUND";
                    t.sourceKey = custom.key;
                    t.levelRequirement = 1;
                }
                viewModel.characterTraits.addAll(features);
            } catch (Exception e) {
                // ignore
            }
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
        if (binding == null) return;
        if (selected instanceof BackgroundEntity) {
            String desc = ((BackgroundEntity) selected).desc;
            binding.backgroundDescription.setText(desc != null ? desc : "No description");
        } else if (selected instanceof CustomBackgroundEntity) {
            String desc = ((CustomBackgroundEntity) selected).desc;
            binding.backgroundDescription.setText(desc != null ? desc : "No description");
        } else {
            binding.backgroundDescription.setText("");
        }
    }
}
