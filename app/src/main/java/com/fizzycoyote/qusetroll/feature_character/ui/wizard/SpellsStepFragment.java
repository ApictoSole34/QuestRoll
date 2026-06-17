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
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellEntity;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class SpellsStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private LinearLayout container;
    private Button nextButton, backButton;
    private List<CheckBox> cantripCheckboxes = new ArrayList<>();
    private List<CheckBox> spellCheckboxes = new ArrayList<>();
    private int maxCantrips = 0;
    private int maxSpells = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_spells, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        container = view.findViewById(R.id.spells_container);
        nextButton = view.findViewById(R.id.next_button);
        backButton = view.findViewById(R.id.back_button);

        maxCantrips = viewModel.cantripsCount;
        if (viewModel.isPreparedCaster) {
            maxSpells = viewModel.spellcastingAbilityMod + 1;
        } else {
            maxSpells = viewModel.spellsKnownCount;
        }

        if (maxCantrips == 0 && maxSpells == 0) {
            Navigation.findNavController(view).navigate(R.id.next_action);
            return;
        }

        loadSpells();

        nextButton.setOnClickListener(v -> {
            viewModel.chosenCantripKeys.clear();
            for (CheckBox cb : cantripCheckboxes) {
                if (cb.isChecked()) {
                    viewModel.chosenCantripKeys.add((String) cb.getTag());
                }
            }
            viewModel.chosenSpellKeys.clear();
            for (CheckBox cb : spellCheckboxes) {
                if (cb.isChecked()) {
                    viewModel.chosenSpellKeys.add((String) cb.getTag());
                }
            }
            if (viewModel.chosenCantripKeys.size() > maxCantrips) {
                Toast.makeText(getContext(), "You can select only " + maxCantrips + " cantrips", Toast.LENGTH_SHORT).show();
                return;
            }
            if (viewModel.chosenSpellKeys.size() > maxSpells) {
                Toast.makeText(getContext(), "You can select only " + maxSpells + " 1st-level spells", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void loadSpells() {
        new Thread(() -> {
            List<SpellEntity> allSpells = Open5eDatabase.getInstance(requireContext())
                    .spellDao()
                    .getAllSync();
            if (allSpells == null) allSpells = new ArrayList<>();

            if (viewModel.classAssignments.isEmpty()) return;
            String className = viewModel.classAssignments.get(0).className;

            List<SpellEntity> cantrips = new ArrayList<>();
            List<SpellEntity> firstLevelSpells = new ArrayList<>();

            for (SpellEntity spell : allSpells) {
                // Case-insensitive sprawdzenie nazwy klasy
                if (spell.classes != null && spell.classes.stream().anyMatch(c -> c.equalsIgnoreCase(className))) {
                    if (spell.level == 0) {
                        cantrips.add(spell);
                    } else if (spell.level == 1) {
                        firstLevelSpells.add(spell);
                    }
                }
            }

            requireActivity().runOnUiThread(() -> {
                container.removeAllViews();
                cantripCheckboxes.clear();
                spellCheckboxes.clear();

                // Jeśli klasa nie ma żadnych zaklęć, pokaż info i pozwól przejść dalej
                if (cantrips.isEmpty() && firstLevelSpells.isEmpty()) {
                    TextView info = new TextView(getContext());
                    info.setText("No spells available for this class (or data missing).");
                    info.setPadding(0, 16, 0, 0);
                    container.addView(info);
                    return;
                }

                if (maxCantrips > 0 && !cantrips.isEmpty()) {
                    TextView header = new TextView(getContext());
                    header.setText("Select cantrips (max " + maxCantrips + "):");
                    header.setPadding(0, 16, 0, 8);
                    header.setTypeface(null, android.graphics.Typeface.BOLD);
                    container.addView(header);

                    for (SpellEntity spell : cantrips) {
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(spell.name);
                        cb.setTag(spell.key);
                        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked && getCheckedCount(cantripCheckboxes) > maxCantrips) {
                                cb.setChecked(false);
                                Toast.makeText(getContext(), "You can select only " + maxCantrips + " cantrips", Toast.LENGTH_SHORT).show();
                            }
                        });
                        container.addView(cb);
                        cantripCheckboxes.add(cb);
                    }
                }

                if (maxSpells > 0 && !firstLevelSpells.isEmpty()) {
                    TextView header = new TextView(getContext());
                    header.setText("Select 1st-level spells (max " + maxSpells + "):");
                    header.setPadding(0, 24, 0, 8);
                    header.setTypeface(null, android.graphics.Typeface.BOLD);
                    container.addView(header);

                    for (SpellEntity spell : firstLevelSpells) {
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(spell.name);
                        cb.setTag(spell.key);
                        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked && getCheckedCount(spellCheckboxes) > maxSpells) {
                                cb.setChecked(false);
                                Toast.makeText(getContext(), "You can select only " + maxSpells + " spells", Toast.LENGTH_SHORT).show();
                            }
                        });
                        container.addView(cb);
                        spellCheckboxes.add(cb);
                    }
                }

                if (cantrips.isEmpty() && firstLevelSpells.isEmpty()) {
                    // Już obsłużone wyżej
                } else if (maxCantrips == 0 && maxSpells == 0) {
                    TextView info = new TextView(getContext());
                    info.setText("This class does not cast spells at 1st level.");
                    info.setPadding(0, 16, 0, 0);
                    container.addView(info);
                }
            });
        }).start();
    }

    private int getCheckedCount(List<CheckBox> boxes) {
        int count = 0;
        for (CheckBox cb : boxes) if (cb.isChecked()) count++;
        return count;
    }
}
