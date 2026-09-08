package com.murkfeatherstudio.questroll.feature_damage_types.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.feature_damage_types.adapter.DamageTypeAdapter;
import com.murkfeatherstudio.questroll.feature_damage_types.view_model.DamageTypeListViewModel;

import java.util.ArrayList;
import java.util.List;

public class DamageTypeListActivity extends BaseActivity {
    private DamageTypeAdapter adapter;
    private DamageTypeListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage_type_list);

        Open5eDatabase db = Open5eDatabase.getInstance(this);
        UserContentDatabase userDb = UserContentDatabase.getInstance(this);
        viewModel = new ViewModelProvider(this,
                new DamageTypeListViewModel.Factory(db.damageTypeDao(), userDb.customDamageTypeDao()))
                .get(DamageTypeListViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();

        findViewById(R.id.fabCreate).setOnClickListener(v -> startActivity(new Intent(this, CustomDamageTypeCreateActivity.class)));
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
        RecyclerView rv = findViewById(R.id.recycler_damage_types);
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

    private void setupObservers() {
        viewModel.getDamageTypes().observe(this, list -> {
            adapter.submitList(list, () -> findViewById(R.id.recycler_damage_types).post(() ->
                    ((RecyclerView) findViewById(R.id.recycler_damage_types)).scrollToPosition(0)));
            ((TextView) findViewById(R.id.tv_count)).setText(list.size() + " damage types");
        });
        viewModel.getFilter().observe(this, f -> {
            TextView tv = findViewById(R.id.tv_active_filters);
            if (f.isEmpty()) tv.setVisibility(View.GONE);
            else {
                tv.setVisibility(View.VISIBLE);
                List<String> parts = new ArrayList<>();
                if (!f.query.isEmpty()) parts.add("\"" + f.query + "\"");
                if (f.customOnly) parts.add("Custom");
                tv.setText("Filters: " + String.join(", ", parts));
            }
        });
    }
}