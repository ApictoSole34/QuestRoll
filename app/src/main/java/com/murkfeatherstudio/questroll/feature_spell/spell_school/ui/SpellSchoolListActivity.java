package com.murkfeatherstudio.questroll.feature_spell.spell_school.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivitySpellSchoolListBinding;
import com.murkfeatherstudio.questroll.feature_spell.spell_school.adapter.SpellSchoolAdapter;
import com.murkfeatherstudio.questroll.feature_spell.spell_school.view_model.SpellSchoolListViewModel;

public class SpellSchoolListActivity extends BaseActivity {
    private SpellSchoolListViewModel viewModel;
    private SpellSchoolAdapter adapter;
    private ActivitySpellSchoolListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpellSchoolListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        viewModel = new ViewModelProvider(this,
                new SpellSchoolListViewModel.Factory(open5eDb.spellSchoolDao(), customDb.customSpellSchoolDao()))
                .get(SpellSchoolListViewModel.class);

        setupRecyclerView();
        setupSearch();

        binding.fabCreate.setOnClickListener(v -> startActivity(new Intent(this, CustomSpellSchoolCreateActivity.class)));

        viewModel.getSpellSchools().observe(this, list -> {
            adapter.submitList(list);
            binding.tvCount.setText(list.size() + " schools");
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
                i.putExtra("SCHOOL_KEY", school.key);
                startActivity(i);
            }
        });
        binding.recyclerSpellSchools.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSpellSchools.setAdapter(adapter);
        binding.recyclerSpellSchools.setSaveEnabled(false);
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
}
