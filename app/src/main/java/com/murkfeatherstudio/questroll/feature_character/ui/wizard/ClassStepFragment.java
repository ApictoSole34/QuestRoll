package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.config.SubclassLevelConfig;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.saving_throw.SavingThrowEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.table_data.TableData;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardClassBinding;
import com.murkfeatherstudio.questroll.feature_character.utils.ClassCastingAbility;
import com.murkfeatherstudio.questroll.feature_character.utils.ClassStartingGold;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private FragmentWizardClassBinding binding;
    private List<Object> combinedClasses = new ArrayList<>();
    private Object currentClassObj;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardClassBinding.inflate(inflater, container, false);
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

        loadClasses();

        binding.classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        binding.nextButton.setOnClickListener(v -> {
            if (viewModel.classAssignments.isEmpty()) {
                Toast.makeText(getContext(), "Please select a class", Toast.LENGTH_SHORT).show();
                return;
            }

            String classKey = viewModel.classAssignments.get(0).classKey;

            if (SubclassLevelConfig.needsSubclassAtLevel(classKey, 1)) {
                Navigation.findNavController(v).navigate(R.id.next_action);
            } else {
                Navigation.findNavController(v).navigate(R.id.action_class_to_attributes);
            }
        });

        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadClasses() {
        AppExecutors.getInstance().diskIO().execute(() -> {
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
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;
                ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(requireContext(),
                        android.R.layout.simple_spinner_item, combinedClasses) {
                    @NonNull
                    @Override
                    public View getView(int pos, View cv, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getView(pos, cv, parent);
                        tv.setText(getName(getItem(pos)));
                        tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return tv;
                    }
                    @Override
                    public View getDropDownView(int pos, View cv, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getDropDownView(pos, cv, parent);
                        tv.setText(getName(getItem(pos)));
                        tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return tv;
                    }
                };
                binding.classSpinner.setAdapter(adapter);
                if (!combinedClasses.isEmpty()) binding.classSpinner.setSelection(0);
            });
        });
    }

    private void handleOpen5eClass(CharacterClassEntity selected) {
        viewModel.classFixedItems.clear();
        viewModel.classAssignments.clear();
        viewModel.classAssignments.add(new WizardViewModel.ClassAssignment(selected.key, selected.name, 1));
        viewModel.chosenSubclassKey = null;

        loadClassEquipment(selected.key);
        loadClassSkillProficiencies(selected.key);

        ClassStartingGold goldInfo = ClassStartingGold.fromClassName(selected.name);
        viewModel.classGoldDice    = goldInfo.getDiceCount() + "d" + goldInfo.getDiceSides();
        viewModel.classStartingGold = goldInfo.rollGold();

        loadClassTraits(selected.key);
        parseSpellcastingInfo(selected.key, selected.name);

        viewModel.classHitDice             = selected.hitDice;
        viewModel.classCasterType          = "";
        viewModel.classSpellcastingAbility = viewModel.spellcastingAbility;
        viewModel.classFixedLanguages.clear();
        viewModel.classLanguageChoices = 0;

        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            List<SavingThrowEntity> savingThrows = Open5eDatabase.getInstance(requireContext())
                    .savingThrowDao().getSavingThrowsForClassSync(selected.key);
            Set<String> abilities = new HashSet<>();
            if (savingThrows != null) {
                for (SavingThrowEntity st : savingThrows) {
                    if (st.abilityKey != null) abilities.add(st.abilityKey);
                }
            }
            if (!isAdded()) return;
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                viewModel.selectedSavingThrows = abilities;
            });
        });
    }

    private void handleCustomClass(CustomCharacterClassEntity selected) {
        viewModel.classAssignments.clear();
        viewModel.classAssignments.add(
                new WizardViewModel.ClassAssignment("custom_" + selected.id, selected.name, 1));
        viewModel.chosenSubclassKey = null;

        viewModel.classHitDice             = selected.hitDice;
        viewModel.classCasterType          = selected.casterType;
        viewModel.classSpellcastingAbility = selected.spellcastingAbility;
        viewModel.classSkillChoices        = selected.skillChoicesCount;

        if (selected.skillOptionsJson != null && !selected.skillOptionsJson.isEmpty()) {
            try {
                Type t = new TypeToken<List<String>>(){}.getType();
                viewModel.classSkillOptions = new Gson().fromJson(selected.skillOptionsJson, t);
            } catch (Exception e) {
                viewModel.classSkillOptions = new ArrayList<>();
            }
        } else {
            viewModel.classSkillOptions = new ArrayList<>();
        }

        viewModel.classEquipmentDescription = selected.equipmentDescription;
        viewModel.classStartingGold         = 0;
        viewModel.classGoldDice             = "0d0";
        viewModel.classEquipment.clear();

        if (selected.languageKeysJson != null && !selected.languageKeysJson.isEmpty()) {
            try {
                Type t = new TypeToken<List<String>>(){}.getType();
                viewModel.classFixedLanguages = new Gson().fromJson(selected.languageKeysJson, t);
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
                Type t = new TypeToken<List<String>>(){}.getType();
                List<String> items = new Gson().fromJson(selected.startingItemsJson, t);
                viewModel.classFixedItems.clear();
                viewModel.classFixedItems.addAll(items);
            } catch (Exception e) {
                viewModel.classFixedItems.clear();
            }
        } else {
            viewModel.classFixedItems.clear();
        }

        if (!"NONE".equals(selected.casterType)) {
            viewModel.cantripsCount    = 2;
            viewModel.spellsKnownCount = 2;
            viewModel.isPreparedCaster = false;
            viewModel.spellcastingAbility = selected.spellcastingAbility;
        } else {
            viewModel.cantripsCount    = 0;
            viewModel.spellsKnownCount = 0;
            viewModel.isPreparedCaster = false;
            viewModel.spellcastingAbility = "NONE";
        }

        viewModel.selectedSavingThrows = new HashSet<>(
                selected.savingThrows != null ? selected.savingThrows : new ArrayList<>());
    }

    private void loadCustomClassFeatures(long classId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
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
                t.sourceType       = "CLASS";
                t.sourceKey        = "custom_" + classId;
                t.name             = f.name;
                t.description      = f.description;
                t.levelRequirement = 1;
                t.displayOrder     = order++;
                traits.add(t);
            }

            if (!isAdded()) return;
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                viewModel.characterTraits.removeIf(t -> "CLASS".equals(t.sourceType));
                viewModel.characterTraits.addAll(traits);
            });
        });
    }

    private void loadClassTraits(String classKey) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao().getFeaturesForClassSync(classKey);

            List<CharacterTraitEntity> classTraits = new ArrayList<>();
            int order = 0;
            for (FeatureEntity f : features) {
                if (f.featureType == null) continue;
                if ("STARTING_EQUIPMENT".equals(f.featureType) || "PROFICIENCIES".equals(f.featureType)) continue;
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
                t.sourceType       = "CLASS";
                t.sourceKey        = classKey;
                t.name             = f.name;
                t.description      = f.desc;
                t.levelRequirement = 1;
                t.displayOrder     = order++;
                classTraits.add(t);
            }

            if (!isAdded()) return;
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                viewModel.characterTraits.removeIf(t -> "CLASS".equals(t.sourceType));
                viewModel.characterTraits.addAll(classTraits);
            });
        });
    }

    private void parseSpellcastingInfo(String classKey, String className) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao().getFeaturesForClassSync(classKey);

            int cantrips = 0;
            int known = 0;
            boolean prepared = false;

            for (FeatureEntity f : features) {
                if ("Cantrips Known".equals(f.name) && f.tableData != null) {
                    for (TableData td : f.tableData) {
                        if (td.level == 1) {
                            try {
                                cantrips = Integer.parseInt(td.columnValue);
                            } catch (NumberFormatException e) { cantrips = 0; }
                            break;
                        }
                    }
                    break;
                }
            }

            for (FeatureEntity f : features) {
                if ("Spells Known".equals(f.name) && f.tableData != null) {
                    for (TableData td : f.tableData) {
                        if (td.level == 1) {
                            try {
                                known = Integer.parseInt(td.columnValue);
                            } catch (NumberFormatException e) { known = 0; }
                            break;
                        }
                    }
                    break;
                }
            }

            if (known == 0 || cantrips == 0) {
                FeatureEntity spellFeature = null;
                for (FeatureEntity f : features) {
                    if (f.name != null && f.name.equals("Spellcasting")) {
                        spellFeature = f;
                        break;
                    }
                }
                if (spellFeature != null && spellFeature.desc != null) {
                    String desc = spellFeature.desc;
                    if (cantrips == 0) {
                        java.util.regex.Matcher m = java.util.regex.Pattern
                                .compile("(\\d+) cantrips?").matcher(desc);
                        if (m.find()) cantrips = Integer.parseInt(m.group(1));
                    }
                    if (known == 0) {
                        if (desc.contains("prepare") || desc.contains("prepared")) {
                            prepared = true;
                        } else {
                            java.util.regex.Matcher m = java.util.regex.Pattern
                                    .compile("(\\d+) (?:spells?|spells? known)").matcher(desc);
                            if (m.find()) known = Integer.parseInt(m.group(1));
                        }
                    }
                }
            }

            String castingAbility = ClassCastingAbility.getCastingAbilityForClass(className);
            if (castingAbility == null) castingAbility = "INT";

            final int finalCantrips = cantrips;
            final int finalKnown = known;
            final boolean finalPrepared = prepared;
            final String finalCastingAbility = castingAbility;

            if (!isAdded()) return;
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                viewModel.cantripsCount = finalCantrips;
                viewModel.spellsKnownCount = finalKnown;
                viewModel.isPreparedCaster = finalPrepared;
                viewModel.spellcastingAbility = finalCastingAbility;
            });
        });
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
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao().getFeaturesForClassSync(classKey);
            FeatureEntity profFeature = null;
            for (FeatureEntity f : features) {
                if ("PROFICIENCIES".equals(f.featureType)
                        || (f.name != null && f.name.equals("Proficiencies"))) {
                    profFeature = f;
                    break;
                }
            }
            if (profFeature != null && profFeature.desc != null) {
                String desc = profFeature.desc;
                String skillsPart = "";
                if (desc.contains("**Skills:**"))
                    skillsPart = desc.split("\\*\\*Skills:\\*\\*")[1].split("\n")[0];
                else if (desc.contains("Skills:"))
                    skillsPart = desc.split("Skills:")[1].split("\n")[0];

                int choices = 0;
                if      (skillsPart.toLowerCase().contains("two"))   choices = 2;
                else if (skillsPart.toLowerCase().contains("one"))   choices = 1;
                else if (skillsPart.toLowerCase().contains("three")) choices = 3;

                List<String> skillOptions = new ArrayList<>();
                for (String opt : skillsPart.split(",")) {
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
        });
    }

    private void loadClassEquipment(String classKey) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao().getFeaturesForClassSync(classKey);
            parseClassSecretLanguages(features);
            FeatureEntity equipmentFeature = null;
            for (FeatureEntity f : features) {
                if ("STARTING_EQUIPMENT".equals(f.featureType) || "Equipment".equals(f.name)) {
                    equipmentFeature = f;
                    break;
                }
            }
            viewModel.classEquipmentDescription =
                    (equipmentFeature != null && equipmentFeature.desc != null)
                            ? equipmentFeature.desc : "";
        });
    }

    private String getName(Object obj) {
        if (obj instanceof CharacterClassEntity)    return ((CharacterClassEntity) obj).name;
        if (obj instanceof CustomCharacterClassEntity) return ((CustomCharacterClassEntity) obj).name;
        return "";
    }
}
