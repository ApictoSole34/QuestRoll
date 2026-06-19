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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class SubclassChoiceStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private Spinner subclassSpinner;
    private TextView descriptionText;
    private List<Object> availableSubclasses = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_subclass, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        subclassSpinner  = view.findViewById(R.id.subclass_spinner);
        descriptionText  = view.findViewById(R.id.subclass_description);
        Button nextButton = view.findViewById(R.id.next_button);
        Button backButton = view.findViewById(R.id.back_button);

        loadSubclasses();

        subclassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < availableSubclasses.size()) {
                    Object selected = availableSubclasses.get(position);
                    viewModel.chosenSubclassKey = getKey(selected);
                    showDescription(selected);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        nextButton.setOnClickListener(v -> {
            if (viewModel.chosenSubclassKey == null) {
                Toast.makeText(getContext(), "Please select a subclass", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });

        descriptionText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
        descriptionText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadSubclasses() {
        new Thread(() -> {
            if (!isAdded()) return;

            if (viewModel.classAssignments.isEmpty()) return;

            String classKey   = viewModel.classAssignments.get(0).classKey;
            String gameSystem = viewModel.gameSystem;

            List<CharacterClassEntity> standardSubs = new ArrayList<>();
            List<CustomCharacterClassEntity> customSubs = new ArrayList<>();

            if (classKey.startsWith("custom_")) {
                // Custom class — look for custom subclasses
                try {
                    long id = Long.parseLong(classKey.substring(7));
                    CustomCharacterClassEntity parent = UserContentDatabase.getInstance(requireContext())
                            .customCharacterClassDao().getClassByIdSync(id);
                    if (parent != null) {
                        String parentKey = "custom_" + parent.id;
                        customSubs = UserContentDatabase.getInstance(requireContext())
                                .customCharacterClassDao()
                                .getSubclassesByParentKeySync(parentKey, gameSystem);
                    }
                } catch (NumberFormatException e) {
                    // Invalid custom key — leave lists empty
                }
            } else {
                // Open5e class — look for standard subclasses
                standardSubs = Open5eDatabase.getInstance(requireContext())
                        .characterClassDao().getSubclassesByParentKeySync(classKey);
            }

            availableSubclasses.clear();
            availableSubclasses.addAll(standardSubs);
            availableSubclasses.addAll(customSubs);
            availableSubclasses.sort((a, b) -> getName(a).compareTo(getName(b)));

            // FIX: isAdded() guard before runOnUiThread
            if (!isAdded()) return;

            final List<Object> finalList = new ArrayList<>(availableSubclasses);

            requireActivity().runOnUiThread(() -> {
                // FIX: isAdded() guard inside runOnUiThread
                if (!isAdded()) return;

                if (finalList.isEmpty()) {
                    // No subclasses in DB for this class — skip this step automatically
                    Toast.makeText(getContext(),
                            "No subclasses available for this class — skipping",
                            Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.next_action);
                    return;
                }

                ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        finalList) {
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
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subclassSpinner.setAdapter(adapter);

                // Pre-select first item or restore previous choice
                if (viewModel.chosenSubclassKey != null) {
                    for (int i = 0; i < finalList.size(); i++) {
                        if (viewModel.chosenSubclassKey.equals(getKey(finalList.get(i)))) {
                            subclassSpinner.setSelection(i);
                            showDescription(finalList.get(i));
                            return;
                        }
                    }
                }
                // Default: select first
                subclassSpinner.setSelection(0);
                viewModel.chosenSubclassKey = getKey(finalList.get(0));
                showDescription(finalList.get(0));
            });
        }).start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getName(Object obj) {
        if (obj instanceof CharacterClassEntity)       return ((CharacterClassEntity) obj).name;
        if (obj instanceof CustomCharacterClassEntity) return ((CustomCharacterClassEntity) obj).name;
        return "";
    }

    private String getKey(Object obj) {
        if (obj instanceof CharacterClassEntity)       return ((CharacterClassEntity) obj).key;
        if (obj instanceof CustomCharacterClassEntity) return "custom_" + ((CustomCharacterClassEntity) obj).id;
        return "";
    }

    private void showDescription(Object obj) {
        String desc = "";
        if (obj instanceof CharacterClassEntity) {
            CharacterClassEntity cls = (CharacterClassEntity) obj;
            // CharacterClassEntity nie ma pola "desc" — wyświetlamy nazwę i typ
            desc = cls.name;
            if (cls.casterType != null && !cls.casterType.isEmpty())
                desc += "\nSpellcasting: " + cls.casterType;
        } else if (obj instanceof CustomCharacterClassEntity) {
            CustomCharacterClassEntity custom = (CustomCharacterClassEntity) obj;
            desc = custom.description != null ? custom.description : custom.name;
        }
        if (descriptionText != null) {
            descriptionText.setText(!desc.isEmpty() ? desc : "No description available.");
        }
    }
}