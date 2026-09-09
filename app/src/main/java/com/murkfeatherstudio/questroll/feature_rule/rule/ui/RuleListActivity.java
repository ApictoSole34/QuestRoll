package com.murkfeatherstudio.questroll.feature_rule.rule.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityRuleListBinding;
import com.murkfeatherstudio.questroll.feature_rule.rule.adapter.RuleAdapter;
import com.murkfeatherstudio.questroll.feature_rule.rule.view_model.RuleListViewModel;

import io.noties.markwon.Markwon;

public class RuleListActivity extends BaseActivity {
    private RuleListViewModel viewModel;
    private RuleAdapter adapter;
    private ActivityRuleListBinding binding;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRuleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String rulesetKey = getIntent().getStringExtra("RULESET_KEY");
        if (rulesetKey == null) { finish(); return; }

        markwon = Markwon.create(this);

        binding.recyclerRules.setNestedScrollingEnabled(false);
        binding.recyclerRules.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RuleAdapter(rule -> {
            Intent i = new Intent(this, RuleDetailActivity.class);
            i.putExtra("RULE_KEY", rule.key);
            startActivity(i);
        });
        binding.recyclerRules.setAdapter(adapter);

        viewModel = new ViewModelProvider(this,
                new RuleListViewModel.Factory(Open5eDatabase.getInstance(this).ruleDao(), rulesetKey))
                .get(RuleListViewModel.class);

        Open5eDatabase.getInstance(this).rulesetDao().getByKey(rulesetKey).observe(this, ruleset -> {
            if (ruleset != null) {
                binding.tvRulesetName.setText(ruleset.name);
                markwon.setMarkdown(binding.tvRulesetDesc, ruleset.desc != null ? ruleset.desc : "");
                String docName = ruleset.documentName != null ? ruleset.documentName : "Unknown source";
                binding.tvDocument.setText("Source: " + docName);
                setTitle(ruleset.name);
            }
        });

        viewModel.getRules().observe(this, rules -> {
            adapter.submitList(rules);
        });
    }
}
