package com.murkfeatherstudio.questroll.feature_item.item_set.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityItemSetListBinding;
import com.murkfeatherstudio.questroll.feature_item.item_set.adapter.ItemSetAdapter;
import com.murkfeatherstudio.questroll.feature_item.item_set.view_model.ItemSetListViewModel;

public class ItemSetListActivity extends BaseActivity {
    private ItemSetListViewModel viewModel;
    private ItemSetAdapter adapter;
    private ActivityItemSetListBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemSetListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);
        viewModel = new ViewModelProvider(this,
                new ItemSetListViewModel.Factory(open5eDb.itemSetDao(), customDb.customItemSetDao()))
                .get(ItemSetListViewModel.class);
        setupRecyclerView();
        setupSearch();
        viewModel.getItemSets().observe(this, list -> {
            adapter.submitList(list);
            binding.tvCount.setText(list.size() + " item sets");
        });
        binding.fabCreate.setOnClickListener(v ->
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
        binding.recyclerItemSets.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerItemSets.setAdapter(adapter);
        binding.recyclerItemSets.setSaveEnabled(false);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }
}