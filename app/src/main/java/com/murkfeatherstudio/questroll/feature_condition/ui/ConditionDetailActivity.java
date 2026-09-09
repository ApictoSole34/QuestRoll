package com.murkfeatherstudio.questroll.feature_condition.ui;

import android.os.Bundle;
import android.view.View;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.condition.ConditionEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityConditionDetailBinding;

import io.noties.markwon.Markwon;

public class ConditionDetailActivity extends BaseActivity {
    private Markwon markwon;
    private ActivityConditionDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConditionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        binding.tvName.setText(c.name);
        markwon.setMarkdown(binding.tvDesc, c.description != null ? c.description : "");

        String sourceText = "Source: " + (c.documentName != null ? c.documentName : "");
        binding.tvSource.setText(sourceText);
        binding.tvSource.setVisibility(View.VISIBLE);
        binding.tvSource.setClickable(true);
        binding.tvSource.setFocusable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        binding.tvSource.setOnClickListener(v -> {
            if (c.documentKey != null && !c.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(c.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });

        binding.btnManage.setVisibility(View.GONE);
    }
}