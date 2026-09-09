package com.murkfeatherstudio.questroll.feature_ability.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityAbilityListBinding;
import com.murkfeatherstudio.questroll.feature_ability.adapter.AbilityAdapter;
import com.murkfeatherstudio.questroll.feature_ability.view_model.AbilityListViewModel;

import java.util.concurrent.Executor;

public class AbilityListActivity extends BaseActivity {

    private AbilityListViewModel viewModel;
    private AbilityAdapter adapter;
    private ActivityAbilityListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAbilityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.recyclerAbilities.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerAbilities.setAdapter(adapter);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.setQuery(q); return true; }
            @Override public boolean onQueryTextChange(String q) { viewModel.setQuery(q); return true; }
        });

        viewModel.abilities.observe(this, list -> {
            adapter.submitList(list, () -> binding.recyclerAbilities.scrollToPosition(0));
            binding.tvAbilityCount.setText(list.size() + " abilities");
        });

        binding.fabCreateAbility.setOnClickListener(v ->
                startActivity(new Intent(this, CustomAbilityCreateActivity.class)));
    }
}
