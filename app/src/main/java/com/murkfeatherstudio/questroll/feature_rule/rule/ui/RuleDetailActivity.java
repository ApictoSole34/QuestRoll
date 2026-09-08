package com.murkfeatherstudio.questroll.feature_rule.rule.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.rule.RuleEntity;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;

public class RuleDetailActivity extends BaseActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_detail);

        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .build();

        String ruleKey = getIntent().getStringExtra("RULE_KEY");  // zmiana: RULE_KEY

        if (ruleKey == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this)
                .ruleDao()
                .getByKey(ruleKey)           // zmiana: getByKey
                .observe(this, rule -> {
                    if (rule != null) {
                        populateUI(rule);
                    }
                });
    }

    private String extractKeyFromUrl(String url) {
        if (url == null) {
            return null;
        }
        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = u.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    private void populateUI(RuleEntity rule) {
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        TextView tvSource = findViewById(R.id.tv_source);

        tvName.setText(rule.name);
        markwon.setMarkdown(tvDesc, rule.desc);

        String sourceText = "Source: " + (rule.documentUrl != null ? rule.documentUrl : "");
        tvSource.setText(sourceText);
        tvSource.setVisibility(View.VISIBLE);
        tvSource.setClickable(true);
        tvSource.setFocusable(true);
        tvSource.setBackgroundColor(Color.TRANSPARENT);

        tvSource.setOnClickListener(v -> {
            String docKey = extractKeyFromUrl(rule.documentUrl);
            if (docKey != null && !docKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(docKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });
    }
}