package com.fizzycoyote.qusetroll.feature_service.ui;

import static androidx.core.content.ContextCompat.startActivity;

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
import com.fizzycoyote.qusetroll.feature_service.adapter.ServiceAdapter;
import com.fizzycoyote.qusetroll.feature_service.view_model.ServiceListViewModel;

public class ServiceListActivity extends AppCompatActivity {

    private ServiceListViewModel viewModel;
    private ServiceAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new ServiceListViewModel.Factory(
                        open5eDb.serviceDao(),
                        customDb.customServiceDao()
                )).get(ServiceListViewModel.class);

        setupRecyclerView();
        setupSearch();

        findViewById(R.id.fabCreate).setOnClickListener(v ->
                startActivity(new Intent(this, CustomServiceCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new ServiceAdapter(service -> {
            if (service.isCustom) {
                Intent i = new Intent(this, CustomServiceDetailActivity.class);
                i.putExtra("CUSTOM_SERVICE_ID", service.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, ServiceDetailActivity.class);
                i.putExtra("SERVICE_KEY", service.key);
                startActivity(i);
            }
        });
        rv = findViewById(R.id.recycler_services);
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
        viewModel.getServices().observe(this, list -> {
            adapter.submitList(list);
            ((TextView) findViewById(R.id.tv_count)).setText(list.size() + " services");
        });
    }
}