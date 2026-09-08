package com.murkfeatherstudio.questroll.feature_item.weapon_property.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.adapter.WeaponPropertyAdapter;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.view_model.WeaponPropertyListViewModel;

public class WeaponPropertyListActivity extends BaseActivity {

    private WeaponPropertyListViewModel viewModel;
    private WeaponPropertyAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weapon_property_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new WeaponPropertyListViewModel.Factory(
                        open5eDb.weaponPropertyDao(),
                        customDb.customWeaponPropertyDao()
                )).get(WeaponPropertyListViewModel.class);

        setupRecyclerView();
        setupSearch();

        findViewById(R.id.fabCreate).setOnClickListener(v ->
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
        rv = findViewById(R.id.recycler_properties);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        rv.setSaveEnabled(false);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getProperties().observe(this, list -> {
            adapter.submitList(list);
            ((TextView) findViewById(R.id.tv_count)).setText(list.size() + " properties");
        });
    }
}