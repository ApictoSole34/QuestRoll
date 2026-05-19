package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesEntity;
import com.fizzycoyote.qusetroll.feature_character.utils.BonusParser;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;
import com.fizzycoyote.qusetroll.feature_species.viewmodel.CustomSpeciesCreateViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class RaceStepFragment extends Fragment {

    private Spinner raceSpinner, subspeciesSpinner;
    private TextView descriptionText;
    private LinearLayout subspeciesContainer;
    private WizardViewModel viewModel;
    private List<Object> combinedBaseRaces = new ArrayList<>();
    private List<Object> combinedSubraces = new ArrayList<>();
    private Object currentRaceObj;
    private Object currentSubspeciesObj;
    private Map<String, String> languageNameToKey = new HashMap<>();

    private static class AbilityBonus {
        String ability;
        int bonus;
        AbilityBonus(String ability, int bonus) { this.ability = ability; this.bonus = bonus; }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_race, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        raceSpinner = view.findViewById(R.id.race_spinner);
        subspeciesSpinner = view.findViewById(R.id.subspecies_spinner);
        descriptionText = view.findViewById(R.id.race_description);
        subspeciesContainer = view.findViewById(R.id.subspecies_container);

        Button nextButton = view.findViewById(R.id.next_button);
        Button backButton = view.findViewById(R.id.back_button);

        loadBaseRaces();

        raceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < combinedBaseRaces.size()) {
                    currentRaceObj = combinedBaseRaces.get(position);
                    loadSubspecies(currentRaceObj);
                    showDescription(currentRaceObj);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        subspeciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (combinedSubraces != null && !combinedSubraces.isEmpty() && position >= 0 && position < combinedSubraces.size()) {
                    currentSubspeciesObj = combinedSubraces.get(position);
                    viewModel.speciesKey = getKey(currentSubspeciesObj);
                    updateRacialFeatures(currentRaceObj, currentSubspeciesObj);
                    showDescription(currentSubspeciesObj);
                } else if (currentRaceObj != null && (combinedSubraces == null || combinedSubraces.isEmpty())) {
                    viewModel.speciesKey = getKey(currentRaceObj);
                    updateRacialFeatures(currentRaceObj, null);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        viewModel.executor.execute(() -> {
            List<LanguageEntity> allLangs = Open5eDatabase.getInstance(requireContext()).languageDao().getAllSync();
            for (LanguageEntity lang : allLangs) {
                languageNameToKey.put(lang.name.toLowerCase(), lang.key);
            }
        });

        nextButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.next_action));
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadBaseRaces() {
        viewModel.executor.execute(() -> {
            try {
                List<SpeciesEntity> open5eBase = Open5eDatabase.getInstance(requireContext())
                        .speciesDao().getBaseSpeciesByGameSystem(viewModel.gameSystem);
                List<CustomSpeciesEntity> customBase = UserContentDatabase.getInstance(requireContext())
                        .customSpeciesDao().getBaseSpeciesSync(viewModel.gameSystem);
                combinedBaseRaces.clear();
                combinedBaseRaces.addAll(open5eBase);
                combinedBaseRaces.addAll(customBase);
                combinedBaseRaces.sort((a,b) -> getName(a).compareTo(getName(b)));
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(requireContext(),
                            android.R.layout.simple_spinner_item, combinedBaseRaces) {
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
                    raceSpinner.setAdapter(adapter);
                    if (!combinedBaseRaces.isEmpty()) raceSpinner.setSelection(0);
                });
            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error loading races: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadSubspecies(Object parent) {
        String parentKey = getKey(parent);
        viewModel.executor.execute(() -> {
            try {
                List<SpeciesEntity> open5eSubs = Open5eDatabase.getInstance(requireContext())
                        .speciesDao().getSubspeciesByParent(parentKey, viewModel.gameSystem);
                List<CustomSpeciesEntity> customSubs = UserContentDatabase.getInstance(requireContext())
                        .customSpeciesDao().getSubspeciesByParentKey(parentKey, viewModel.gameSystem);
                combinedSubraces.clear();
                combinedSubraces.addAll(open5eSubs);
                combinedSubraces.addAll(customSubs);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!combinedSubraces.isEmpty()) {
                        subspeciesContainer.setVisibility(View.VISIBLE);
                        ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(requireContext(),
                                android.R.layout.simple_spinner_item, combinedSubraces) {
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
                        subspeciesSpinner.setAdapter(adapter);
                        if (!combinedSubraces.isEmpty()) subspeciesSpinner.setSelection(0);
                    } else {
                        subspeciesContainer.setVisibility(View.GONE);
                        updateRacialFeatures(currentRaceObj, null);
                    }
                });
            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error loading subraces: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateRacialFeatures(Object base, Object sub) {
        viewModel.executor.execute(() -> {
            ParsedRaceData total = new ParsedRaceData();
            if (base != null) total.merge(parseRaceObject(base));
            if (sub != null) total.merge(parseRaceObject(sub));

            List<Integer> bonusList = new ArrayList<>();
            for (int b : total.abilityBonuses) bonusList.add(b);
            viewModel.racialBonuses = bonusList;

            viewModel.raceSpeed = total.speed;
            viewModel.raceSize = total.size;
            viewModel.racialFixedLanguages = total.fixedLanguages;
            viewModel.racialLanguageChoices = total.languageChoices;
            viewModel.characterTraits.addAll(total.otherTraits);
            viewModel.recalcFinalAttributes();
        });
    }

    private ParsedRaceData parseRaceObject(Object obj) {
        ParsedRaceData data = new ParsedRaceData();
        if (obj instanceof SpeciesEntity) {
            SpeciesEntity s = (SpeciesEntity) obj;
            int[] bonuses = BonusParser.parseAbilityBonusesFromTraits(s.traitsJson);
            for (int i=0;i<6;i++) data.abilityBonuses[i] += bonuses[i];
            ParsedRaceLanguages langs = parseLanguagesFromSpecies(s);
            data.fixedLanguages.addAll(langs.fixed);
            data.languageChoices += langs.choices;
            data.otherTraits.addAll(parseTraitsFromSpecies(s, "RACE", s.key));
        } else if (obj instanceof CustomSpeciesEntity) {
            CustomSpeciesEntity cs = (CustomSpeciesEntity) obj;
            Gson gson = new Gson();
            Type bonusType = new TypeToken<List<AbilityBonus>>(){}.getType();
            List<AbilityBonus> bonuses = gson.fromJson(cs.abilityBonusesJson, bonusType);
            for (AbilityBonus ab : bonuses) {
                int idx = getAbilityIndex(ab.ability);
                data.abilityBonuses[idx] += ab.bonus;
            }
            data.speed = cs.speed;
            data.size = cs.size;
            Type langType = new TypeToken<List<String>>(){}.getType();
            List<String> langKeys = gson.fromJson(cs.languageKeysJson, langType);
            data.fixedLanguages.addAll(langKeys);
            data.languageChoices += cs.languageChoices;
            Type traitType = new TypeToken<List<CustomCreatureAction>>(){}.getType();
            List<CustomCreatureAction> traits = gson.fromJson(cs.otherTraitsJson, traitType);
            int order = 0;
            for (CustomCreatureAction t : traits) {
                CharacterTraitEntity trait = new CharacterTraitEntity();
                trait.sourceType = "RACE";
                trait.sourceKey = getKey(obj);
                trait.name = t.name;
                trait.description = t.desc;
                trait.levelRequirement = 1;
                trait.displayOrder = order++;
                data.otherTraits.add(trait);
            }
        }
        return data;
    }

    private int getAbilityIndex(String ability) {
        switch (ability.toUpperCase()) {
            case "STR": return 0;
            case "DEX": return 1;
            case "CON": return 2;
            case "INT": return 3;
            case "WIS": return 4;
            case "CHA": return 5;
            default: return 0;
        }
    }

    private void showDescription(Object obj) {
        if (obj instanceof SpeciesEntity) {
            String desc = ((SpeciesEntity) obj).desc;
            descriptionText.setText(desc != null ? desc : "No description");
        } else if (obj instanceof CustomSpeciesEntity) {
            String desc = ((CustomSpeciesEntity) obj).desc;
            descriptionText.setText(desc != null ? desc : "No description");
        } else {
            descriptionText.setText("");
        }
    }

    private String getName(Object obj) {
        if (obj instanceof SpeciesEntity) return ((SpeciesEntity) obj).name;
        if (obj instanceof CustomSpeciesEntity) return ((CustomSpeciesEntity) obj).name;
        return "";
    }

    private String getKey(Object obj) {
        if (obj instanceof SpeciesEntity) return ((SpeciesEntity) obj).key;
        if (obj instanceof CustomSpeciesEntity) return "custom_" + ((CustomSpeciesEntity) obj).id;
        return "";
    }

    private static class ParsedRaceLanguages {
        List<String> fixed = new ArrayList<>();
        int choices = 0;
    }

    private ParsedRaceLanguages parseLanguagesFromSpecies(SpeciesEntity species) {
        ParsedRaceLanguages result = new ParsedRaceLanguages();
        if (species == null || species.traitsJson == null) return result;
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>(){}.getType();
            List<Map<String, String>> traits = gson.fromJson(species.traitsJson, listType);
            for (Map<String, String> trait : traits) {
                if ("Languages".equals(trait.get("name"))) {
                    String desc = trait.get("desc");
                    if (desc == null) continue;
                    String lowerDesc = desc.toLowerCase();
                    if (lowerDesc.contains("one other language") || lowerDesc.contains("one additional language") ||
                            lowerDesc.contains("you can speak one") || lowerDesc.contains("choose one language")) {
                        result.choices = 1;
                    } else if (lowerDesc.contains("two other languages") || lowerDesc.contains("two additional languages")) {
                        result.choices = 2;
                    } else {
                        String[] parts = desc.split("[ ,]+");
                        for (String part : parts) {
                            part = part.replace(".", "").trim();
                            if (part.isEmpty()) continue;
                            if (Character.isUpperCase(part.charAt(0))) {
                                String lowerPart = part.toLowerCase();
                                if (languageNameToKey.containsKey(lowerPart)) {
                                    result.fixed.add(part);
                                }
                            }
                        }
                        result.fixed = new ArrayList<>(new LinkedHashSet<>(result.fixed));
                    }
                    break;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    private List<CharacterTraitEntity> parseTraitsFromSpecies(SpeciesEntity species, String sourceType, String sourceKey) {
        List<CharacterTraitEntity> traits = new ArrayList<>();
        if (species == null || species.traitsJson == null) return traits;
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>(){}.getType();
            List<Map<String, String>> traitList = gson.fromJson(species.traitsJson, listType);
            int order = 0;
            for (Map<String, String> trait : traitList) {
                String name = trait.get("name");
                String desc = trait.get("desc");
                if (name == null || desc == null) continue;
                if (name.equals("Ability Score Increase") || name.equals("Languages")) continue;
                CharacterTraitEntity t = new CharacterTraitEntity();
                t.sourceType = sourceType;
                t.sourceKey = sourceKey;
                t.name = name;
                t.description = desc;
                t.levelRequirement = 1;
                t.displayOrder = order++;
                traits.add(t);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return traits;
    }

    private static class ParsedRaceData {
        int[] abilityBonuses = new int[6];
        String speed = "";
        String size = "";
        List<String> fixedLanguages = new ArrayList<>();
        int languageChoices = 0;
        List<CharacterTraitEntity> otherTraits = new ArrayList<>();
        void merge(ParsedRaceData other) {
            for (int i=0;i<6;i++) abilityBonuses[i] += other.abilityBonuses[i];
            if (!other.speed.isEmpty()) speed = other.speed;
            if (!other.size.isEmpty()) size = other.size;
            fixedLanguages.addAll(other.fixedLanguages);
            languageChoices += other.languageChoices;
            otherTraits.addAll(other.otherTraits);
        }
    }
}