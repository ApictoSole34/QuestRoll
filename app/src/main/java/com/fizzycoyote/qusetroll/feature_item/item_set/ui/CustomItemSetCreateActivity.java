package com.fizzycoyote.qusetroll.feature_item.item_set.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;
import com.fizzycoyote.qusetroll.feature_item.item_set.view_model.CustomItemSetCreateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;


public class CustomItemSetCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_item_set_id";

    private CustomItemSetCreateViewModel viewModel;
    private TextInputEditText etName, etDesc;
    private RecyclerView rvItems;
    private ItemSelectionAdapter itemAdapter;
    private long editId = -1;

    private final List<String> selectedItemKeys = new ArrayList<>();
    private List<ItemDisplay> allAvailableItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_item_set_create);

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
        ((MaterialButton) findViewById(R.id.btnSave)).setText(editId == -1 ? "Save" : "Update");
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnSelectItems).setOnClickListener(v -> showItemSelectionDialog());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        rvItems = findViewById(R.id.rvSelectedItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemSelectionAdapter(new ArrayList<>(), this::removeItem);
        rvItems.setAdapter(itemAdapter);
    }

    private void setupObservers() {
        viewModel.getEditData().observe(this, set -> {
            if (set == null) return;
            etName.setText(set.name);
            etDesc.setText(set.desc);
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
        new Thread(() -> {
            List<String> displayNames = new ArrayList<>();
            Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
            UserContentDatabase userDb = UserContentDatabase.getInstance(this);

            for (String key : selectedItemKeys) {
                String name = null;
                if (key.startsWith("custom_")) {
                    long id = Long.parseLong(key.substring(7));
                    CustomItemEntity custom = userDb.customItemDao().getByIdSync(id);
                    if (custom != null) name = custom.name;
                } else {
                    ItemEntity item = open5eDb.itemDao().getByKeySync(key);
                    if (item != null) name = item.name;
                }
                if (name == null) name = key;
                displayNames.add(name);
            }
            runOnUiThread(() -> itemAdapter.updateItems(displayNames));
        }).start();
    }

    private void removeItem(int position) {
        if (position >= 0 && position < selectedItemKeys.size()) {
            selectedItemKeys.remove(position);
            updateSelectedItemsAdapter();
        }
    }

    private void showItemSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_items, null);

        android.widget.SearchView searchView = dialogView.findViewById(R.id.search_view);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemSelectAdapter selectAdapter = new ItemSelectAdapter();
        recyclerView.setAdapter(selectAdapter);

        new Thread(() -> {
            List<ItemEntity> apiItems = Open5eDatabase.getInstance(this).itemDao().getAllSync();
            List<CustomItemEntity> customItems = UserContentDatabase.getInstance(this).customItemDao().getAllSync();
            List<ItemDisplay> allItems = new ArrayList<>();
            for (ItemEntity i : apiItems) allItems.add(new ItemDisplay(i.key, i.name, false));
            for (CustomItemEntity c : customItems) allItems.add(new ItemDisplay("custom_" + c.id, c.name, true));
            allAvailableItems = allItems;
            runOnUiThread(() -> {
                selectAdapter.setFullList(allItems);
                selectAdapter.setItems(allItems);
            });
        }).start();

        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
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

        builder.setView(dialogView)
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
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        CustomItemSetEntity entity = new CustomItemSetEntity();
        entity.name = name;
        entity.desc = etDesc.getText().toString().trim();
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkbox, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ItemDisplay item = items.get(position);
            holder.checkBox.setText(item.name);
            holder.checkBox.setChecked(selected.contains(item.key));
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
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
            CheckBox checkBox;
            ViewHolder(View v) {
                super(v);
                checkBox = v.findViewById(R.id.checkbox);
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvName.setText(items.get(position));
            holder.btnRemove.setOnClickListener(v -> listener.onRemove(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        interface OnRemoveListener {
            void onRemove(int position);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            Button btnRemove;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_name);
                btnRemove = v.findViewById(R.id.btn_remove);
            }
        }
    }
}