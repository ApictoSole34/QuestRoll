package com.murkfeatherstudio.questroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardBasicInfoBinding;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.HashMap;
import java.util.Map;

public class ClassWizardBasicInfoFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private FragmentWizardBasicInfoBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWizardBasicInfoBinding.inflate(inflater, container, false);
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
        viewModel = new ViewModelProvider(requireActivity()).get(ClassWizardViewModel.class);

        setupPrereqRow(binding.cbReqStr, binding.etReqStrVal, "STR");
        setupPrereqRow(binding.cbReqDex, binding.etReqDexVal, "DEX");
        setupPrereqRow(binding.cbReqCon, binding.etReqConVal, "CON");
        setupPrereqRow(binding.cbReqInt, binding.etReqIntVal, "INT");
        setupPrereqRow(binding.cbReqWis, binding.etReqWisVal, "WIS");
        setupPrereqRow(binding.cbReqCha, binding.etReqChaVal, "CHA");

        // Set fonts
        binding.etClassName.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.etDescription.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));

        // Hit Dice spinner
        String[] hitDiceOptions = {"d6", "d8", "d10", "d12"};
        ArrayAdapter<String> hitDiceAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, hitDiceOptions);
        hitDiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerHitDice.setAdapter(hitDiceAdapter);

        // Caster Type spinner
        String[] casterTypes = {"NONE", "FULL", "HALF", "THIRD", "WARLOCK"};
        ArrayAdapter<String> casterAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, casterTypes);
        casterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCasterType.setAdapter(casterAdapter);

        // Spellcasting Ability spinner
        String[] abilities = {"NONE", "INT", "WIS", "CHA"};
        ArrayAdapter<String> abilityAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, abilities);
        abilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSpellcastingAbility.setAdapter(abilityAdapter);

        // Fill with data from ViewModel
        if (viewModel.className != null) binding.etClassName.setText(viewModel.className);
        if (viewModel.description != null) binding.etDescription.setText(viewModel.description);

        setSpinnerSelection(binding.spinnerHitDice, viewModel.hitDice);
        setSpinnerSelection(binding.spinnerCasterType, viewModel.casterType);
        setSpinnerSelection(binding.spinnerSpellcastingAbility, viewModel.spellcastingAbility);

        // Load prereqs
        loadPrereq(binding.cbReqStr, binding.etReqStrVal, "STR");
        loadPrereq(binding.cbReqDex, binding.etReqDexVal, "DEX");
        loadPrereq(binding.cbReqCon, binding.etReqConVal, "CON");
        loadPrereq(binding.cbReqInt, binding.etReqIntVal, "INT");
        loadPrereq(binding.cbReqWis, binding.etReqWisVal, "WIS");
        loadPrereq(binding.cbReqCha, binding.etReqChaVal, "CHA");
    }

    private void setupPrereqRow(CheckBox cb, EditText et, String key) {
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            et.setEnabled(isChecked);
            if (!isChecked) et.setText("13");
        });
    }

    private void loadPrereq(CheckBox cb, EditText et, String key) {
        if (viewModel.multiclassPrereqs.containsKey(key)) {
            cb.setChecked(true);
            et.setEnabled(true);
            et.setText(String.valueOf(viewModel.multiclassPrereqs.get(key)));
        } else {
            cb.setChecked(false);
            et.setEnabled(false);
            et.setText("13");
        }
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
        if (binding == null) return false;
        String name = binding.etClassName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.etClassName.setError("Class name is required");
            return false;
        }
        return true;
    }

    @Override
    public void saveData() {
        if (binding == null) return;
        viewModel.className = binding.etClassName.getText().toString().trim();
        viewModel.description = binding.etDescription.getText().toString().trim();
        viewModel.hitDice = binding.spinnerHitDice.getSelectedItem().toString();
        viewModel.casterType = binding.spinnerCasterType.getSelectedItem().toString();
        viewModel.spellcastingAbility = binding.spinnerSpellcastingAbility.getSelectedItem().toString();

        Map<String, Integer> prereqs = new HashMap<>();
        savePrereq(prereqs, binding.cbReqStr, binding.etReqStrVal, "STR");
        savePrereq(prereqs, binding.cbReqDex, binding.etReqDexVal, "DEX");
        savePrereq(prereqs, binding.cbReqCon, binding.etReqConVal, "CON");
        savePrereq(prereqs, binding.cbReqInt, binding.etReqIntVal, "INT");
        savePrereq(prereqs, binding.cbReqWis, binding.etReqWisVal, "WIS");
        savePrereq(prereqs, binding.cbReqCha, binding.etReqChaVal, "CHA");
        viewModel.multiclassPrereqs = prereqs;
    }

    private void savePrereq(Map<String, Integer> map, CheckBox cb, EditText et, String key) {
        if (cb.isChecked()) {
            try {
                map.put(key, Integer.parseInt(et.getText().toString().trim()));
            } catch (NumberFormatException e) {
                map.put(key, 13);
            }
        }
    }
}
