package com.murkfeatherstudio.questroll.feature_spell.spell_school.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.spell_school.SpellSchoolEntity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;

import io.noties.markwon.Markwon;

public class SpellSchoolDetailActivity extends BaseActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_school_detail);
        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("SCHOOL_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this)
                .spellSchoolDao()
                .getByKey(key)
                .observe(this, school -> {
                    if (school != null) populateUI(school);
                });
    }

    private void populateUI(SpellSchoolEntity s) {
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        TextView tvSource = findViewById(R.id.tv_source);

        tvName.setText(s.name);

        markwon.setMarkdown(tvDesc, s.desc != null ? s.desc : "");

        if (s.document != null && !s.document.isEmpty()) {
            tvSource.setText("Source: " + s.document);
            tvSource.setVisibility(View.VISIBLE);
            tvSource.setClickable(true);
            tvSource.setFocusable(true);
            tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
            tvSource.setOnClickListener(v -> {
                DocumentDetailDialogFragment fragment =
                        DocumentDetailDialogFragment.newInstance(s.document);
                fragment.show(getSupportFragmentManager(), "document_detail");
            });
        } else {
            tvSource.setVisibility(View.GONE);
        }

        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}