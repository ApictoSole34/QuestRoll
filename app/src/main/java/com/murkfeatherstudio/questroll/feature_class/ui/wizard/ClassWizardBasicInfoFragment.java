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
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.HashMap;
import java.util.Map;

public class ClassWizardBasicInfoFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private EditText etClassName, etDescription;
    private Spinner spinnerHitDice, spinnerCasterType, spinnerSpellcastingAbility;

    // Prerequisites
    private CheckBox cbStr, cbDex, cbCon, cbInt, cbWis, cbCha;
    private EditText etStr, etDex, etCon, etInt, etWis, etCha;

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

        cbStr = view.findViewById(R.id.cb_req_str);
        cbDex = view.findViewById(R.id.cb_req_dex);
        cbCon = view.findViewById(R.id.cb_req_con);
        cbInt = view.findViewById(R.id.cb_req_int);
        cbWis = view.findViewById(R.id.cb_req_wis);
        cbCha = view.findViewById(R.id.cb_req_cha);

        etStr = view.findViewById(R.id.et_req_str_val);
        etDex = view.findViewById(R.id.et_req_dex_val);
        etCon = view.findViewById(R.id.et_req_con_val);
        etInt = view.findViewById(R.id.et_req_int_val);
        etWis = view.findViewById(R.id.et_req_wis_val);
        etCha = view.findViewById(R.id.et_req_cha_val);

        setupPrereqRow(cbStr, etStr, "STR");
        setupPrereqRow(cbDex, etDex, "DEX");
        setupPrereqRow(cbCon, etCon, "CON");
        setupPrereqRow(cbInt, etInt, "INT");
        setupPrereqRow(cbWis, etWis, "WIS");
        setupPrereqRow(cbCha, etCha, "CHA");

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

        setSpinnerSelection(spinnerHitDice, viewModel.hitDice);
        setSpinnerSelection(spinnerCasterType, viewModel.casterType);
        setSpinnerSelection(spinnerSpellcastingAbility, viewModel.spellcastingAbility);

        // Load prereqs
        loadPrereq(cbStr, etStr, "STR");
        loadPrereq(cbDex, etDex, "DEX");
        loadPrereq(cbCon, etCon, "CON");
        loadPrereq(cbInt, etInt, "INT");
        loadPrereq(cbWis, etWis, "WIS");
        loadPrereq(cbCha, etCha, "CHA");
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

        Map<String, Integer> prereqs = new HashMap<>();
        savePrereq(prereqs, cbStr, etStr, "STR");
        savePrereq(prereqs, cbDex, etDex, "DEX");
        savePrereq(prereqs, cbCon, etCon, "CON");
        savePrereq(prereqs, cbInt, etInt, "INT");
        savePrereq(prereqs, cbWis, etWis, "WIS");
        savePrereq(prereqs, cbCha, etCha, "CHA");
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