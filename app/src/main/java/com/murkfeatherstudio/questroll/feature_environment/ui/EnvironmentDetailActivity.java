package com.murkfeatherstudio.questroll.feature_environment.ui;

import android.os.Bundle;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.environment.EnvironmentEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityEnvironmentDetailBinding;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class EnvironmentDetailActivity extends BaseActivity {

    private Markwon markwon;
    private ActivityEnvironmentDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnvironmentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(HtmlPlugin.create())
                .build();

        String key = getIntent().getStringExtra("ENVIRONMENT_KEY");
        if (key == null) {
            finish();
            return;
        }
        Open5eDatabase.getInstance(this).environmentDao()
                .getByKey(key)
                .observe(this, env -> {
                    if (env != null) populateUI(env);
                });
    }

    private void populateUI(EnvironmentEntity e) {
        binding.tvName.setText(e.name);
        binding.tvName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
        binding.tvName.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        String type = "";
        if (e.aquatic) type = "Aquatic";
        else if (e.planar) type = "Planar";
        else if (e.interior) type = "Interior";
        else type = "Land";

        binding.tvType.setText("Type: " + type);
        binding.tvType.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvType.setTextColor(getResources().getColor(R.color.threads_gold, null));

        String descText = e.desc != null ? e.desc : "No description.";
        markwon.setMarkdown(binding.tvDesc, descText);
        binding.tvDesc.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvDesc.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        final String docKey = (e.document != null && !e.document.isEmpty())
                ? extractKeyFromUrl(e.document)
                : null;

        String sourceDisplay = "Source: " + (docKey != null ? formatDocumentName(docKey) : "Unknown");
        binding.tvSource.setText(sourceDisplay);
        binding.tvSource.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvSource.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
        binding.tvSource.setClickable(true);
        binding.tvSource.setFocusable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);

        binding.tvSource.setOnClickListener(v -> {
            if (docKey != null) {
                DocumentDetailDialogFragment fragment =
                        DocumentDetailDialogFragment.newInstance(docKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });

        binding.btnManage.setVisibility(View.GONE);
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
}