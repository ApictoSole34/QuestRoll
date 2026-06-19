package com.fizzycoyote.qusetroll.feature_ability.ui;

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
import com.fizzycoyote.qusetroll.feature_ability.adapter.AbilityAdapter;
import com.fizzycoyote.qusetroll.feature_ability.view_model.AbilityListViewModel;

import java.util.concurrent.Executor;

public class AbilityListActivity extends BaseActivity {

    private AbilityListViewModel viewModel;
    private AbilityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ability_list);

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase custDb  = UserContentDatabase.getInstance(this);
        Executor executor = open5eDb.getQueryExecutor();

        viewModel = new ViewModelProvider(this, new AbilityListViewModel.Factory(
                open5eDb.abilityDao(),
                custDb.customAbilityDao(),
                open5eDb.skillDao(),
                custDb.customSkillDao(),
                executor
        )).get(AbilityListViewModel.class);

        RecyclerView rv = findViewById(R.id.recycler_abilities);
        adapter = new AbilityAdapter(ability -> {
            Intent i;
            if (ability.isCustom) {
                i = new Intent(this, CustomAbilityDetailActivity.class);
                i.putExtra("CUSTOM_ABILITY_ID", ability.customId);
            } else {
                i = new Intent(this, AbilityDetailActivity.class);
                i.putExtra("ABILITY_KEY", ability.key);
            }
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        ((SearchView) findViewById(R.id.search_view))
                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
                    @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
                });

        viewModel.abilities.observe(this, list -> {
            adapter.submitList(list, () -> rv.scrollToPosition(0));
            ((TextView) findViewById(R.id.tv_ability_count))
                    .setText(list.size() + " abilities");
        });

        findViewById(R.id.fab_create_ability).setOnClickListener(v ->
                startActivity(new Intent(this, CustomAbilityCreateActivity.class)));
    }
}