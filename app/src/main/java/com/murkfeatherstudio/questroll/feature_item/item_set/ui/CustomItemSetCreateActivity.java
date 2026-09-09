package com.murkfeatherstudio.questroll.feature_item.item_set.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item.CustomItemEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomItemSetCreateBinding;
import com.murkfeatherstudio.questroll.databinding.DialogSelectItemsBinding;
import com.murkfeatherstudio.questroll.databinding.ItemCheckboxBinding;
import com.murkfeatherstudio.questroll.databinding.ItemSelectedItemBinding;
import com.murkfeatherstudio.questroll.feature_item.item_set.view_model.CustomItemSetCreateViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;


public class CustomItemSetCreateActivity extends BaseActivity {

    public static final String EXTRA_EDIT_ID = "edit_item_set_id";

    private CustomItemSetCreateViewModel viewModel;
    private ActivityCustomItemSetCreateBinding binding;
    private ItemSelectionAdapter itemAdapter;
    private long editId = -1;

    private final List<String> selectedItemKeys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomItemSetCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_EDIT_ID, -1);

        viewModel = new ViewModelProvider(this,
                new CustomItemSetCreateViewModel.Factory(
                        UserContentDatabase.getInstance(this).customItemSetDao(),
                        editId,
                        Executors.newSingleThreadExecutor()
                )).get(CustomItemSetCreateViewModel.class);

        initViews();
        setupObservers();

        setTitle(editId == -1 ? "Create Item Set" : "Edit Item Set");
        binding.btnSave.setText(editId == -1 ? "Save" : "Update");
        binding.btnSave.setOnClickListener(v -> save());
        binding.btnSelectItems.setOnClickListener(v -> showItemSelectionDialog());
    }

    private void initViews() {
        binding.rvSelectedItems.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemSelectionAdapter(new ArrayList<>(), this::removeItem);
        binding.rvSelectedItems.setAdapter(itemAdapter);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, set -> {
            if (set == null) return;
            binding.etName.setText(set.name);
            binding.etDesc.setText(set.desc);
            if (set.itemKeys != null) {
                selectedItemKeys.clear();
                selectedItemKeys.addAll(set.itemKeys);
                updateSelectedItemsAdapter();
            }
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, editId == -1 ? "Item set saved!" : "Item set updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "An item set with this name already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSelectedItemsAdapter() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<String> displayNames = new ArrayList<>();
            Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
            UserContentDatabase userDb = UserContentDatabase.getInstance(this);

            for (String key : selectedItemKeys) {
                String name = null;
                if (key.startsWith("custom_")) {
                    try {
                        long id = Long.parseLong(key.substring(7));
                        CustomItemEntity custom = userDb.customItemDao().getByIdSync(id);
                        if (custom != null) name = custom.name;
                    } catch (NumberFormatException ignored) {}
                } else {
                    ItemEntity item = open5eDb.itemDao().getByKeySync(key);
                    if (item != null) name = item.name;
                }
                if (name == null) name = key;
                displayNames.add(name);
            }
            AppExecutors.getInstance().mainThread().execute(() -> itemAdapter.updateItems(displayNames));
        });
    }

    private void removeItem(int position) {
        if (position >= 0 && position < selectedItemKeys.size()) {
            selectedItemKeys.remove(position);
            updateSelectedItemsAdapter();
        }
    }

    private void showItemSelectionDialog() {
        DialogSelectItemsBinding dialogBinding = DialogSelectItemsBinding.inflate(getLayoutInflater());
        dialogBinding.recyclerItems.setLayoutManager(new LinearLayoutManager(this));

        ItemSelectAdapter selectAdapter = new ItemSelectAdapter();
        dialogBinding.recyclerItems.setAdapter(selectAdapter);

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<ItemEntity> apiItems = Open5eDatabase.getInstance(this).itemDao().getAllSync();
            List<CustomItemEntity> customItems = UserContentDatabase.getInstance(this).customItemDao().getAllSync();
            List<ItemDisplay> allItems = new ArrayList<>();
            for (ItemEntity i : apiItems) allItems.add(new ItemDisplay(i.key, i.name, false));
            for (CustomItemEntity c : customItems) allItems.add(new ItemDisplay("custom_" + c.id, c.name, true));
            
            AppExecutors.getInstance().mainThread().execute(() -> {
                selectAdapter.setFullList(allItems);
                selectAdapter.setItems(allItems);
            });
        });

        dialogBinding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                selectAdapter.filter(newText);
                return true;
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Select Items")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add Selected", (d, w) -> {
                    List<String> newKeys = selectAdapter.getSelectedKeys();
                    for (String key : newKeys) {
                        if (!selectedItemKeys.contains(key)) {
                            selectedItemKeys.add(key);
                        }
                    }
                    updateSelectedItemsAdapter();
                    Toast.makeText(this, "Added " + newKeys.size() + " items", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void save() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }
        CustomItemSetEntity entity = new CustomItemSetEntity();
        entity.name = name;
        entity.desc = binding.etDesc.getText() != null ? binding.etDesc.getText().toString().trim() : "";
        entity.itemKeys = new ArrayList<>(selectedItemKeys);
        viewModel.save(entity);
    }

    static class ItemDisplay {
        String key;
        String name;
        boolean isCustom;
        ItemDisplay(String k, String n, boolean c) {
            key = k;
            name = n;
            isCustom = c;
        }
    }

    static class ItemSelectAdapter extends RecyclerView.Adapter<ItemSelectAdapter.ViewHolder> {
        private List<ItemDisplay> items = new ArrayList<>();
        private List<ItemDisplay> fullList = new ArrayList<>();
        private final Set<String> selected = new HashSet<>();

        void setFullList(List<ItemDisplay> list) {
            fullList = list;
        }

        void setItems(List<ItemDisplay> list) {
            items = list;
            notifyDataSetChanged();
        }

        void filter(String query) {
            if (query == null || query.isEmpty()) {
                setItems(new ArrayList<>(fullList));
            } else {
                List<ItemDisplay> filtered = new ArrayList<>();
                String lowerQuery = query.toLowerCase();
                for (ItemDisplay item : fullList) {
                    if (item.name.toLowerCase().contains(lowerQuery)) {
                        filtered.add(item);
                    }
                }
                setItems(filtered);
            }
        }

        List<String> getSelectedKeys() {
            return new ArrayList<>(selected);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCheckboxBinding binding = ItemCheckboxBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ItemDisplay item = items.get(position);
            holder.binding.checkbox.setText(item.name);
            holder.binding.checkbox.setOnCheckedChangeListener(null);
            holder.binding.checkbox.setChecked(selected.contains(item.key));
            holder.binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selected.add(item.key);
                } else {
                    selected.remove(item.key);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemCheckboxBinding binding;
            ViewHolder(ItemCheckboxBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    static class ItemSelectionAdapter extends RecyclerView.Adapter<ItemSelectionAdapter.ViewHolder> {
        private List<String> items;
        private final OnRemoveListener listener;

        ItemSelectionAdapter(List<String> items, OnRemoveListener listener) {
            this.items = items;
            this.listener = listener;
        }

        void updateItems(List<String> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSelectedItemBinding binding = ItemSelectedItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.binding.tvName.setText(items.get(position));
            holder.binding.btnRemove.setOnClickListener(v -> listener.onRemove(position));
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        interface OnRemoveListener {
            void onRemove(int position);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemSelectedItemBinding binding;
            ViewHolder(ItemSelectedItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
