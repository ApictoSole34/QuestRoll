package com.fizzycoyote.qusetroll.feature_rule.rule.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.feature_rule.rule.adapter.RuleAdapter;
import com.fizzycoyote.qusetroll.feature_rule.rule.view_model.RuleListViewModel;

import io.noties.markwon.Markwon;

public class RuleListActivity extends BaseActivity {
    private RuleListViewModel viewModel;
    private RuleAdapter adapter;
    private RecyclerView rv;
    private TextView tvRulesetName, tvRulesetDesc, tvDocument;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_list);

        String rulesetKey = getIntent().getStringExtra("RULESET_KEY");
        if (rulesetKey == null) { finish(); return; }

        markwon = Markwon.create(this);

        tvRulesetName = findViewById(R.id.tv_ruleset_name);
        tvRulesetDesc = findViewById(R.id.tv_ruleset_desc);
        tvDocument = findViewById(R.id.tv_document);
        rv = findViewById(R.id.recycler_rules);

        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RuleAdapter(rule -> {
            Intent i = new Intent(this, RuleDetailActivity.class);
            i.putExtra("RULE_KEY", rule.key);  // <-- zmiana: url -> key
            startActivity(i);
        });
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this,
                new RuleListViewModel.Factory(Open5eDatabase.getInstance(this).ruleDao(), rulesetKey))
                .get(RuleListViewModel.class);

        Open5eDatabase.getInstance(this).rulesetDao().getByKey(rulesetKey).observe(this, ruleset -> {
            if (ruleset != null) {
                tvRulesetName.setText(ruleset.name);
                markwon.setMarkdown(tvRulesetDesc, ruleset.desc != null ? ruleset.desc : "");
                String docName = ruleset.documentName != null ? ruleset.documentName : "Unknown source";
                tvDocument.setText("Source: " + docName);
                setTitle(ruleset.name);
            }
        });

        viewModel.getRules().observe(this, rules -> {
            adapter.submitList(rules);
        });
    }
}
