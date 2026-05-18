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
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesEntity;
import com.fizzycoyote.qusetroll.feature_character.utils.BonusParser;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class RaceStepFragment extends Fragment {

    private Spinner raceSpinner, subspeciesSpinner;
    private TextView descriptionText;
    private LinearLayout subspeciesContainer;
    private WizardViewModel viewModel;
    private List<SpeciesEntity> baseRaceList;
    private List<SpeciesEntity> subspeciesList;
    private SpeciesEntity currentRace;
    private Map<String, String> languageNameToKey = new HashMap<>();

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
                if (baseRaceList != null && position < baseRaceList.size()) {
                    currentRace = baseRaceList.get(position);
                    loadSubspecies(currentRace.key);
                    showDescription(currentRace);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        subspeciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (subspeciesList != null && !subspeciesList.isEmpty()
                        && position < subspeciesList.size()) {
                    SpeciesEntity selected = subspeciesList.get(position);
                    viewModel.speciesKey = selected.key;
                    updateRacialFeatures(currentRace, selected);
                    showDescription(selected);
                } else if (currentRace != null
                        && (subspeciesList == null || subspeciesList.isEmpty())) {
                    viewModel.speciesKey = currentRace.key;
                    updateRacialFeatures(currentRace, null);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        viewModel.executor.execute(() -> {
            List<LanguageEntity> allLangs =
                    Open5eDatabase.getInstance(requireContext()).languageDao().getAllSync();
            for (LanguageEntity lang : allLangs) {
                languageNameToKey.put(lang.name.toLowerCase(), lang.key);
            }
        });

        nextButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.next_action));
        backButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadBaseRaces() {
        viewModel.executor.execute(() -> {
            try {
                baseRaceList = Open5eDatabase.getInstance(requireContext())
                        .speciesDao()
                        .getBaseSpeciesByGameSystem(viewModel.gameSystem);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    ArrayAdapter<SpeciesEntity> adapter =
                            new ArrayAdapter<SpeciesEntity>(requireContext(),
                                    android.R.layout.simple_spinner_item, baseRaceList) {
                                @NonNull @Override
                                public View getView(int position, View convertView,
                                                    @NonNull ViewGroup parent) {
                                    TextView view = (TextView) super.getView(
                                            position, convertView, parent);
                                    SpeciesEntity item = getItem(position);
                                    view.setText(item != null ? item.name : "");
                                    return view;
                                }
                                @Override
                                public View getDropDownView(int position, View convertView,
                                                            @NonNull ViewGroup parent) {
                                    TextView view = (TextView) super.getDropDownView(
                                            position, convertView, parent);
                                    SpeciesEntity item = getItem(position);
                                    view.setText(item != null ? item.name : "");
                                    return view;
                                }
                            };
                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    raceSpinner.setAdapter(adapter);
                    if (baseRaceList != null && !baseRaceList.isEmpty()) {
                        raceSpinner.setSelection(0);
                    }
                });
            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "Error loading races: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadSubspecies(String parentKey) {
        viewModel.executor.execute(() -> {
            try {
                subspeciesList = Open5eDatabase.getInstance(requireContext())
                        .speciesDao()
                        .getSubspeciesByParent(parentKey, viewModel.gameSystem);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (subspeciesList != null && !subspeciesList.isEmpty()) {
                        subspeciesContainer.setVisibility(View.VISIBLE);
                        ArrayAdapter<SpeciesEntity> adapter =
                                new ArrayAdapter<SpeciesEntity>(requireContext(),
                                        android.R.layout.simple_spinner_item, subspeciesList) {
                                    @NonNull @Override
                                    public View getView(int position, View convertView,
                                                        @NonNull ViewGroup parent) {
                                        TextView view = (TextView) super.getView(
                                                position, convertView, parent);
                                        SpeciesEntity item = getItem(position);
                                        view.setText(item != null ? item.name : "");
                                        return view;
                                    }
                                    @Override
                                    public View getDropDownView(int position, View convertView,
                                                                @NonNull ViewGroup parent) {
                                        TextView view = (TextView) super.getDropDownView(
                                                position, convertView, parent);
                                        SpeciesEntity item = getItem(position);
                                        view.setText(item != null ? item.name : "");
                                        return view;
                                    }
                                };
                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        subspeciesSpinner.setAdapter(adapter);
                        SpeciesEntity firstSub = subspeciesList.get(0);
                        viewModel.speciesKey = firstSub.key;
                        updateRacialFeatures(currentRace, firstSub);
                        showDescription(firstSub);
                        subspeciesSpinner.setSelection(0);
                    } else {
                        subspeciesContainer.setVisibility(View.GONE);
                        if (currentRace != null) {
                            viewModel.speciesKey = currentRace.key;
                            updateRacialFeatures(currentRace, null);
                        }
                    }
                });
            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "Error loading subraces: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateRacialFeatures(SpeciesEntity baseSpecies, SpeciesEntity subspecies) {
        viewModel.executor.execute(() -> {
            try {
                ParsedRaceLanguages baseLangs = parseLanguagesFromSpecies(baseSpecies);
                ParsedRaceLanguages subLangs = new ParsedRaceLanguages();
                if (subspecies != null) {
                    subLangs = parseLanguagesFromSpecies(subspecies);
                }
                List<String> allFixed = new ArrayList<>();
                allFixed.addAll(baseLangs.fixed);
                allFixed.addAll(subLangs.fixed);
                viewModel.racialFixedLanguages = allFixed;
                viewModel.racialLanguageChoices = baseLangs.choices + subLangs.choices;

                int[] bonuses = BonusParser.parseAbilityBonusesFromTraits(baseSpecies.traitsJson);
                if (subspecies != null) {
                    int[] subBonuses = BonusParser.parseAbilityBonusesFromTraits(
                            subspecies.traitsJson);
                    for (int i = 0; i < bonuses.length; i++) {
                        bonuses[i] += subBonuses[i];
                    }
                }
                List<Integer> bonusList = new ArrayList<>();
                for (int b : bonuses) bonusList.add(b);
                viewModel.racialBonuses = bonusList;

                viewModel.characterTraits.addAll(parseTraitsFromSpecies(baseSpecies, "RACE", baseSpecies.key));
                if (subspecies != null) {
                    viewModel.characterTraits.addAll(parseTraitsFromSpecies(subspecies, "RACE", subspecies.key));
                }

                viewModel.recalcFinalAttributes();

            } catch (Exception e) {
                viewModel.errorLiveData.postValue("Error parsing race features: " + e.getMessage());
            }
        });
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
                    if (lowerDesc.contains("one other language")
                            || lowerDesc.contains("one additional language")
                            || lowerDesc.contains("you can speak one")
                            || lowerDesc.contains("choose one language")) {
                        result.choices = 1;
                    } else if (lowerDesc.contains("two other languages")
                            || lowerDesc.contains("two additional languages")) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                // Skip already processed: Ability Score Increase and Languages
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return traits;
    }

    private void showDescription(SpeciesEntity species) {
        if (species != null) {
            descriptionText.setText(species.desc != null ? species.desc : "No description");
        } else {
            descriptionText.setText("");
        }
    }
}