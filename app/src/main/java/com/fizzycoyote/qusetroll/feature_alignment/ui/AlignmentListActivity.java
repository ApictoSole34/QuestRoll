package com.fizzycoyote.qusetroll.feature_alignment.ui;

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
import com.fizzycoyote.qusetroll.feature_alignment.adapter.AlignmentAdapter;
import com.fizzycoyote.qusetroll.feature_alignment.view_model.AlignmentListViewModel;

public class AlignmentListActivity extends BaseActivity {

    private AlignmentListViewModel viewModel;
    private AlignmentAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alignment_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new AlignmentListViewModel.Factory(
                        open5eDb.alignmentDao(),
                        customDb.customAlignmentDao()
                )).get(AlignmentListViewModel.class);

        setupRecyclerView();
        setupSearch();

        findViewById(R.id.fabCreate).setOnClickListener(v ->
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
        rv = findViewById(R.id.recycler_alignments);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        rv.setSaveEnabled(false);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
            ((TextView) findViewById(R.id.tv_count)).setText(list.size() + " alignments");
        });
    }
}