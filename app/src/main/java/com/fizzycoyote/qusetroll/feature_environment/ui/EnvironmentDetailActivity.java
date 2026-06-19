package com.fizzycoyote.qusetroll.feature_environment.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentEntity;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class EnvironmentDetailActivity extends BaseActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_detail);

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
        TextView tvName = findViewById(R.id.tv_name);
        tvName.setText(e.name);
        tvName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
        tvName.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        String type = "";
        if (e.aquatic) type = "Aquatic";
        else if (e.planar) type = "Planar";
        else if (e.interior) type = "Interior";
        else type = "Land";

        TextView tvType = findViewById(R.id.tv_type);
        tvType.setText("Type: " + type);
        tvType.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        tvType.setTextColor(getResources().getColor(R.color.threads_gold, null));

        TextView tvDesc = findViewById(R.id.tv_desc);
        String descText = e.desc != null ? e.desc : "No description.";
        markwon.setMarkdown(tvDesc, descText);
        tvDesc.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        tvDesc.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        TextView tvSource = findViewById(R.id.tv_source);

        final String docKey = (e.document != null && !e.document.isEmpty())
                ? extractKeyFromUrl(e.document)
                : null;

        String sourceDisplay = "Source: " + (docKey != null ? formatDocumentName(docKey) : "Unknown");
        tvSource.setText(sourceDisplay);
        tvSource.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        tvSource.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
        tvSource.setClickable(true);
        tvSource.setFocusable(true);
        tvSource.setBackgroundResource(android.R.drawable.list_selector_background);

        tvSource.setOnClickListener(v -> {
            if (docKey != null) {
                DocumentDetailDialogFragment fragment =
                        DocumentDetailDialogFragment.newInstance(docKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });

        findViewById(R.id.btnManage).setVisibility(View.GONE);
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