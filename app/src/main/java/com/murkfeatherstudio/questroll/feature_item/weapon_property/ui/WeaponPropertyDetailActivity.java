package com.murkfeatherstudio.questroll.feature_item.weapon_property.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.weapon_property.WeaponPropertyDao;
import com.murkfeatherstudio.questroll.core.models.open5e.weapon_property.WeaponPropertyEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityWeaponPropertyDetailBinding;

public class WeaponPropertyDetailActivity extends BaseActivity {

    private ActivityWeaponPropertyDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeaponPropertyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.tvName.setText(p.name);
        binding.tvType.setText(p.type != null ? p.type : "Property");
        binding.tvDesc.setText(p.desc != null ? p.desc : "");

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
        binding.tvSource.setText(sourceDisplay);
        binding.tvSource.setVisibility(View.VISIBLE);
        binding.tvSource.setClickable(true);
        binding.tvSource.setFocusable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        binding.tvSource.setOnClickListener(v -> {
            if (docKey != null && !docKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(docKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            } else {
                Toast.makeText(this, "Document key not available", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnManage.setVisibility(View.GONE);
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