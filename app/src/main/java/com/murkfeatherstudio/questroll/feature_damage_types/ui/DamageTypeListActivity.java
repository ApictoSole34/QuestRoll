package com.murkfeatherstudio.questroll.feature_damage_types.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityDamageTypeListBinding;
import com.murkfeatherstudio.questroll.feature_damage_types.adapter.DamageTypeAdapter;
import com.murkfeatherstudio.questroll.feature_damage_types.view_model.DamageTypeListViewModel;

import java.util.ArrayList;
import java.util.List;

public class DamageTypeListActivity extends BaseActivity {
    private DamageTypeAdapter adapter;
    private DamageTypeListViewModel viewModel;
    private ActivityDamageTypeListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDamageTypeListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase db = Open5eDatabase.getInstance(this);
        UserContentDatabase userDb = UserContentDatabase.getInstance(this);
        viewModel = new ViewModelProvider(this,
                new DamageTypeListViewModel.Factory(db.damageTypeDao(), userDb.customDamageTypeDao()))
                .get(DamageTypeListViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();

        binding.fabCreate.setOnClickListener(v -> startActivity(new Intent(this, CustomDamageTypeCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new DamageTypeAdapter(type -> {
            if (type.isCustom) {
                Intent i = new Intent(this, CustomDamageTypeDetailActivity.class);
                i.putExtra("CUSTOM_DAMAGE_TYPE_ID", type.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, DamageTypeDetailActivity.class);
                i.putExtra("DAMAGE_TYPE_KEY", type.key);
                startActivity(i);
            }
        });
        binding.recyclerDamageTypes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDamageTypes.setAdapter(adapter);
        binding.recyclerDamageTypes.setSaveEnabled(false);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    private void setupObservers() {
        viewModel.getDamageTypes().observe(this, list -> {
            adapter.submitList(list, () -> binding.recyclerDamageTypes.post(() ->
                    binding.recyclerDamageTypes.scrollToPosition(0)));
            binding.tvCount.setText(list.size() + " damage types");
        });
        viewModel.getFilter().observe(this, f -> {
            if (f.isEmpty()) binding.tvActiveFilters.setVisibility(View.GONE);
            else {
                binding.tvActiveFilters.setVisibility(View.VISIBLE);
                List<String> parts = new ArrayList<>();
                if (!f.query.isEmpty()) parts.add("\"" + f.query + "\"");
                if (f.customOnly) parts.add("Custom");
                binding.tvActiveFilters.setText("Filters: " + String.join(", ", parts));
            }
        });
    }
}
