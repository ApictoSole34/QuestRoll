package com.fizzycoyote.qusetroll.feature_spell.spell_school.ui;

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
import com.fizzycoyote.qusetroll.feature_spell.spell_school.adapter.SpellSchoolAdapter;
import com.fizzycoyote.qusetroll.feature_spell.spell_school.view_model.SpellSchoolListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SpellSchoolListActivity extends AppCompatActivity {
    private SpellSchoolListViewModel viewModel;
    private SpellSchoolAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_school_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new SpellSchoolListViewModel.Factory(open5eDb.spellSchoolDao(), customDb.customSpellSchoolDao()))
                .get(SpellSchoolListViewModel.class);

        setupRecyclerView();
        setupSearch();

        FloatingActionButton fab = findViewById(R.id.fabCreate);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CustomSpellSchoolCreateActivity.class)));

        viewModel.getSpellSchools().observe(this, list -> {
            adapter.submitList(list);
            ((TextView) findViewById(R.id.tv_count)).setText(list.size() + " schools");
        });
    }

    private void setupRecyclerView() {
        adapter = new SpellSchoolAdapter(school -> {
            if (school.isCustom) {
                Intent i = new Intent(this, CustomSpellSchoolDetailActivity.class);
                i.putExtra("CUSTOM_SPELL_SCHOOL_ID", school.customId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, SpellSchoolDetailActivity.class);
                i.putExtra("SCHOOL_SLUG", school.key);
                startActivity(i);
            }
        });
        rv = findViewById(R.id.recycler_spell_schools);
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
}