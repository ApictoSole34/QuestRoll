package com.fizzycoyote.qusetroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassWizardViewModel;

public class ClassWizardBasicInfoFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private EditText etClassName, etDescription;
    private Spinner spinnerHitDice, spinnerCasterType, spinnerSpellcastingAbility;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_basic_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ClassWizardViewModel.class);

        etClassName = view.findViewById(R.id.et_class_name);
        etDescription = view.findViewById(R.id.et_description);
        spinnerHitDice = view.findViewById(R.id.spinner_hit_dice);
        spinnerCasterType = view.findViewById(R.id.spinner_caster_type);
        spinnerSpellcastingAbility = view.findViewById(R.id.spinner_spellcasting_ability);

        // Ustaw fonty
        etClassName.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        etDescription.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));

        // Hit Dice spinner
        String[] hitDiceOptions = {"d6", "d8", "d10", "d12"};
        ArrayAdapter<String> hitDiceAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, hitDiceOptions);
        hitDiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHitDice.setAdapter(hitDiceAdapter);

        // Caster Type spinner
        String[] casterTypes = {"NONE", "FULL", "HALF", "THIRD", "WARLOCK"};
        ArrayAdapter<String> casterAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, casterTypes);
        casterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCasterType.setAdapter(casterAdapter);

        // Spellcasting Ability spinner
        String[] abilities = {"NONE", "INT", "WIS", "CHA"};
        ArrayAdapter<String> abilityAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, abilities);
        abilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpellcastingAbility.setAdapter(abilityAdapter);

        // Wypełnij danymi z ViewModel
        if (viewModel.className != null) etClassName.setText(viewModel.className);
        if (viewModel.description != null) etDescription.setText(viewModel.description);

        // Ustaw spinnery
        setSpinnerSelection(spinnerHitDice, viewModel.hitDice);
        setSpinnerSelection(spinnerCasterType, viewModel.casterType);
        setSpinnerSelection(spinnerSpellcastingAbility, viewModel.spellcastingAbility);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    @Override
    public boolean validate() {
        String name = etClassName.getText().toString().trim();
        if (name.isEmpty()) {
            etClassName.setError("Class name is required");
            return false;
        }
        return true;
    }

    @Override
    public void saveData() {
        viewModel.className = etClassName.getText().toString().trim();
        viewModel.description = etDescription.getText().toString().trim();
        viewModel.hitDice = spinnerHitDice.getSelectedItem().toString();
        viewModel.casterType = spinnerCasterType.getSelectedItem().toString();
        viewModel.spellcastingAbility = spinnerSpellcastingAbility.getSelectedItem().toString();
    }
}