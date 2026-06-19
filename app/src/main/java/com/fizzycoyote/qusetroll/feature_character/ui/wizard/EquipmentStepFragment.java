package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
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
    private LinearLayout backgroundFixedItemsContainer;

    // Class
    private TextView classDescText;
    private RadioGroup classChoiceRadio;
    private LinearLayout classPackageContainer;
    private LinearLayout classGoldContainer;
    private TextView classGoldAmount;
    private Button rerollGoldButton;
    private Button addClassItemButton;
    private LinearLayout classItemsContainer;
    private LinearLayout classFixedItemsContainer;

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
        backgroundFixedItemsContainer = view.findViewById(R.id.background_fixed_items_container);

        // Class
        classDescText = view.findViewById(R.id.class_equipment_desc);
        classChoiceRadio = view.findViewById(R.id.class_choice_radio);
        classPackageContainer = view.findViewById(R.id.class_package_container);
        classGoldContainer = view.findViewById(R.id.class_gold_container);
        classGoldAmount = view.findViewById(R.id.class_gold_amount);
        rerollGoldButton = view.findViewById(R.id.reroll_gold_button);
        addClassItemButton = view.findViewById(R.id.add_class_item_button);
        classItemsContainer = view.findViewById(R.id.class_items_container);
        classFixedItemsContainer = view.findViewById(R.id.class_fixed_items_container);

        Button nextButton = view.findViewById(R.id.next_button);
        Button backButton = view.findViewById(R.id.back_button);

        backgroundDescText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
        backgroundDescText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        classDescText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
        classDescText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        classGoldAmount.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
        classGoldAmount.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        backgroundDescText.setText(viewModel.backgroundEquipmentDescription);
        backgroundGoldEdit.setText(String.valueOf(viewModel.backgroundGold));
        classDescText.setText(viewModel.classEquipmentDescription);

        updateBackgroundFixedItemsList();
        updateBackgroundItemsList();
        updateClassItemsList();
        updateClassFixedItemsList();

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
        updateClassFixedItemsList();
    }

    private void updateClassFixedItemsList() {
        classFixedItemsContainer.removeAllViews();
        if (viewModel.classFixedItems.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No fixed items");
            empty.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
            empty.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
            classFixedItemsContainer.addView(empty);
        } else {
            for (String itemName : viewModel.classFixedItems) {
                TextView tv = new TextView(getContext());
                tv.setText("• " + itemName);
                tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                tv.setPadding(dp(16), dp(4), 0, dp(4));
                classFixedItemsContainer.addView(tv);
            }
        }
    }

    private void updateBackgroundFixedItemsList() {
        backgroundFixedItemsContainer.removeAllViews();
        if (viewModel.backgroundFixedItems.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No fixed items");
            empty.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
            empty.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
            backgroundFixedItemsContainer.addView(empty);
        } else {
            for (String itemName : viewModel.backgroundFixedItems) {
                TextView tv = new TextView(getContext());
                tv.setText("• " + itemName);
                tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                tv.setPadding(dp(16), dp(4), 0, dp(4));
                backgroundFixedItemsContainer.addView(tv);
            }
        }
    }

    private void updateBackgroundItemsList() {
        backgroundItemsContainer.removeAllViews();
        if (viewModel.backgroundCustomItems.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No items – add via search");
            empty.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
            empty.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
            backgroundItemsContainer.addView(empty);
        } else {
            for (CharacterCreationDTO.InventoryItemDTO item : viewModel.backgroundCustomItems) {
                TextView tv = new TextView(getContext());
                tv.setText(item.customName + " (x" + item.quantity + ", weight: " + item.customWeight + ")");
                tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                tv.setPadding(dp(16), dp(8), dp(16), dp(8));
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
            empty.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
            empty.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
            classItemsContainer.addView(empty);
        } else {
            for (CharacterCreationDTO.InventoryItemDTO item : viewModel.classEquipment) {
                TextView tv = new TextView(getContext());
                tv.setText(item.customName + " (x" + item.quantity + ", weight: " + item.customWeight + ")");
                tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                tv.setPadding(dp(16), dp(8), dp(16), dp(8));
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

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}