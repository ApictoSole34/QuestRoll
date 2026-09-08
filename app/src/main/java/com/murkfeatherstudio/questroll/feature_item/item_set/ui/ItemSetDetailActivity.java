package com.murkfeatherstudio.questroll.feature_item.item_set.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.item_set.ItemSetEntity;
import com.murkfeatherstudio.questroll.feature_item.ui.ItemDetailActivity;
import io.noties.markwon.Markwon;

public class ItemSetDetailActivity extends BaseActivity {
    private Markwon markwon;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_set_detail);
        markwon = Markwon.create(this);
        String key = getIntent().getStringExtra("ITEM_SET_KEY");
        if (key == null) { finish(); return; }
        Open5eDatabase.getInstance(this).itemSetDao().getByKey(key).observe(this, set -> {
            if (set != null) populateUI(set);
        });
    }
    private void populateUI(ItemSetEntity set) {
        ((TextView) findViewById(R.id.tv_name)).setText(set.name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        markwon.setMarkdown(tvDesc, set.desc != null ? set.desc : "");
        TextView tvSource = findViewById(R.id.tv_source);
        String sourceText = "Source: " + (set.documentUrl != null ? set.documentUrl : "Unknown");
        tvSource.setText(sourceText);
        tvSource.setClickable(true);
        tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        tvSource.setOnClickListener(v -> {
            String key = extractKeyFromUrl(set.documentUrl);
            if (key != null) {
                DocumentDetailDialogFragment.newInstance(key).show(getSupportFragmentManager(), "doc");
            }
        });
        LinearLayout itemsContainer = findViewById(R.id.items_container);
        if (set.itemKeys != null) {
            for (String itemKey : set.itemKeys) {
                TextView tvItem = new TextView(this);
                tvItem.setText("• " + itemKey);
                tvItem.setPadding(0, dp(4), 0, dp(4));
                tvItem.setClickable(true);
                tvItem.setOnClickListener(v -> {
                    Intent i = new Intent(this, ItemDetailActivity.class);
                    i.putExtra("ITEM_KEY", itemKey);
                    startActivity(i);
                });
                itemsContainer.addView(tvItem);
            }
        }
        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }

    private String extractKeyFromUrl(String url) {
        if (url == null) return null;
        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = u.split("/");
        if (parts.length > 0) return parts[parts.length - 1];
        return null;
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}