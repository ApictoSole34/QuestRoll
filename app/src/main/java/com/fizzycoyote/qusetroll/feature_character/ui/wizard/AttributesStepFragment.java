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
import com.fizzycoyote.qusetroll.feature_character.utils.AttributeGenerator;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Fragment for selecting attribute generation method and editing attributes.
 * Supports Standard (spinners), Point Buy (+/- buttons, cost), and Roll 4d6 drop lowest.
 * All changes are stored in WizardViewModel.baseAttributes, final values (with racial/background bonuses)
 * are displayed and stored in attributes.
 */
public class AttributesStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private RadioGroup methodGroup;
    private LinearLayout standardContainer, pointBuyContainer, rollContainer;
    private TextView remainingPointsText, rollResultText, racialBonusText;
    private Button rollButton, applyRollButton, resetButton;

    private List<Integer> availableStandardValues = new ArrayList<>(Arrays.asList(15, 14, 13, 12, 10, 8));
    private List<Spinner> standardSpinners = new ArrayList<>();
    private List<ArrayAdapter<Object>> standardAdapters = new ArrayList<>();
    private List<Integer> rolledRaw = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_attributes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        methodGroup = view.findViewById(R.id.attr_method_group);
        standardContainer = view.findViewById(R.id.attributes_standard_container);
        pointBuyContainer = view.findViewById(R.id.attributes_pointbuy_container);
        rollContainer = view.findViewById(R.id.attributes_roll_container);
        remainingPointsText = view.findViewById(R.id.remaining_points_text);
        rollResultText = view.findViewById(R.id.roll_result_text);
        racialBonusText = view.findViewById(R.id.racial_bonus_text);
        rollButton = view.findViewById(R.id.roll_button);
        applyRollButton = view.findViewById(R.id.apply_roll_button);
        resetButton = view.findViewById(R.id.reset_button);

        Button nextButton = view.findViewById(R.id.next_button);
        Button backButton = view.findViewById(R.id.back_button);

        String savedMethod = viewModel.attributeMethod;
        if ("ROLL".equals(savedMethod)) methodGroup.check(R.id.radio_roll);
        else if ("POINT_BUY".equals(savedMethod)) methodGroup.check(R.id.radio_point_buy);
        else methodGroup.check(R.id.radio_standard);

        methodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_roll) switchToRollMethod();
            else if (checkedId == R.id.radio_point_buy) switchToPointBuyMethod();
            else switchToStandardMethod();
        });

        rollButton.setOnClickListener(v -> performRoll());
        applyRollButton.setOnClickListener(v -> applyRolledValues());
        resetButton.setOnClickListener(v -> resetStandardMethod());

        nextButton.setOnClickListener(v -> {
            // Calculate language bonus from INT
            int intScore = viewModel.attributes.get(3);
            int intMod = (intScore - 10) / 2;
            viewModel.bonusLanguagesFromInt = Math.max(0, intMod);

            // Calculate spellcasting ability modifier
            String castingAbility = viewModel.spellcastingAbility;
            if (castingAbility != null && !castingAbility.isEmpty()) {
                int abilityScore = 0;
                switch (castingAbility) {
                    case "STR": abilityScore = viewModel.attributes.get(0); break;
                    case "DEX": abilityScore = viewModel.attributes.get(1); break;
                    case "CON": abilityScore = viewModel.attributes.get(2); break;
                    case "INT": abilityScore = viewModel.attributes.get(3); break;
                    case "WIS": abilityScore = viewModel.attributes.get(4); break;
                    case "CHA": abilityScore = viewModel.attributes.get(5); break;
                    default: abilityScore = 10;
                }
                int mod = (abilityScore - 10) / 2;
                viewModel.spellcastingAbilityMod = Math.max(0, mod);
            }

            int checkedId = methodGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.radio_roll) viewModel.attributeMethod = "ROLL";
            else if (checkedId == R.id.radio_point_buy) viewModel.attributeMethod = "POINT_BUY";
            else viewModel.attributeMethod = "STANDARD";
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));

        updateRacialBonusText();

        int startId = methodGroup.getCheckedRadioButtonId();
        if (startId == R.id.radio_roll) switchToRollMethod();
        else if (startId == R.id.radio_point_buy) switchToPointBuyMethod();
        else switchToStandardMethod();
    }

    public void refreshAttributesWithRacialBonuses() {
        viewModel.recalcFinalAttributes();
        updateRacialBonusText();
        int checkedId = methodGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_standard) refreshStandardUI();
        else if (checkedId == R.id.radio_point_buy) refreshPointBuyUI();
        else if (checkedId == R.id.radio_roll) refreshRollUI();
    }

    private void updateRacialBonusText() {
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
        racialBonusText.setText(sb.toString());
    }

    // ---------- Standard method ----------
    private void switchToStandardMethod() {
        standardContainer.setVisibility(View.VISIBLE);
        pointBuyContainer.setVisibility(View.GONE);
        rollContainer.setVisibility(View.GONE);
        remainingPointsText.setVisibility(View.GONE);
        resetButton.setVisibility(View.VISIBLE);
        for (int i = 0; i < 6; i++) viewModel.baseAttributes.set(i, 0);
        viewModel.recalcFinalAttributes();
        refreshStandardUI();
    }

    private void refreshStandardUI() {
        standardContainer.removeAllViews();
        standardSpinners.clear();
        standardAdapters.clear();
        availableStandardValues = new ArrayList<>(Arrays.asList(15, 14, 13, 12, 10, 8));
        for (int val : viewModel.baseAttributes) {
            if (val != 0 && availableStandardValues.contains(val)) {
                availableStandardValues.remove((Integer) val);
            }
        }
        String[] attrNames = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (int i = 0; i < attrNames.length; i++) {
            final int index = i;
            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_standard_attribute, standardContainer, false);
            TextView label = item.findViewById(R.id.attr_label);
            Spinner spinner = item.findViewById(R.id.attr_spinner);
            label.setText(attrNames[index]);

            List<Object> displayList = new ArrayList<>();
            displayList.add("— Choose —");
            displayList.addAll(availableStandardValues);
            int currentBase = viewModel.baseAttributes.get(index);
            if (currentBase != 0 && !displayList.contains(currentBase)) {
                displayList.add(currentBase);
                Collections.sort(displayList.subList(1, displayList.size()), (a, b) -> ((Integer) b).compareTo((Integer) a));
            }

            ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(getContext(), android.R.layout.simple_spinner_item, displayList) {
                @NonNull @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    Object item = getItem(position);
                    if (item instanceof Integer) view.setText(String.valueOf(item));
                    else view.setText((String) item);
                    return view;
                }
                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    Object item = getItem(position);
                    if (item instanceof Integer) view.setText(String.valueOf(item));
                    else view.setText((String) item);
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            if (currentBase != 0) {
                int pos = displayList.indexOf(currentBase);
                if (pos >= 0) spinner.setSelection(pos, false);
                else spinner.setSelection(0, false);
            } else {
                spinner.setSelection(0, false);
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
            standardContainer.addView(item);
            standardSpinners.add(spinner);
            standardAdapters.add(adapter);
        }
    }

    private void resetStandardMethod() {
        if (methodGroup.getCheckedRadioButtonId() == R.id.radio_standard) {
            for (int i = 0; i < 6; i++) viewModel.baseAttributes.set(i, 0);
            viewModel.recalcFinalAttributes();
            refreshStandardUI();
            Toast.makeText(getContext(), "Reset choices", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- Point Buy method ----------
    private void switchToPointBuyMethod() {
        standardContainer.setVisibility(View.GONE);
        pointBuyContainer.setVisibility(View.VISIBLE);
        rollContainer.setVisibility(View.GONE);
        remainingPointsText.setVisibility(View.VISIBLE);
        resetButton.setVisibility(View.GONE);
        for (int i = 0; i < 6; i++) viewModel.baseAttributes.set(i, 8);
        viewModel.recalcFinalAttributes();
        refreshPointBuyUI();
    }

    private void refreshPointBuyUI() {
        pointBuyContainer.removeAllViews();
        String[] names = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (int i = 0; i < names.length; i++) {
            final int index = i;
            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_attribute_editor, pointBuyContainer, false);
            TextView label = item.findViewById(R.id.attr_label);
            TextView valueView = item.findViewById(R.id.attr_value);
            Button btnMinus = item.findViewById(R.id.btn_minus);
            Button btnPlus = item.findViewById(R.id.btn_plus);
            TextView costHint = new TextView(getContext());
            costHint.setTextSize(12);
            costHint.setPadding(8,0,0,0);
            ((LinearLayout) item).addView(costHint);

            label.setText(names[i]);
            int currentBase = viewModel.baseAttributes.get(index);
            valueView.setText(String.valueOf(currentBase));
            updateCostHint(costHint, currentBase, currentBase);

            btnMinus.setOnClickListener(v -> {
                int oldVal = viewModel.baseAttributes.get(index);
                if (oldVal > 8) {
                    int newVal = oldVal - 1;
                    if (isPointBuyValid(index, newVal)) {
                        viewModel.baseAttributes.set(index, newVal);
                        viewModel.recalcFinalAttributes();
                        valueView.setText(String.valueOf(newVal));
                        updateCostHint(costHint, newVal, oldVal);
                        updateRemainingPoints();
                    } else {
                        Toast.makeText(getContext(), "Not enough points", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            btnPlus.setOnClickListener(v -> {
                int oldVal = viewModel.baseAttributes.get(index);
                if (oldVal < 15) {
                    int newVal = oldVal + 1;
                    if (isPointBuyValid(index, newVal)) {
                        viewModel.baseAttributes.set(index, newVal);
                        viewModel.recalcFinalAttributes();
                        valueView.setText(String.valueOf(newVal));
                        updateCostHint(costHint, newVal, oldVal);
                        updateRemainingPoints();
                    } else {
                        Toast.makeText(getContext(), "Not enough points", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Maximum 15", Toast.LENGTH_SHORT).show();
                }
            });
            pointBuyContainer.addView(item);
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
        int totalCost = 0;
        for (int i = 0; i < 6; i++) {
            totalCost += AttributeGenerator.getPointCost(viewModel.baseAttributes.get(i));
        }
        int remaining = 27 - totalCost;
        remainingPointsText.setText("Remaining points: " + remaining);
        remainingPointsText.setTextColor(remaining < 0 ? 0xFFFF0000 : 0xFF000000);
    }

    // ---------- Roll method ----------
    private void switchToRollMethod() {
        standardContainer.setVisibility(View.GONE);
        pointBuyContainer.setVisibility(View.GONE);
        rollContainer.setVisibility(View.VISIBLE);
        remainingPointsText.setVisibility(View.GONE);
        resetButton.setVisibility(View.GONE);
        if (rolledRaw.isEmpty()) performRoll();
        else displayRolledValues();
        refreshRollUI();
    }

    private void performRoll() {
        rolledRaw = AttributeGenerator.roll4d6DropLowest();
        displayRolledValues();
    }

    private void displayRolledValues() {
        StringBuilder sb = new StringBuilder("Rolled values:\n");
        for (int v : rolledRaw) sb.append(v).append(" ");
        rollResultText.setText(sb.toString());
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
        LinearLayout preview = getView().findViewById(R.id.roll_preview_container);
        if (preview == null) return;
        preview.removeAllViews();
        String[] names = {"STR","DEX","CON","INT","WIS","CHA"};
        List<Integer> finalAttrs = viewModel.attributes;
        for (int i = 0; i < names.length; i++) {
            TextView tv = new TextView(getContext());
            tv.setText(names[i] + ": " + finalAttrs.get(i));
            preview.addView(tv);
        }
    }
}