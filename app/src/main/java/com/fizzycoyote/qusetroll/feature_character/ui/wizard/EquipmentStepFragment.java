package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.character.CharacterCreationDTO;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;

public class EquipmentStepFragment extends Fragment {

    private WizardViewModel viewModel;

    // Background
    private TextView backgroundDescText;
    private EditText backgroundGoldEdit;
    private Button addBackgroundItemButton;
    private LinearLayout backgroundItemsContainer;

    // Class
    private TextView classDescText;
    private RadioGroup classChoiceRadio;
    private LinearLayout classPackageContainer;
    private LinearLayout classGoldContainer;
    private TextView classGoldAmount;
    private Button rerollGoldButton;
    private Button addClassItemButton;
    private LinearLayout classItemsContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_equipment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        // Background
        backgroundDescText = view.findViewById(R.id.background_equipment_desc);
        backgroundGoldEdit = view.findViewById(R.id.background_gold_edit);
        addBackgroundItemButton = view.findViewById(R.id.add_background_item_button);
        backgroundItemsContainer = view.findViewById(R.id.background_items_container);

        // Class
        classDescText = view.findViewById(R.id.class_equipment_desc);
        classChoiceRadio = view.findViewById(R.id.class_choice_radio);
        classPackageContainer = view.findViewById(R.id.class_package_container);
        classGoldContainer = view.findViewById(R.id.class_gold_container);
        classGoldAmount = view.findViewById(R.id.class_gold_amount);
        rerollGoldButton = view.findViewById(R.id.reroll_gold_button);
        addClassItemButton = view.findViewById(R.id.add_class_item_button);
        classItemsContainer = view.findViewById(R.id.class_items_container);

        Button nextButton = view.findViewById(R.id.next_button);
        Button backButton = view.findViewById(R.id.back_button);

        backgroundDescText.setText(viewModel.backgroundEquipmentDescription);
        backgroundGoldEdit.setText(String.valueOf(viewModel.backgroundGold));
        classDescText.setText(viewModel.classEquipmentDescription);

        updateBackgroundItemsList();
        updateClassItemsList();

        backgroundGoldEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                try {
                    viewModel.backgroundGold = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    viewModel.backgroundGold = 0;
                }
            }
        });

        addBackgroundItemButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("add_to_background", true);
            args.putBoolean("add_to_class", false);
            Navigation.findNavController(v).navigate(R.id.action_equipment_to_item_search, args);
        });

        addClassItemButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("add_to_background", false);
            args.putBoolean("add_to_class", true);
            Navigation.findNavController(v).navigate(R.id.action_equipment_to_item_search, args);
        });

        classChoiceRadio.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_class_items) {
                viewModel.useClassEquipment = true;
                classPackageContainer.setVisibility(View.VISIBLE);
                classGoldContainer.setVisibility(View.GONE);
            } else {
                viewModel.useClassEquipment = false;
                classPackageContainer.setVisibility(View.GONE);
                classGoldContainer.setVisibility(View.VISIBLE);
                classGoldAmount.setText("Gold: " + viewModel.classStartingGold + " gp (" + viewModel.classGoldDice + " × 10)");
            }
        });

        rerollGoldButton.setOnClickListener(v -> {
            String[] parts = viewModel.classGoldDice.split("d");
            if (parts.length < 2) return;
            try {
                int diceCount = Integer.parseInt(parts[0]);
                int diceSides = Integer.parseInt(parts[1]);
                int total = 0;
                for (int i = 0; i < diceCount; i++) {
                    total += (int) (Math.random() * diceSides) + 1;
                }
                int newGold = total * 10;
                viewModel.classStartingGold = newGold;
                classGoldAmount.setText("Gold: " + newGold + " gp (" + viewModel.classGoldDice + " × 10)");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid gold dice format", Toast.LENGTH_SHORT).show();
            }
        });

        nextButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_equipment_to_summary));
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBackgroundItemsList();
        updateClassItemsList();
    }

    private void updateBackgroundItemsList() {
        backgroundItemsContainer.removeAllViews();
        if (viewModel.backgroundCustomItems.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No items – add via search");
            backgroundItemsContainer.addView(empty);
        } else {
            for (CharacterCreationDTO.InventoryItemDTO item : viewModel.backgroundCustomItems) {
                TextView tv = new TextView(getContext());
                tv.setText(item.customName + " (x" + item.quantity + ", weight: " + item.customWeight + ")");
                tv.setPadding(16, 8, 16, 8);
                tv.setOnLongClickListener(v -> {
                    int index = viewModel.backgroundCustomItems.indexOf(item);
                    if (index != -1) {
                        viewModel.backgroundCustomItems.remove(index);
                        updateBackgroundItemsList();
                        Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
                backgroundItemsContainer.addView(tv);
            }
        }
    }

    private void updateClassItemsList() {
        classItemsContainer.removeAllViews();
        if (viewModel.classEquipment.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No items – add via search");
            classItemsContainer.addView(empty);
        } else {
            for (CharacterCreationDTO.InventoryItemDTO item : viewModel.classEquipment) {
                TextView tv = new TextView(getContext());
                tv.setText(item.customName + " (x" + item.quantity + ", weight: " + item.customWeight + ")");
                tv.setPadding(16, 8, 16, 8);
                tv.setOnLongClickListener(v -> {
                    int index = viewModel.classEquipment.indexOf(item);
                    if (index != -1) {
                        viewModel.classEquipment.remove(index);
                        updateClassItemsList();
                        Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
                classItemsContainer.addView(tv);
            }
        }
    }
}