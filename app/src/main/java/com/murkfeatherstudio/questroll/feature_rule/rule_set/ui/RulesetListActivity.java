package com.murkfeatherstudio.questroll.feature_rule.rule_set.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityRulesetListBinding;
import com.murkfeatherstudio.questroll.feature_rule.rule.ui.RuleListActivity;
import com.murkfeatherstudio.questroll.feature_rule.rule_set.adapter.RulesetAdapter;
import com.murkfeatherstudio.questroll.feature_rule.rule_set.view_model.RulesetListViewModel;
import com.google.android.material.chip.Chip;

import java.util.List;

public class RulesetListActivity extends BaseActivity {

    private RulesetListViewModel viewModel;
    private RulesetAdapter adapter;
    private ActivityRulesetListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRulesetListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Rules & Lore");

        viewModel = new ViewModelProvider(this,
                new RulesetListViewModel.Factory(Open5eDatabase.getInstance(this).rulesetDao()))
                .get(RulesetListViewModel.class);

        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new RulesetAdapter(ruleset -> {
            Intent i = new Intent(this, RuleListActivity.class);
            i.putExtra("RULESET_KEY", ruleset.key);
            startActivity(i);
        });
        binding.recyclerRulesets.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerRulesets.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getFilteredRulesets().observe(this, list -> {
            if (list != null) {
                adapter.submitList(list);
                binding.tvCount.setText(list.size() + " categories");
            }
        });

        viewModel.getSources().observe(this, sources -> {
            if (sources != null && !sources.isEmpty()) buildSourceChips(sources);
        });
    }

    /**
     * JAVADOC: chipGroupSources is a dynamic container. Chips are created programmatically 
     * based on the distinct rule sources found in the database. Since these chips are 
     * not defined in the static XML layout, they are added via addView() and are 
     * not accessible through View Binding.
     */
    private void buildSourceChips(List<String> sources) {
        binding.chipGroupSources.removeAllViews();
        Chip chipAll = new Chip(this);
        chipAll.setText("All");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipAll.setTag("");
        chipAll.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                for (int i = 1; i < binding.chipGroupSources.getChildCount(); i++) {
                    View child = binding.chipGroupSources.getChildAt(i);
                    if (child instanceof Chip) {
                        ((Chip) child).setChecked(false);
                    }
                }
                viewModel.setSelectedSource("");
            }
        });
        binding.chipGroupSources.addView(chipAll);

        for (String source : sources) {
            Chip chip = new Chip(this);
            chip.setText(source);
            chip.setTag(source);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    chipAll.setChecked(false);
                    viewModel.setSelectedSource((String) btn.getTag());
                }
            });
            binding.chipGroupSources.addView(chip);
        }
    }
}
