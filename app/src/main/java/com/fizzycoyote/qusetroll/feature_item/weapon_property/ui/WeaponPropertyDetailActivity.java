package com.fizzycoyote.qusetroll.feature_item.weapon_property.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyDao;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyEntity;

public class WeaponPropertyDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weapon_property_detail);

        String key = getIntent().getStringExtra("PROPERTY_KEY");
        String name = getIntent().getStringExtra("PROPERTY_NAME");

        WeaponPropertyDao dao = Open5eDatabase.getInstance(this).weaponPropertyDao();

        if (key != null && !key.isEmpty()) {
            dao.getByKey(key).observe(this, property -> {
                if (property != null) populateUI(property);
                else if (name != null) fallbackToName(dao, name);
                else finish();
            });
        } else if (name != null && !name.isEmpty()) {
            fallbackToName(dao, name);
        } else {
            finish();
        }
    }

    private void fallbackToName(WeaponPropertyDao dao, String name) {
        dao.getByName(name).observe(this, property -> {
            if (property != null) populateUI(property);
            else {
                Toast.makeText(this, "Property not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateUI(WeaponPropertyEntity p) {
        ((TextView) findViewById(R.id.tv_name)).setText(p.name);
        ((TextView) findViewById(R.id.tv_type)).setText(p.type != null ? p.type : "Property");
        ((TextView) findViewById(R.id.tv_desc)).setText(p.desc != null ? p.desc : "");

        TextView tvSource = findViewById(R.id.tv_source);
        if (tvSource != null) {
            String sourceDisplay = "Source: ";
            String docKey;
            if (p.document != null && !p.document.isEmpty()) {
                docKey = extractKeyFromUrl(p.document);
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
                } else {
                    Toast.makeText(this, "Document key not available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }

    private String extractKeyFromUrl(String url) {
        if (url == null) return null;
        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = u.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }

    private String formatDocumentName(String key) {
        if (key == null) return "Unknown";
        switch (key) {
            case "srd-2014":
                return "SRD 5.1";
            case "srd-2024":
                return "SRD 5.2";
            case "a5e-ag":
                return "Adventurer's Guide";
            default:
                return key;
        }
    }
}