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
import com.murkfeatherstudio.questroll.databinding.FragmentWizardAttributesBinding;
import com.murkfeatherstudio.questroll.databinding.ItemAttributeEditorBinding;
import com.murkfeatherstudio.questroll.databinding.ItemStandardAttributeBinding;
import com.murkfeatherstudio.questroll.feature_character.engine.CharacterEngine;
import com.murkfeatherstudio.questroll.feature_character.utils.AttributeGenerator;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Fragment for selecting attribute generation method and editing attributes.
 */
public class AttributesStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private FragmentWizardAttributesBinding binding;

    private List<Integer> availableStandardValues = new ArrayList<>(Arrays.asList(15, 14, 13, 12, 10, 8));
    private List<Integer> rolledRaw = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardAttributesBinding.inflate(inflater, container, false);
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

        String savedMethod = viewModel.attributeMethod;
        if ("ROLL".equals(savedMethod)) binding.attrMethodGroup.check(R.id.radio_roll);
        else if ("POINT_BUY".equals(savedMethod)) binding.attrMethodGroup.check(R.id.radio_point_buy);
        else binding.attrMethodGroup.check(R.id.radio_standard);

        binding.attrMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_roll) switchToRollMethod();
            else if (checkedId == R.id.radio_point_buy) switchToPointBuyMethod();
            else switchToStandardMethod();
        });

        binding.rollResultText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.rollResultText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.rollButton.setOnClickListener(v -> performRoll());
        binding.applyRollButton.setOnClickListener(v -> applyRolledValues());
        binding.resetButton.setOnClickListener(v -> resetStandardMethod());

        binding.nextButton.setOnClickListener(v -> {
            // Calculate language bonus from INT
            int intScore = viewModel.attributes.get(3);
            int intMod = CharacterEngine.getAbilityModifier(intScore);
            viewModel.bonusLanguagesFromInt = Math.max(0, intMod);

            // Calculate spellcasting ability modifier
            String castingAbility = viewModel.spellcastingAbility;
            if (castingAbility != null && !castingAbility.isEmpty()) {
                int abilityScore = 0;
                switch (castingAbility) {
                    case "STR":
                        abilityScore = viewModel.attributes.get(0);
                        break;
                    case "DEX":
                        abilityScore = viewModel.attributes.get(1);
                        break;
                    case "CON":
                        abilityScore = viewModel.attributes.get(2);
                        break;
                    case "INT":
                        abilityScore = viewModel.attributes.get(3);
                        break;
                    case "WIS":
                        abilityScore = viewModel.attributes.get(4);
                        break;
                    case "CHA":
                        abilityScore = viewModel.attributes.get(5);
                        break;
                    default:
                        abilityScore = 10;
                }
                int mod = CharacterEngine.getAbilityModifier(abilityScore);
                viewModel.spellcastingAbilityMod = mod; // Can be negative in 5e
            }

            int checkedId = binding.attrMethodGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.radio_roll) viewModel.attributeMethod = "ROLL";
            else if (checkedId == R.id.radio_point_buy) viewModel.attributeMethod = "POINT_BUY";
            else viewModel.attributeMethod = "STANDARD";
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));

        updateRacialBonusText();

        int startId = binding.attrMethodGroup.getCheckedRadioButtonId();
        if (startId == R.id.radio_roll) switchToRollMethod();
        else if (startId == R.id.radio_point_buy) switchToPointBuyMethod();
        else switchToStandardMethod();
    }

    public void refreshAttributesWithRacialBonuses() {
        if (binding == null) return;
        viewModel.recalcFinalAttributes();
        updateRacialBonusText();
        int checkedId = binding.attrMethodGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_standard) refreshStandardUI();
        else if (checkedId == R.id.radio_point_buy) refreshPointBuyUI();
        else if (checkedId == R.id.radio_roll) refreshRollUI();
    }

    private void updateRacialBonusText() {
        if (binding == null) return;
        List<Integer> bonuses = viewModel.racialBonuses;
        String[] names = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        StringBuilder sb = new StringBuilder("Racial bonuses: ");
        boolean hasBonus = false;
        for (int i = 0; i < bonuses.size(); i++) {
            int bonus = bonuses.get(i);
            if (bonus != 0) {
                if (hasBonus) sb.append(", ");
                sb.append(names[i]).append(" +").append(bonus);
                hasBonus = true;
            }
        }
        if (!hasBonus) sb.append("none");
        binding.racialBonusText.setText(sb.toString());
    }

    // ---------- Standard method ----------
    private void switchToStandardMethod() {
        if (binding == null) return;
        binding.attributesStandardContainer.setVisibility(View.VISIBLE);
        binding.attributesPointbuyContainer.setVisibility(View.GONE);
        binding.attributesRollContainer.setVisibility(View.GONE);
        binding.remainingPointsText.setVisibility(View.GONE);
        binding.resetButton.setVisibility(View.VISIBLE);
        for (int i = 0; i < 6; i++) viewModel.baseAttributes.set(i, 0);
        viewModel.recalcFinalAttributes();
        refreshStandardUI();
    }

    private void refreshStandardUI() {
        if (binding == null) return;
        binding.attributesStandardContainer.removeAllViews();
        availableStandardValues = new ArrayList<>(Arrays.asList(15, 14, 13, 12, 10, 8));
        for (int val : viewModel.baseAttributes) {
            if (val != 0 && availableStandardValues.contains(val)) {
                availableStandardValues.remove((Integer) val);
            }
        }
        String[] attrNames = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (int i = 0; i < attrNames.length; i++) {
            final int index = i;
            ItemStandardAttributeBinding itemBinding = ItemStandardAttributeBinding.inflate(getLayoutInflater(), binding.attributesStandardContainer, false);
            itemBinding.attrLabel.setText(attrNames[index]);
            itemBinding.attrLabel.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_semibold));
            itemBinding.attrLabel.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

            List<Object> displayList = new ArrayList<>();
            displayList.add("— Choose —");
            displayList.addAll(availableStandardValues);
            int currentBase = viewModel.baseAttributes.get(index);
            if (currentBase != 0 && !displayList.contains(currentBase)) {
                displayList.add(currentBase);
                Collections.sort(displayList.subList(1, displayList.size()), (a, b) -> ((Integer) b).compareTo((Integer) a));
            }

            ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(requireContext(), android.R.layout.simple_spinner_item, displayList) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    Object item = getItem(position);
                    if (item instanceof Integer) view.setText(String.valueOf(item));
                    else view.setText((String) item);
                    view.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                    view.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    Object item = getItem(position);
                    if (item instanceof Integer) view.setText(String.valueOf(item));
                    else view.setText((String) item);
                    view.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                    view.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            itemBinding.attrSpinner.setAdapter(adapter);

            if (currentBase != 0) {
                int pos = displayList.indexOf(currentBase);
                if (pos >= 0) itemBinding.attrSpinner.setSelection(pos, false);
                else itemBinding.attrSpinner.setSelection(0, false);
            } else {
                itemBinding.attrSpinner.setSelection(0, false);
            }

            itemBinding.attrSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object selected = parent.getItemAtPosition(position);
                    if (selected instanceof String) {
                        int oldVal = viewModel.baseAttributes.get(index);
                        if (oldVal != 0 && !availableStandardValues.contains(oldVal)) {
                            availableStandardValues.add(oldVal);
                            Collections.sort(availableStandardValues, Collections.reverseOrder());
                        }
                        viewModel.baseAttributes.set(index, 0);
                        viewModel.recalcFinalAttributes();
                        refreshStandardUI();
                    } else {
                        int newVal = (Integer) selected;
                        int oldVal = viewModel.baseAttributes.get(index);
                        if (oldVal == newVal) return;
                        if (oldVal != 0 && !availableStandardValues.contains(oldVal)) {
                            availableStandardValues.add(oldVal);
                            Collections.sort(availableStandardValues, Collections.reverseOrder());
                        }
                        availableStandardValues.remove((Integer) newVal);
                        viewModel.baseAttributes.set(index, newVal);
                        viewModel.recalcFinalAttributes();
                        refreshStandardUI();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            binding.attributesStandardContainer.addView(itemBinding.getRoot());
        }
    }

    private void resetStandardMethod() {
        if (binding != null && binding.attrMethodGroup.getCheckedRadioButtonId() == R.id.radio_standard) {
            for (int i = 0; i < 6; i++) viewModel.baseAttributes.set(i, 0);
            viewModel.recalcFinalAttributes();
            refreshStandardUI();
            Toast.makeText(getContext(), "Reset choices", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- Point Buy method ----------
    private void switchToPointBuyMethod() {
        if (binding == null) return;
        binding.attributesStandardContainer.setVisibility(View.GONE);
        binding.attributesPointbuyContainer.setVisibility(View.VISIBLE);
        binding.attributesRollContainer.setVisibility(View.GONE);
        binding.remainingPointsText.setVisibility(View.VISIBLE);
        binding.resetButton.setVisibility(View.GONE);
        for (int i = 0; i < 6; i++) viewModel.baseAttributes.set(i, 8);
        viewModel.recalcFinalAttributes();
        refreshPointBuyUI();
    }

    private void refreshPointBuyUI() {
        if (binding == null) return;
        binding.attributesPointbuyContainer.removeAllViews();
        String[] names = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (int i = 0; i < names.length; i++) {
            final int index = i;
            ItemAttributeEditorBinding itemBinding = ItemAttributeEditorBinding.inflate(getLayoutInflater(), binding.attributesPointbuyContainer, false);
            TextView costHint = new TextView(requireContext());
            costHint.setTextSize(12);
            costHint.setPadding((8), 0, 0, 0);
            costHint.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            costHint.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
            itemBinding.getRoot().addView(costHint);

            itemBinding.attrLabel.setText(names[index]);
            itemBinding.attrLabel.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_semibold));
            itemBinding.attrLabel.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

            int currentBase = viewModel.baseAttributes.get(index);
            itemBinding.attrValue.setText(String.valueOf(currentBase));
            itemBinding.attrValue.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            itemBinding.attrValue.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
            updateCostHint(costHint, currentBase, currentBase);

            itemBinding.btnMinus.setOnClickListener(v -> {
                int oldVal = viewModel.baseAttributes.get(index);
                if (oldVal > 8) {
                    int newVal = oldVal - 1;
                    if (isPointBuyValid(index, newVal)) {
                        viewModel.baseAttributes.set(index, newVal);
                        viewModel.recalcFinalAttributes();
                        itemBinding.attrValue.setText(String.valueOf(newVal));
                        updateCostHint(costHint, newVal, oldVal);
                        updateRemainingPoints();
                    } else {
                        Toast.makeText(getContext(), "Not enough points", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            itemBinding.btnPlus.setOnClickListener(v -> {
                int oldVal = viewModel.baseAttributes.get(index);
                if (oldVal < 15) {
                    int newVal = oldVal + 1;
                    if (isPointBuyValid(index, newVal)) {
                        viewModel.baseAttributes.set(index, newVal);
                        viewModel.recalcFinalAttributes();
                        itemBinding.attrValue.setText(String.valueOf(newVal));
                        updateCostHint(costHint, newVal, oldVal);
                        updateRemainingPoints();
                    } else {
                        Toast.makeText(getContext(), "Not enough points", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Maximum 15", Toast.LENGTH_SHORT).show();
                }
            });
            binding.attributesPointbuyContainer.addView(itemBinding.getRoot());
        }
        updateRemainingPoints();
    }

    private void updateCostHint(TextView costHint, int newVal, int oldVal) {
        int newCost = AttributeGenerator.getPointCost(newVal);
        int oldCost = AttributeGenerator.getPointCost(oldVal);
        int delta = newCost - oldCost;
        if (delta > 0) costHint.setText("+" + delta + " pts");
        else if (delta < 0) costHint.setText(delta + " pts");
        else costHint.setText("0 pts");
    }

    private boolean isPointBuyValid(int index, int newVal) {
        int totalCost = 0;
        for (int i = 0; i < 6; i++) {
            int val = (i == index) ? newVal : viewModel.baseAttributes.get(i);
            totalCost += AttributeGenerator.getPointCost(val);
        }
        return totalCost <= 27;
    }

    private void updateRemainingPoints() {
        if (binding == null) return;
        int totalCost = 0;
        for (int i = 0; i < 6; i++) {
            totalCost += AttributeGenerator.getPointCost(viewModel.baseAttributes.get(i));
        }
        int remaining = 27 - totalCost;
        binding.remainingPointsText.setText("Remaining points: " + remaining);
        binding.remainingPointsText.setTextColor(remaining < 0 ? 0xFFFF0000 : 0xFF000000);
    }

    // ---------- Roll method ----------
    private void switchToRollMethod() {
        if (binding == null) return;
        binding.attributesStandardContainer.setVisibility(View.GONE);
        binding.attributesPointbuyContainer.setVisibility(View.GONE);
        binding.attributesRollContainer.setVisibility(View.VISIBLE);
        binding.remainingPointsText.setVisibility(View.GONE);
        binding.resetButton.setVisibility(View.GONE);
        if (rolledRaw.isEmpty()) performRoll();
        else displayRolledValues();
        refreshRollUI();
    }

    private void performRoll() {
        rolledRaw = AttributeGenerator.roll4d6DropLowest();
        displayRolledValues();
    }

    private void displayRolledValues() {
        if (binding == null) return;
        StringBuilder sb = new StringBuilder("Rolled values:\n");
        for (int v : rolledRaw) sb.append(v).append(" ");
        binding.rollResultText.setText(sb.toString());
    }

    private void applyRolledValues() {
        if (rolledRaw.size() == 6) {
            for (int i = 0; i < 6; i++) viewModel.baseAttributes.set(i, rolledRaw.get(i));
            viewModel.recalcFinalAttributes();
            refreshRollUI();
            Toast.makeText(getContext(), "Values assigned to attributes (order: STR, DEX, CON, INT, WIS, CHA)", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Roll first", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshRollUI() {
        if (binding == null || binding.rollPreviewContainer == null) return;
        binding.rollPreviewContainer.removeAllViews();
        String[] names = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        List<Integer> finalAttrs = viewModel.attributes;
        for (int i = 0; i < names.length; i++) {
            TextView tv = new TextView(requireContext());
            tv.setText(names[i] + ": " + finalAttrs.get(i));
            tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
            tv.setPadding(0, (4), 0, (4));
            binding.rollPreviewContainer.addView(tv);
        }
    }
}
