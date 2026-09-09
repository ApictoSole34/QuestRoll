package com.murkfeatherstudio.questroll.feature_item.item_category.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityItemCategoryListBinding;
import com.murkfeatherstudio.questroll.feature_item.item_category.adapter.ItemCategoryAdapter;
import com.murkfeatherstudio.questroll.feature_item.item_category.view_model.ItemCategoryListViewModel;

public class ItemCategoryListActivity extends BaseActivity {

    private ItemCategoryListViewModel viewModel;
    private ItemCategoryAdapter adapter;
    private ActivityItemCategoryListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemCategoryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Item Categories");

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new ItemCategoryListViewModel.Factory(
                        open5eDb.itemCategoryDao(),
                        customDb.customItemCategoryDao()
                )).get(ItemCategoryListViewModel.class);

        setupRecyclerView();
        setupSearch();

        binding.fabCreate.setOnClickListener(v -> startActivity(new Intent(this, CustomItemCategoryCreateActivity.class)));

        viewModel.getCategories().observe(this, list -> {
            adapter.submitList(list);
            binding.tvCount.setText(list.size() + " categories");
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
        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCategories.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override
            public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }
}
