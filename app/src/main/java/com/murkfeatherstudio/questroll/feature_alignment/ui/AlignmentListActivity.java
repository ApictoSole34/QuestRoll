package com.murkfeatherstudio.questroll.feature_alignment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityAlignmentListBinding;
import com.murkfeatherstudio.questroll.feature_alignment.adapter.AlignmentAdapter;
import com.murkfeatherstudio.questroll.feature_alignment.view_model.AlignmentListViewModel;

public class AlignmentListActivity extends BaseActivity {

    private AlignmentListViewModel viewModel;
    private AlignmentAdapter adapter;
    private ActivityAlignmentListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlignmentListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new AlignmentListViewModel.Factory(
                        open5eDb.alignmentDao(),
                        customDb.customAlignmentDao()
                )).get(AlignmentListViewModel.class);

        setupRecyclerView();
        setupSearch();

        binding.fabCreate.setOnClickListener(v ->
                startActivity(new Intent(this, CustomAlignmentCreateActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new AlignmentAdapter(alignment -> {
            if (alignment.isCustom) {
                Intent i = new Intent(this, CustomAlignmentDetailActivity.class);
                i.putExtra("CUSTOM_ALIGNMENT_ID", alignment.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, AlignmentDetailActivity.class);
                i.putExtra("ALIGNMENT_KEY", alignment.key);
                startActivity(i);
            }
        });
        binding.recyclerAlignments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerAlignments.setAdapter(adapter);
        binding.recyclerAlignments.setSaveEnabled(false);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        viewModel.getAlignments().observe(this, list -> {
            adapter.submitList(list);
            binding.tvCount.setText(list.size() + " alignments");
        });
    }
}