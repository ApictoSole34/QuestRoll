package com.murkfeatherstudio.questroll.feature_item.item_category.ui;

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
import com.murkfeatherstudio.questroll.feature_item.item_category.adapter.ItemCategoryAdapter;
import com.murkfeatherstudio.questroll.feature_item.item_category.view_model.ItemCategoryListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ItemCategoryListActivity extends BaseActivity {

    private ItemCategoryListViewModel viewModel;
    private ItemCategoryAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_category_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new ItemCategoryListViewModel.Factory(
                        open5eDb.itemCategoryDao(),
                        customDb.customItemCategoryDao()
                )).get(ItemCategoryListViewModel.class);

        setupRecyclerView();
        setupSearch();

        FloatingActionButton fab = findViewById(R.id.fabCreate);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CustomItemCategoryCreateActivity.class)));

        viewModel.getCategories().observe(this, list -> {
            adapter.submitList(list);
            ((TextView) findViewById(R.id.tv_count)).setText(list.size() + " categories");
        });
    }

    private void setupRecyclerView() {
        adapter = new ItemCategoryAdapter(category -> {
            if (category.isCustom) {
                Intent i = new Intent(this, CustomItemCategoryDetailActivity.class);
                i.putExtra("CUSTOM_CATEGORY_ID", category.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, ItemCategoryDetailActivity.class);
                i.putExtra("CATEGORY_KEY", category.key);
                startActivity(i);
            }
        });
        rv = findViewById(R.id.recycler_categories);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        rv.setSaveEnabled(false);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override
            public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }
}
