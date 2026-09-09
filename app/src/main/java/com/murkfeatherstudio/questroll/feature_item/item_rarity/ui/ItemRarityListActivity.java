package com.murkfeatherstudio.questroll.feature_item.item_rarity.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityItemRarityListBinding;
import com.murkfeatherstudio.questroll.feature_item.item_rarity.adapter.ItemRarityAdapter;
import com.murkfeatherstudio.questroll.feature_item.item_rarity.view_model.ItemRarityListViewModel;

public class ItemRarityListActivity extends BaseActivity {

    private ItemRarityListViewModel viewModel;
    private ItemRarityAdapter adapter;
    private ActivityItemRarityListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemRarityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Rarities");

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new ItemRarityListViewModel.Factory(
                        open5eDb.itemRarityDao(),
                        customDb.customItemRarityDao()
                )).get(ItemRarityListViewModel.class);

        setupRecyclerView();
        setupSearch();

        binding.fabCreate.setOnClickListener(v ->
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
        binding.recyclerRarities.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerRarities.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
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
            binding.tvCount.setText(list.size() + " rarities");
        });
    }
}
