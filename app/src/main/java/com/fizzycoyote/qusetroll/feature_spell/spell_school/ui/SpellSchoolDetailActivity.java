package com.fizzycoyote.qusetroll.feature_spell.spell_school.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.spell_school.SpellSchoolEntity;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;

import io.noties.markwon.Markwon;

public class SpellSchoolDetailActivity extends AppCompatActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_school_detail);
        markwon = Markwon.create(this);

        String slug = getIntent().getStringExtra("SCHOOL_SLUG");
        if (slug == null) {
            finish();
            return;
        }
        Open5eDatabase.getInstance(this).spellSchoolDao().getBySlug(slug).observe(this, school -> {
            if (school != null) populateUI(school);
        });
    }

    private void populateUI(SpellSchoolEntity s) {
        ((TextView) findViewById(R.id.tv_name)).setText(s.name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        markwon.setMarkdown(tvDesc, s.description != null ? s.description : "");

        TextView tvSource = findViewById(R.id.tv_source);
        if (s.documentUrl != null && !s.documentUrl.isEmpty()) {
            tvSource.setText("Source: " + s.documentUrl);
            tvSource.setVisibility(View.VISIBLE);
            tvSource.setClickable(true);
            tvSource.setFocusable(true);
            tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
            tvSource.setOnClickListener(v -> {
                String docKey = extractKeyFromUrl(s.documentUrl);
                if (docKey != null) {
                    DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(docKey);
                    fragment.show(getSupportFragmentManager(), "document_detail");
                }
            });
        } else {
            tvSource.setVisibility(View.GONE);
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
}