package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterCreationDTO;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item_set.ItemSetEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardItemSearchStepBinding;
import com.murkfeatherstudio.questroll.databinding.ItemSearchRowBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class ItemSearchStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private FragmentWizardItemSearchStepBinding binding;
    private ItemSearchAdapter adapter;
    private List<ItemEntity> allItems = new ArrayList<>();
    private List<ItemSetEntity> allItemSets = new ArrayList<>();

    private boolean addToBackground = false;
    private boolean addToClass = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardItemSearchStepBinding.inflate(inflater, container, false);
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

        if (getArguments() != null) {
            addToBackground = getArguments().getBoolean("add_to_background", false);
            addToClass = getArguments().getBoolean("add_to_class", false);
        }

        binding.searchEditText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.searchEditText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.searchEditText.setHintTextColor(getResources().getColor(R.color.threads_text_secondary, null));

        binding.itemRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemSearchAdapter();
        binding.itemRecycler.setAdapter(adapter);

        loadData();

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.cancelButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
        });
    }

    private void loadData() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            String gameSystem = viewModel.gameSystem;
            allItems = Open5eDatabase.getInstance(requireContext())
                    .itemDao()
                    .getAllByGameSystem(gameSystem);
            allItemSets = Open5eDatabase.getInstance(requireContext())
                    .itemSetDao()
                    .getAllSync();

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                filterItems("");
            });
        });
    }

    private void filterItems(String query) {
        List<Object> results = new ArrayList<>();
        if (query.isEmpty()) {
            results.addAll(allItems);
            results.addAll(allItemSets);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ItemEntity item : allItems) {
                if (item.name != null && item.name.toLowerCase().contains(lowerQuery)) {
                    results.add(item);
                }
            }
            for (ItemSetEntity set : allItemSets) {
                if (set.name != null && set.name.toLowerCase().contains(lowerQuery)) {
                    results.add(set);
                }
            }
        }
        if (adapter != null) {
            adapter.setResults(results);
        }
    }

    private class ItemSearchAdapter extends RecyclerView.Adapter<ItemSearchAdapter.ViewHolder> {
        private List<Object> results = new ArrayList<>();

        void setResults(List<Object> results) {
            this.results = results;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSearchRowBinding itemBinding = ItemSearchRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Object obj = results.get(position);

            holder.binding.itemName.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            holder.binding.itemName.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
            holder.binding.itemDesc.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            holder.binding.itemDesc.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));

            if (obj instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) obj;
                holder.binding.itemName.setText(item.name);
                holder.binding.itemDesc.setText(item.desc != null ? item.desc : "");
                holder.binding.selectButton.setOnClickListener(v -> {
                    CharacterCreationDTO.InventoryItemDTO dtoItem = new CharacterCreationDTO.InventoryItemDTO();
                    dtoItem.itemKey = item.key;
                    dtoItem.customName = item.name;
                    dtoItem.quantity = 1;
                    dtoItem.customWeight = item.weight;
                    if (addToBackground) {
                        viewModel.backgroundCustomItems.add(dtoItem);
                        Toast.makeText(getContext(), "Added to background: " + item.name, Toast.LENGTH_SHORT).show();
                    } else if (addToClass) {
                        viewModel.classEquipment.add(dtoItem);
                        Toast.makeText(getContext(), "Added to class: " + item.name, Toast.LENGTH_SHORT).show();
                    }
                    NavController navController = Navigation.findNavController(v);
                    navController.popBackStack();
                });
            } else if (obj instanceof ItemSetEntity) {
                ItemSetEntity set = (ItemSetEntity) obj;
                holder.binding.itemName.setText(set.name);
                holder.binding.itemDesc.setText(set.desc != null ? set.desc : "");
                holder.binding.selectButton.setOnClickListener(v -> showItemSetSelectionDialog(set));
            }
        }

        @Override
        public int getItemCount() { return results.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemSearchRowBinding binding;
            ViewHolder(ItemSearchRowBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private void showItemSetSelectionDialog(ItemSetEntity set) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            List<ItemEntity> items = Open5eDatabase.getInstance(requireContext())
                    .itemDao()
                    .getByKeysAndGameSystemSync(set.itemKeys, viewModel.gameSystem);

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                if (items.isEmpty()) {
                    Toast.makeText(getContext(), "No items in this set for the selected system", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Select items from set: " + set.name);
                String[] itemNames = items.stream().map(i -> i.name).toArray(String[]::new);
                boolean[] checkedItems = new boolean[items.size()];
                builder.setMultiChoiceItems(itemNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                });
                builder.setPositiveButton("Add selected", (dialog, which) -> {
                    for (int i = 0; i < items.size(); i++) {
                        if (checkedItems[i]) {
                            ItemEntity item = items.get(i);
                            CharacterCreationDTO.InventoryItemDTO dtoItem = new CharacterCreationDTO.InventoryItemDTO();
                            dtoItem.itemKey = item.key;
                            dtoItem.customName = item.name;
                            dtoItem.quantity = 1;
                            dtoItem.customWeight = item.weight;
                            if (addToBackground) {
                                viewModel.backgroundCustomItems.add(dtoItem);
                            } else if (addToClass) {
                                viewModel.classEquipment.add(dtoItem);
                            }
                        }
                    }
                    Toast.makeText(getContext(), "Added selected items", Toast.LENGTH_SHORT).show();
                    if (isAdded()) {
                        NavController navController = Navigation.findNavController(requireView());
                        navController.popBackStack();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            });
        });
    }
}
