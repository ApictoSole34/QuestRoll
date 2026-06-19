package com.fizzycoyote.qusetroll.feature_item.item_set.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.feature_item.item_set.adapter.ItemSetAdapter;
import com.fizzycoyote.qusetroll.feature_item.item_set.view_model.ItemSetListViewModel;

public class ItemSetListActivity extends BaseActivity {
    private ItemSetListViewModel viewModel;
    private ItemSetAdapter adapter;
    private RecyclerView rv;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_set_list);
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);
        viewModel = new ViewModelProvider(this,
                new ItemSetListViewModel.Factory(open5eDb.itemSetDao(), customDb.customItemSetDao()))
                .get(ItemSetListViewModel.class);
        setupRecyclerView();
        setupSearch();
        viewModel.getItemSets().observe(this, list -> {
            adapter.submitList(list);
            ((TextView) findViewById(R.id.tv_count)).setText(list.size() + " item sets");
        });
        findViewById(R.id.fabCreate).setOnClickListener(v ->
                startActivity(new Intent(this, CustomItemSetCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new ItemSetAdapter(itemSet -> {
            if (itemSet.isCustom) {
                Intent i = new Intent(this, CustomItemSetDetailActivity.class);
                i.putExtra("CUSTOM_ITEM_SET_ID", itemSet.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, ItemSetDetailActivity.class);
                i.putExtra("ITEM_SET_KEY", itemSet.key);
                startActivity(i);
            }
        });
        rv = findViewById(R.id.recycler_item_sets);
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
}