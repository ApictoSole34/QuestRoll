package com.murkfeatherstudio.questroll.feature_environment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityEnvironmentListBinding;
import com.murkfeatherstudio.questroll.feature_environment.adapter.EnvironmentAdapter;
import com.murkfeatherstudio.questroll.feature_environment.view_model.EnvironmentListViewModel;

public class EnvironmentListActivity extends BaseActivity {
    private EnvironmentListViewModel viewModel;
    private EnvironmentAdapter adapter;
    private ActivityEnvironmentListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnvironmentListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new EnvironmentListViewModel.Factory(open5eDb.environmentDao(), customDb.customEnvironmentDao()))
                .get(EnvironmentListViewModel.class);

        setupRecyclerView();
        setupSearch();
        binding.fabCreate.setOnClickListener(v -> startActivity(new Intent(this, CustomEnvironmentCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new EnvironmentAdapter(env -> {
            if (env.isCustom) {
                Intent i = new Intent(this, CustomEnvironmentDetailActivity.class);
                i.putExtra("CUSTOM_ENVIRONMENT_ID", env.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, EnvironmentDetailActivity.class);
                i.putExtra("ENVIRONMENT_KEY", env.key);
                startActivity(i);
            }
        });
        binding.recyclerEnvironments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerEnvironments.setAdapter(adapter);
        binding.recyclerEnvironments.setSaveEnabled(false);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        viewModel.getEnvironments().observe(this, list -> {
            adapter.submitList(list);
            binding.tvCount.setText(list.size() + " environments");
        });
    }
}
