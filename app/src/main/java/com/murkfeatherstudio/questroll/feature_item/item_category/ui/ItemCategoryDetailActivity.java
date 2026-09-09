package com.murkfeatherstudio.questroll.feature_item.item_category.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.item_category.ItemCategoryEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityItemCategoryDetailBinding;

public class ItemCategoryDetailActivity extends BaseActivity {

    private ActivityItemCategoryDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemCategoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String key = getIntent().getStringExtra("CATEGORY_KEY");
        if (key == null) {
            finish();
            return;
        }
        Open5eDatabase.getInstance(this).itemCategoryDao().getByKey(key).observe(this, cat -> {
            if (cat != null) populateUI(cat);
        });
    }

    private void populateUI(ItemCategoryEntity cat) {
        binding.tvName.setText(cat.name);
        binding.tvDesc.setText("");

        String sourceText = "Source: " + (cat.documentName != null ? cat.documentName : "Unknown");
        binding.tvSource.setText(sourceText);
        binding.tvSource.setVisibility(View.VISIBLE);
        binding.tvSource.setClickable(true);
        binding.tvSource.setFocusable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        binding.tvSource.setOnClickListener(v -> {
            if (cat.documentKey != null && !cat.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(cat.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            } else {
                Toast.makeText(this, "Document key not available", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnManage.setVisibility(View.GONE);
    }
}