package com.fizzycoyote.qusetroll.feature_item.item_rarity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.feature_item.item_rarity.adapter.ItemRarityAdapter;
import com.fizzycoyote.qusetroll.feature_item.item_rarity.view_model.ItemRarityListViewModel;

public class ItemRarityListActivity extends AppCompatActivity {

    private ItemRarityListViewModel viewModel;
    private ItemRarityAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_rarity_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new ItemRarityListViewModel.Factory(
                        open5eDb.itemRarityDao(),
                        customDb.customItemRarityDao()
                )).get(ItemRarityListViewModel.class);

        setupRecyclerView();
        setupSearch();

        findViewById(R.id.fabCreate).setOnClickListener(v ->
                startActivity(new Intent(this, CustomItemRarityCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new ItemRarityAdapter(rarity -> {
            if (rarity.isCustom) {
                Intent i = new Intent(this, CustomItemRarityDetailActivity.class);
                i.putExtra("CUSTOM_ITEM_RARITY_ID", rarity.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, ItemRarityDetailActivity.class);
                i.putExtra("ITEM_RARITY_KEY", rarity.key);
                startActivity(i);
            }
        });
        rv = findViewById(R.id.recycler_rarities);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        rv.setSaveEnabled(false);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                viewModel.setQuery(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String q) {
                viewModel.setQuery(q);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getRarities().observe(this, list -> {
            adapter.submitList(list);
            ((TextView) findViewById(R.id.tv_count)).setText(list.size() + " rarities");
        });
    }
}