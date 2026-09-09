package com.murkfeatherstudio.questroll.feature_item.weapon_property.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityWeaponPropertyListBinding;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.adapter.WeaponPropertyAdapter;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.view_model.WeaponPropertyListViewModel;

public class WeaponPropertyListActivity extends BaseActivity {

    private WeaponPropertyListViewModel viewModel;
    private WeaponPropertyAdapter adapter;
    private ActivityWeaponPropertyListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeaponPropertyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new WeaponPropertyListViewModel.Factory(
                        open5eDb.weaponPropertyDao(),
                        customDb.customWeaponPropertyDao()
                )).get(WeaponPropertyListViewModel.class);

        setupRecyclerView();
        setupSearch();

        binding.fabCreate.setOnClickListener(v ->
                startActivity(new Intent(this, CustomWeaponPropertyCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new WeaponPropertyAdapter(property -> {
            if (property.isCustom) {
                Intent i = new Intent(this, CustomWeaponPropertyDetailActivity.class);
                i.putExtra("CUSTOM_WEAPON_PROPERTY_ID", property.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, WeaponPropertyDetailActivity.class);
                i.putExtra("PROPERTY_KEY", property.key);
                startActivity(i);
            }
        });
        binding.recyclerProperties.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerProperties.setAdapter(adapter);
        binding.recyclerProperties.setSaveEnabled(false);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getProperties().observe(this, list -> {
            adapter.submitList(list);
            binding.tvCount.setText(list.size() + " properties");
        });
    }
}