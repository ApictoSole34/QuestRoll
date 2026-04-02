package com.fizzycoyote.qusetroll.feature_rule.rule.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.rule.RuleEntity;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;

public class RuleDetailActivity extends AppCompatActivity {
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_detail);

        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .build();

        String ruleUrl = getIntent().getStringExtra("RULE_URL");
        if (ruleUrl == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).ruleDao().getByUrl(ruleUrl).observe(this, rule -> {
            if (rule != null) populateUI(rule);
        });
    }

    private void populateUI(RuleEntity rule) {
        ((TextView) findViewById(R.id.tv_name)).setText(rule.name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        markwon.setMarkdown(tvDesc, rule.desc);
        TextView tvSource = findViewById(R.id.tv_source);
        tvSource.setText("Source: " + rule.documentUrl);
    }
}