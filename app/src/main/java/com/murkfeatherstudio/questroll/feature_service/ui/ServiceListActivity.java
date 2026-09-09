package com.murkfeatherstudio.questroll.feature_service.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityServiceListBinding;
import com.murkfeatherstudio.questroll.feature_service.adapter.ServiceAdapter;
import com.murkfeatherstudio.questroll.feature_service.view_model.ServiceListViewModel;

public class ServiceListActivity extends BaseActivity {

    private ServiceListViewModel viewModel;
    private ServiceAdapter adapter;
    private ActivityServiceListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new ServiceListViewModel.Factory(
                        open5eDb.serviceDao(),
                        customDb.customServiceDao()
                )).get(ServiceListViewModel.class);

        setupRecyclerView();
        setupSearch();

        binding.fabCreate.setOnClickListener(v ->
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
        binding.recyclerServices.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerServices.setAdapter(adapter);
        binding.recyclerServices.setSaveEnabled(false);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getServices().observe(this, list -> {
            adapter.submitList(list);
            binding.tvCount.setText(list.size() + " services");
        });
    }
}