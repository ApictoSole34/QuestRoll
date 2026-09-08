package com.murkfeatherstudio.questroll.feature_damage_types.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.damage_type.DamageTypeEntity;

public class DamageTypeDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage_type_detail);

        String key = getIntent().getStringExtra("DAMAGE_TYPE_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).damageTypeDao()
                .getByKey(key).observe(this, type -> {
                    if (type != null) populateUI(type);
                });
    }

    private String extractKeyFromUrl(String url) {
        if (url == null) return null;
        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = u.split("/");
        if (parts.length > 0) return parts[parts.length - 1];
        return null;
    }
    private String formatDocumentName(String key) {
        if (key == null) return "Unknown";
        switch (key) {
            case "core": return "Core Rules";
            case "srd-2014": return "SRD 5.1";
            case "srd-2024": return "SRD 5.2";
            default: return key;
        }
    }

    private void populateUI(DamageTypeEntity type) {
        ((TextView) findViewById(R.id.tv_name)).setText(type.name);
        ((TextView) findViewById(R.id.tv_description)).setText(type.description != null ? type.description : "No description.");
        findViewById(R.id.btnManage).setVisibility(View.GONE);

        TextView tvSource = findViewById(R.id.tv_source);
        String sourceDisplay = "Source: ";
        String docKey;
        if (type.document != null && !type.document.isEmpty()) {
            docKey = extractKeyFromUrl(type.document);
            String displayName = formatDocumentName(docKey);
            sourceDisplay += displayName;
        } else {
            docKey = null;
            sourceDisplay += "Unknown";
        }
        tvSource.setText(sourceDisplay);
        tvSource.setVisibility(View.VISIBLE);
        tvSource.setClickable(true);
        tvSource.setFocusable(true);
        tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        tvSource.setOnClickListener(v -> {
            if (docKey != null && !docKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(docKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });
    }
}