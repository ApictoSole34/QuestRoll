package com.murkfeatherstudio.questroll.feature_item.item_set.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.item_set.ItemSetEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityItemSetDetailBinding;
import com.murkfeatherstudio.questroll.feature_item.ui.ItemDetailActivity;
import io.noties.markwon.Markwon;

public class ItemSetDetailActivity extends BaseActivity {
    private Markwon markwon;
    private ActivityItemSetDetailBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemSetDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        markwon = Markwon.create(this);
        String key = getIntent().getStringExtra("ITEM_SET_KEY");
        if (key == null) { finish(); return; }
        Open5eDatabase.getInstance(this).itemSetDao().getByKey(key).observe(this, set -> {
            if (set != null) populateUI(set);
        });
    }

    /**
     * NOTE: Part of the UI (item list) is built dynamically at runtime (number of fields depends on the set content),
     * there is no static XML layout for individual rows - in this place ViewBinding does not apply
     * for elements inside items_container.
     */
    private void populateUI(ItemSetEntity set) {
        binding.tvName.setText(set.name);
        markwon.setMarkdown(binding.tvDesc, set.desc != null ? set.desc : "");
        
        String sourceText = "Source: " + (set.documentUrl != null ? set.documentUrl : "Unknown");
        binding.tvSource.setText(sourceText);
        binding.tvSource.setClickable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        binding.tvSource.setOnClickListener(v -> {
            String key = extractKeyFromUrl(set.documentUrl);
            if (key != null) {
                DocumentDetailDialogFragment.newInstance(key).show(getSupportFragmentManager(), "doc");
            }
        });

        binding.itemsContainer.removeAllViews();
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
                binding.itemsContainer.addView(tvItem);
            }
        }
        binding.btnManage.setVisibility(View.GONE);
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
