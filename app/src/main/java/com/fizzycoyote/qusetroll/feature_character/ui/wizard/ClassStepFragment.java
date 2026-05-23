package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterCreationDTO;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.feature_character.utils.BenefitParser;
import com.fizzycoyote.qusetroll.feature_character.utils.ClassCastingAbility;
import com.fizzycoyote.qusetroll.feature_character.utils.ClassStartingGold;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ClassStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private Spinner classSpinner;
    private List<Object> combinedClasses = new ArrayList<>();
    private Object currentClassObj;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        classSpinner = view.findViewById(R.id.class_spinner);
        Button nextButton = view.findViewById(R.id.next_button);
        Button backButton = view.findViewById(R.id.back_button);

        loadClasses();

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < combinedClasses.size()) {
                    currentClassObj = combinedClasses.get(position);
                    if (currentClassObj instanceof CharacterClassEntity) {
                        handleOpen5eClass((CharacterClassEntity) currentClassObj);
                    } else if (currentClassObj instanceof CustomCharacterClassEntity) {
                        handleCustomClass((CustomCharacterClassEntity) currentClassObj);
                    }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        nextButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.next_action));
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadClasses() {
        new Thread(() -> {
            if (!isAdded()) return;
            List<CharacterClassEntity> open5eClasses = Open5eDatabase.getInstance(requireContext())
                    .characterClassDao().getBaseClassesByGameSystem(viewModel.gameSystem);
            List<CustomCharacterClassEntity> customClasses = UserContentDatabase.getInstance(requireContext())
                    .customCharacterClassDao().getBaseClassesSync(viewModel.gameSystem);
            combinedClasses.clear();
            combinedClasses.addAll(open5eClasses);
            combinedClasses.addAll(customClasses);
            combinedClasses.sort((a, b) -> getName(a).compareTo(getName(b)));

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(requireContext(),
                        android.R.layout.simple_spinner_item, combinedClasses) {
                    @NonNull
                    @Override
                    public View getView(int pos, View cv, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getView(pos, cv, parent);
                        tv.setText(getName(getItem(pos)));
                        return tv;
                    }
                    @Override
                    public View getDropDownView(int pos, View cv, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getDropDownView(pos, cv, parent);
                        tv.setText(getName(getItem(pos)));
                        return tv;
                    }
                };
                classSpinner.setAdapter(adapter);
                if (!combinedClasses.isEmpty()) classSpinner.setSelection(0);
            });
        }).start();
    }

    private String getName(Object obj) {
        if (obj instanceof CharacterClassEntity) return ((CharacterClassEntity) obj).name;
        if (obj instanceof CustomCharacterClassEntity) return ((CustomCharacterClassEntity) obj).name;
        return "";
    }

    private String getKey(Object obj) {
        if (obj instanceof CharacterClassEntity) return ((CharacterClassEntity) obj).key;
        if (obj instanceof CustomCharacterClassEntity) return "custom_" + ((CustomCharacterClassEntity) obj).id;
        return "";
    }

    private void handleOpen5eClass(CharacterClassEntity selected) {
        viewModel.classFixedItems.clear();
        viewModel.classAssignments.clear();
        viewModel.classAssignments.add(new WizardViewModel.ClassAssignment(selected.key, selected.name, 1));
        loadClassEquipment(selected.key);
        loadClassSkillProficiencies(selected.key);
        ClassStartingGold goldInfo = ClassStartingGold.fromClassName(selected.name);
        viewModel.classGoldDice = goldInfo.getDiceCount() + "d" + goldInfo.getDiceSides();
        viewModel.classStartingGold = goldInfo.rollGold();
        loadClassTraits(selected.key);
        parseSpellcastingInfo(selected.key, selected.name);
        viewModel.classHitDice = selected.hitDice;
        viewModel.classCasterType = "";
        viewModel.classSpellcastingAbility = viewModel.spellcastingAbility;
        viewModel.classFixedLanguages.clear();
        viewModel.classLanguageChoices = 0;
    }

    private void handleCustomClass(CustomCharacterClassEntity selected) {
        viewModel.classAssignments.clear();
        viewModel.classAssignments.add(new WizardViewModel.ClassAssignment("custom_" + selected.id, selected.name, 1));
        viewModel.classHitDice = selected.hitDice;
        viewModel.classCasterType = selected.casterType;
        viewModel.classSpellcastingAbility = selected.spellcastingAbility;
        viewModel.classSkillChoices = selected.skillChoicesCount;

        if (selected.skillOptionsJson != null && !selected.skillOptionsJson.isEmpty()) {
            try {
                Type listType = new TypeToken<List<String>>(){}.getType();
                viewModel.classSkillOptions = new Gson().fromJson(selected.skillOptionsJson, listType);
            } catch (Exception e) {
                viewModel.classSkillOptions = new ArrayList<>();
            }
        } else {
            viewModel.classSkillOptions = new ArrayList<>();
        }

        viewModel.classEquipmentDescription = selected.equipmentDescription;
        viewModel.classStartingGold = 0;
        viewModel.classGoldDice = "0d0";
        viewModel.classEquipment.clear();

        if (selected.languageKeysJson != null && !selected.languageKeysJson.isEmpty()) {
            try {
                Type listType = new TypeToken<List<String>>(){}.getType();
                viewModel.classFixedLanguages = new Gson().fromJson(selected.languageKeysJson, listType);
            } catch (Exception e) {
                viewModel.classFixedLanguages = new ArrayList<>();
            }
        } else {
            viewModel.classFixedLanguages = new ArrayList<>();
        }
        viewModel.classLanguageChoices = selected.languageChoices;

        loadCustomClassFeatures(selected.id);

        if (selected.startingItemsJson != null && !selected.startingItemsJson.isEmpty()) {
            try {
                Type itemType = new TypeToken<List<String>>() {}.getType();
                List<String> items = new Gson().fromJson(selected.startingItemsJson, itemType);
                viewModel.classFixedItems.clear();
                viewModel.classFixedItems.addAll(items);
            } catch (Exception e) {
                viewModel.classFixedItems.clear();
            }
        } else {
            viewModel.classFixedItems.clear();
        }

        if (!"NONE".equals(selected.casterType)) {
            viewModel.cantripsCount = 2;
            viewModel.spellsKnownCount = 2;
            viewModel.isPreparedCaster = false;
            viewModel.spellcastingAbility = selected.spellcastingAbility;
        } else {
            viewModel.cantripsCount = 0;
            viewModel.spellsKnownCount = 0;
            viewModel.isPreparedCaster = false;
            viewModel.spellcastingAbility = "NONE";
        }
    }

    private void loadCustomClassFeatures(long classId) {
        new Thread(() -> {
            if (!isAdded()) return;
            CustomCharacterClassWithFeatures data = UserContentDatabase.getInstance(requireContext())
                    .customCharacterClassDao().getClassWithFeaturesSync(classId);
            if (data == null || data.features == null) return;

            List<CharacterTraitEntity> traits = new ArrayList<>();
            int order = 0;
            for (CustomFeatureEntity f : data.features) {
                if ("STARTING_EQUIPMENT".equals(f.type) || "PROFICIENCIES".equals(f.type)) continue;
                boolean available = false;
                if (f.customGainedAt != null && !f.customGainedAt.isEmpty()) {
                    for (CustomGainedAt gained : f.customGainedAt) {
                        if (gained.level <= 1) { available = true; break; }
                    }
                } else {
                    available = true;
                }
                if (!available) continue;
                CharacterTraitEntity t = new CharacterTraitEntity();
                t.sourceType = "CLASS";
                t.sourceKey = "custom_" + classId;
                t.name = f.name;
                t.description = f.description;
                t.levelRequirement = 1;
                t.displayOrder = order++;
                traits.add(t);
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                viewModel.characterTraits.addAll(traits);
            });
        }).start();
    }

    private void loadClassTraits(String classKey) {
        new Thread(() -> {
            if (!isAdded()) return;
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao().getFeaturesForClassSync(classKey);
            List<CharacterTraitEntity> classTraits = new ArrayList<>();
            int order = 0;
            for (FeatureEntity f : features) {
                if (f.featureType == null) continue;
                if (f.featureType.equals("STARTING_EQUIPMENT") || f.featureType.equals("PROFICIENCIES")) continue;
                boolean available = false;
                if (f.gainedAt != null) {
                    for (var gained : f.gainedAt) {
                        if (gained.level <= 1) { available = true; break; }
                    }
                } else {
                    available = true;
                }
                if (!available) continue;
                CharacterTraitEntity t = new CharacterTraitEntity();
                t.sourceType = "CLASS";
                t.sourceKey = classKey;
                t.name = f.name;
                t.description = f.desc;
                t.levelRequirement = 1;
                t.displayOrder = order++;
                classTraits.add(t);
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                viewModel.characterTraits.addAll(classTraits);
            });
        }).start();
    }

    private void parseSpellcastingInfo(String classKey, String className) {
        new Thread(() -> {
            if (!isAdded()) return;
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao().getFeaturesForClassSync(classKey);
            FeatureEntity spellFeature = null;
            for (FeatureEntity f : features) {
                if (f.name != null && f.name.equals("Spellcasting")) {
                    spellFeature = f;
                    break;
                }
            }
            if (spellFeature == null || spellFeature.desc == null) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    viewModel.cantripsCount = 0;
                    viewModel.spellsKnownCount = 0;
                    viewModel.preparedCount = 0;
                    viewModel.isPreparedCaster = false;
                });
                return;
            }
            String desc = spellFeature.desc;
            int cantrips = 0;
            int known = 0;
            boolean prepared = false;

            java.util.regex.Pattern pCantrip = java.util.regex.Pattern.compile("(\\d+) cantrips?");
            java.util.regex.Matcher mCantrip = pCantrip.matcher(desc);
            if (mCantrip.find()) cantrips = Integer.parseInt(mCantrip.group(1));

            if (desc.contains("prepare") || desc.contains("prepared")) {
                prepared = true;
            } else {
                java.util.regex.Pattern pKnown = java.util.regex.Pattern.compile("(\\d+) (?:spells?|spells? known)");
                java.util.regex.Matcher mKnown = pKnown.matcher(desc);
                if (mKnown.find()) known = Integer.parseInt(mKnown.group(1));
            }

            String castingAbility = ClassCastingAbility.getCastingAbilityForClass(className);
            if (castingAbility == null) castingAbility = "INT";

            final int finalCantrips = cantrips;
            final int finalKnown = known;
            final boolean finalPrepared = prepared;
            final String finalCastingAbility = castingAbility;

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                viewModel.cantripsCount = finalCantrips;
                viewModel.spellsKnownCount = finalKnown;
                viewModel.isPreparedCaster = finalPrepared;
                viewModel.spellcastingAbility = finalCastingAbility;
            });
        }).start();
    }

    private void parseClassSecretLanguages(List<FeatureEntity> features) {
        List<String> secretLangs = new ArrayList<>();
        for (FeatureEntity f : features) {
            String name = f.name != null ? f.name.toLowerCase() : "";
            String desc = f.desc != null ? f.desc.toLowerCase() : "";
            if (name.contains("druidic") || desc.contains("druidic")) secretLangs.add("Druidic");
            if (name.contains("thieves' cant") || desc.contains("thieves' cant")) secretLangs.add("Thieves' Cant");
        }
        viewModel.classSecretLanguages = secretLangs;
    }

    private void loadClassSkillProficiencies(String classKey) {
        new Thread(() -> {
            if (!isAdded()) return;
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao().getFeaturesForClassSync(classKey);
            FeatureEntity profFeature = null;
            for (FeatureEntity f : features) {
                if ("PROFICIENCIES".equals(f.featureType) || (f.name != null && f.name.equals("Proficiencies"))) {
                    profFeature = f;
                    break;
                }
            }
            if (profFeature != null && profFeature.desc != null) {
                String desc = profFeature.desc;
                String skillsPart = "";
                if (desc.contains("**Skills:**")) {
                    skillsPart = desc.split("\\*\\*Skills:\\*\\*")[1].split("\n")[0];
                } else if (desc.contains("Skills:")) {
                    skillsPart = desc.split("Skills:")[1].split("\n")[0];
                }
                int choices = 0;
                if (skillsPart.toLowerCase().contains("two")) choices = 2;
                else if (skillsPart.toLowerCase().contains("one")) choices = 1;
                else if (skillsPart.toLowerCase().contains("three")) choices = 3;
                String[] options = skillsPart.split(",");
                List<String> skillOptions = new ArrayList<>();
                for (String opt : options) {
                    String trimmed = opt.trim();
                    if (trimmed.startsWith("and ")) trimmed = trimmed.substring(4);
                    if (!trimmed.isEmpty()) skillOptions.add(trimmed);
                }
                viewModel.classSkillChoices = choices;
                viewModel.classSkillOptions = skillOptions;
            } else {
                viewModel.classSkillChoices = 0;
                viewModel.classSkillOptions.clear();
            }
        }).start();
    }

    private void loadClassEquipment(String classKey) {
        new Thread(() -> {
            if (!isAdded()) return;
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao().getFeaturesForClassSync(classKey);
            FeatureEntity equipmentFeature = null;
            parseClassSecretLanguages(features);
            for (FeatureEntity f : features) {
                if ("STARTING_EQUIPMENT".equals(f.featureType) || "Equipment".equals(f.name)) {
                    equipmentFeature = f;
                    break;
                }
            }
            if (equipmentFeature != null && equipmentFeature.desc != null) {
                viewModel.classEquipmentDescription = equipmentFeature.desc;
            } else {
                viewModel.classEquipmentDescription = "";
            }
        }).start();
    }
}