package com.murkfeatherstudio.questroll.feature_environment.ui;

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
import com.murkfeatherstudio.questroll.feature_environment.adapter.EnvironmentAdapter;
import com.murkfeatherstudio.questroll.feature_environment.view_model.EnvironmentListViewModel;

public class EnvironmentListActivity extends BaseActivity {
    private EnvironmentListViewModel viewModel;
    private EnvironmentAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new EnvironmentListViewModel.Factory(open5eDb.environmentDao(), customDb.customEnvironmentDao()))
                .get(EnvironmentListViewModel.class);

        setupRecyclerView();
        setupSearch();
        findViewById(R.id.fabCreate).setOnClickListener(v -> startActivity(new Intent(this, CustomEnvironmentCreateActivity.class)));
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
        rv = findViewById(R.id.recycler_environments);
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

    @Override protected void onResume() {
        super.onResume();
        viewModel.getEnvironments().observe(this, list -> {
            adapter.submitList(list);
            ((TextView) findViewById(R.id.tv_count)).setText(list.size() + " environments");
        });
    }
}
