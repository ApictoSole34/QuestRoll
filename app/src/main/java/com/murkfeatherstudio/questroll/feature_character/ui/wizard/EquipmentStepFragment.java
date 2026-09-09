package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.character.CharacterCreationDTO;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardEquipmentBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

public class EquipmentStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private FragmentWizardEquipmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardEquipmentBinding.inflate(inflater, container, false);
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

        binding.backgroundEquipmentDesc.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.backgroundEquipmentDesc.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.classEquipmentDesc.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.classEquipmentDesc.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.classGoldAmount.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.classGoldAmount.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.backgroundEquipmentDesc.setText(viewModel.backgroundEquipmentDescription);
        binding.backgroundGoldEdit.setText(String.valueOf(viewModel.backgroundGold));
        binding.classEquipmentDesc.setText(viewModel.classEquipmentDescription);

        updateBackgroundFixedItemsList();
        updateBackgroundItemsList();
        updateClassItemsList();
        updateClassFixedItemsList();

        binding.backgroundGoldEdit.addTextChangedListener(new TextWatcher() {
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

        binding.addBackgroundItemButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("add_to_background", true);
            args.putBoolean("add_to_class", false);
            Navigation.findNavController(v).navigate(R.id.action_equipment_to_item_search, args);
        });

        binding.addClassItemButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("add_to_background", false);
            args.putBoolean("add_to_class", true);
            Navigation.findNavController(v).navigate(R.id.action_equipment_to_item_search, args);
        });

        binding.classChoiceRadio.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_class_items) {
                viewModel.useClassEquipment = true;
                binding.classPackageContainer.setVisibility(View.VISIBLE);
                binding.classGoldContainer.setVisibility(View.GONE);
            } else {
                viewModel.useClassEquipment = false;
                binding.classPackageContainer.setVisibility(View.GONE);
                binding.classGoldContainer.setVisibility(View.VISIBLE);
                binding.classGoldAmount.setText("Gold: " + viewModel.classStartingGold + " gp (" + viewModel.classGoldDice + " × 10)");
            }
        });

        binding.rerollGoldButton.setOnClickListener(v -> {
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
                binding.classGoldAmount.setText("Gold: " + newGold + " gp (" + viewModel.classGoldDice + " × 10)");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid gold dice format", Toast.LENGTH_SHORT).show();
            }
        });

        binding.nextButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_equipment_to_summary));
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) return;
        updateBackgroundItemsList();
        updateClassItemsList();
        updateClassFixedItemsList();
    }

    private void updateClassFixedItemsList() {
        if (binding == null) return;
        binding.classFixedItemsContainer.removeAllViews();
        if (viewModel.classFixedItems.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No fixed items");
            empty.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            empty.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
            binding.classFixedItemsContainer.addView(empty);
        } else {
            for (String itemName : viewModel.classFixedItems) {
                TextView tv = new TextView(getContext());
                tv.setText("• " + itemName);
                tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                tv.setPadding(dp(16), dp(4), 0, dp(4));
                binding.classFixedItemsContainer.addView(tv);
            }
        }
    }

    private void updateBackgroundFixedItemsList() {
        if (binding == null) return;
        binding.backgroundFixedItemsContainer.removeAllViews();
        if (viewModel.backgroundFixedItems.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No fixed items");
            empty.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            empty.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
            binding.backgroundFixedItemsContainer.addView(empty);
        } else {
            for (String itemName : viewModel.backgroundFixedItems) {
                TextView tv = new TextView(getContext());
                tv.setText("• " + itemName);
                tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                tv.setPadding(dp(16), dp(4), 0, dp(4));
                binding.backgroundFixedItemsContainer.addView(tv);
            }
        }
    }

    private void updateBackgroundItemsList() {
        if (binding == null) return;
        binding.backgroundItemsContainer.removeAllViews();
        if (viewModel.backgroundCustomItems.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No items – add via search");
            empty.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            empty.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
            binding.backgroundItemsContainer.addView(empty);
        } else {
            for (CharacterCreationDTO.InventoryItemDTO item : viewModel.backgroundCustomItems) {
                TextView tv = new TextView(getContext());
                tv.setText(item.customName + " (x" + item.quantity + ", weight: " + item.customWeight + ")");
                tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
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
                binding.backgroundItemsContainer.addView(tv);
            }
        }
    }

    private void updateClassItemsList() {
        if (binding == null) return;
        binding.classItemsContainer.removeAllViews();
        if (viewModel.classEquipment.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No items – add via search");
            empty.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            empty.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
            binding.classItemsContainer.addView(empty);
        } else {
            for (CharacterCreationDTO.InventoryItemDTO item : viewModel.classEquipment) {
                TextView tv = new TextView(getContext());
                tv.setText(item.customName + " (x" + item.quantity + ", weight: " + item.customWeight + ")");
                tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
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
                binding.classItemsContainer.addView(tv);
            }
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
