package com.fizzycoyote.qusetroll.feature_environment.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentEntity;

public class EnvironmentDetailActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_detail);
        String key = getIntent().getStringExtra("ENVIRONMENT_KEY");
        if (key == null) { finish(); return; }
        Open5eDatabase.getInstance(this).environmentDao().getByKey(key).observe(this, env -> {
            if (env != null) populateUI(env);
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

    private void populateUI(EnvironmentEntity e) {
        ((TextView) findViewById(R.id.tv_name)).setText(e.name);
        String type = "";
        if (e.aquatic) type = "Aquatic";
        else if (e.planar) type = "Planar";
        else if (e.interior) type = "Interior";
        else type = "Land";
        ((TextView) findViewById(R.id.tv_type)).setText("Type: " + type);
        ((TextView) findViewById(R.id.tv_desc)).setText(e.desc != null ? e.desc : "No description.");
        TextView tvSource = findViewById(R.id.tv_source);
        String sourceDisplay = "Source: ";
        String docKey;
        if (e.document != null && !e.document.isEmpty()) {
            docKey = extractKeyFromUrl(e.document);
            sourceDisplay += formatDocumentName(docKey);
        } else {
            docKey = null;
            sourceDisplay += "Unknown";
        }
        tvSource.setText(sourceDisplay);
        tvSource.setClickable(true);
        tvSource.setFocusable(true);
        tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        tvSource.setOnClickListener(v -> {
            if (docKey != null) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(docKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });
        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}
