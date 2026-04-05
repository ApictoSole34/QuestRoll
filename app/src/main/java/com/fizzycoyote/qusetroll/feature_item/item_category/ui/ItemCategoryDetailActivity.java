package com.fizzycoyote.qusetroll.feature_item.item_category.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.item_category.ItemCategoryEntity;

public class ItemCategoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_category_detail);
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
        ((TextView) findViewById(R.id.tv_name)).setText(cat.name);
        ((TextView) findViewById(R.id.tv_desc)).setText("");

        TextView tvSource = findViewById(R.id.tv_source);
        String sourceText = "Source: " + (cat.documentName != null ? cat.documentName : "Unknown");
        tvSource.setText(sourceText);
        tvSource.setVisibility(View.VISIBLE);
        tvSource.setClickable(true);
        tvSource.setFocusable(true);
        tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        tvSource.setOnClickListener(v -> {
            if (cat.documentKey != null && !cat.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(cat.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            } else {
                Toast.makeText(this, "Document key not available", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}