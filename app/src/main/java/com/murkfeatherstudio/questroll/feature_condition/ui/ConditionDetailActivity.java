package com.murkfeatherstudio.questroll.feature_condition.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.condition.ConditionEntity;

import io.noties.markwon.Markwon;

public class ConditionDetailActivity extends BaseActivity {
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition_detail);
        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("CONDITION_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).conditionDao().getByKey(key).observe(this, condition -> {
            if (condition != null) populateUI(condition);
        });
    }

    private void populateUI(ConditionEntity c) {
        ((TextView) findViewById(R.id.tv_name)).setText(c.name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        markwon.setMarkdown(tvDesc, c.description != null ? c.description : "");

        TextView tvSource = findViewById(R.id.tv_source);
        String sourceText = "Source: " + (c.documentName != null ? c.documentName : "");
        tvSource.setText(sourceText);
        tvSource.setVisibility(View.VISIBLE);
        tvSource.setClickable(true);
        tvSource.setFocusable(true);
        tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        tvSource.setOnClickListener(v -> {
            if (c.documentKey != null && !c.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(c.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });

        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}