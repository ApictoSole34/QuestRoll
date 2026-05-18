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
import com.fizzycoyote.qusetroll.core.models.character.CharacterCreationDTO;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.feature_character.utils.BenefitParser;
import com.fizzycoyote.qusetroll.feature_character.utils.ClassCastingAbility;
import com.fizzycoyote.qusetroll.feature_character.utils.ClassStartingGold;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class ClassStepFragment extends Fragment {
    private WizardViewModel viewModel;
    private Spinner classSpinner;
    private List<CharacterClassEntity> classList;

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
                if (classList != null && position < classList.size()) {
                    CharacterClassEntity selected = classList.get(position);
                    // Always level 1 at creation
                    viewModel.classAssignments.clear();
                    viewModel.classAssignments.add(new WizardViewModel.ClassAssignment(selected.key, selected.name, 1));
                    loadClassEquipment(selected.key);
                    loadClassSkillProficiencies(selected.key);
                    ClassStartingGold goldInfo = ClassStartingGold.fromClassName(selected.name);
                    viewModel.classGoldDice = goldInfo.getDiceCount() + "d" + goldInfo.getDiceSides();
                    viewModel.classStartingGold = goldInfo.rollGold();

                    loadClassTraits(selected.key);
                    parseSpellcastingInfo(selected.key, selected.name);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        nextButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.next_action));
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadClasses() {
        new Thread(() -> {
            classList = Open5eDatabase.getInstance(requireContext())
                    .characterClassDao()
                    .getBaseClassesByGameSystem(viewModel.gameSystem);
            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<CharacterClassEntity> adapter = new ArrayAdapter<CharacterClassEntity>(requireContext(),
                        android.R.layout.simple_spinner_item, classList) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        CharacterClassEntity item = getItem(position);
                        view.setText(item != null ? item.name : "");
                        return view;
                    }
                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                        CharacterClassEntity item = getItem(position);
                        view.setText(item != null ? item.name : "");
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                classSpinner.setAdapter(adapter);
                if (classList != null && !classList.isEmpty()) {
                    classSpinner.setSelection(0);
                }
            });
        }).start();
    }

    private void loadClassTraits(String classKey) {
        new Thread(() -> {
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao()
                    .getFeaturesForClassSync(classKey);
            List<CharacterTraitEntity> classTraits = new ArrayList<>();
            int order = 0;
            for (FeatureEntity f : features) {
                if (f.featureType == null) continue;
                if (f.featureType.equals("STARTING_EQUIPMENT") || f.featureType.equals("PROFICIENCIES"))
                    continue;
                // Check if the feature is available at level 1
                boolean available = false;
                if (f.gainedAt != null) {
                    for (var gained : f.gainedAt) {
                        if (gained.level <= 1) {
                            available = true;
                            break;
                        }
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
            requireActivity().runOnUiThread(() -> {
                viewModel.characterTraits.addAll(classTraits);
            });
        }).start();
    }

    private void parseSpellcastingInfo(String classKey, String className) {
        new Thread(() -> {
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao()
                    .getFeaturesForClassSync(classKey);
            FeatureEntity spellFeature = null;
            for (FeatureEntity f : features) {
                if (f.name != null && f.name.equals("Spellcasting")) {
                    spellFeature = f;
                    break;
                }
            }
            if (spellFeature == null || spellFeature.desc == null) {
                // Class does not cast spells
                requireActivity().runOnUiThread(() -> {
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

            // Number of cantrips
            java.util.regex.Pattern pCantrip = java.util.regex.Pattern.compile("(\\d+) cantrips?");
            java.util.regex.Matcher mCantrip = pCantrip.matcher(desc);
            if (mCantrip.find()) cantrips = Integer.parseInt(mCantrip.group(1));

            // Check if the class prepares spells
            if (desc.contains("prepare") || desc.contains("prepared")) {
                prepared = true;
                // Number of prepared spells = modifier + class level (level 1) – will be calculated later
            } else {
                // Number of known spells (for fixed-known classes)
                java.util.regex.Pattern pKnown = java.util.regex.Pattern.compile("(\\d+) (?:spells?|spells? known)");
                java.util.regex.Matcher mKnown = pKnown.matcher(desc);
                if (mKnown.find()) known = Integer.parseInt(mKnown.group(1));
            }

            // Spellcasting ability from enum
            String castingAbility = ClassCastingAbility.getCastingAbilityForClass(className);
            if (castingAbility == null) castingAbility = "INT";

            final int finalCantrips = cantrips;
            final int finalKnown = known;
            final boolean finalPrepared = prepared;
            final String finalCastingAbility = castingAbility;

            requireActivity().runOnUiThread(() -> {
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
            if (name.contains("druidic") || desc.contains("druidic")) {
                secretLangs.add("Druidic");
            }
            if (name.contains("thieves' cant") || desc.contains("thieves' cant")) {
                secretLangs.add("Thieves' Cant");
            }
        }
        viewModel.classSecretLanguages = secretLangs;
    }

    private void loadClassSkillProficiencies(String classKey) {
        new Thread(() -> {
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao()
                    .getFeaturesForClassSync(classKey);
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
            List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                    .featureDao()
                    .getFeaturesForClassSync(classKey);
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