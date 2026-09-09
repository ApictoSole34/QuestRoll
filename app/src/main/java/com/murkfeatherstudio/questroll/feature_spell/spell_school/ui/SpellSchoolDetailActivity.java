package com.murkfeatherstudio.questroll.feature_spell.spell_school.ui;

import android.os.Bundle;
import android.view.View;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.spell_school.SpellSchoolEntity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.databinding.ActivitySpellSchoolDetailBinding;

import io.noties.markwon.Markwon;

public class SpellSchoolDetailActivity extends BaseActivity {

    private Markwon markwon;
    private ActivitySpellSchoolDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpellSchoolDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
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
        binding.tvName.setText(s.name);

        markwon.setMarkdown(binding.tvDesc, s.desc != null ? s.desc : "");

        if (s.document != null && !s.document.isEmpty()) {
            binding.tvSource.setText("Source: " + s.document);
            binding.tvSource.setVisibility(View.VISIBLE);
            binding.tvSource.setClickable(true);
            binding.tvSource.setFocusable(true);
            binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
            binding.tvSource.setOnClickListener(v -> {
                DocumentDetailDialogFragment fragment =
                        DocumentDetailDialogFragment.newInstance(s.document);
                fragment.show(getSupportFragmentManager(), "document_detail");
            });
        } else {
            binding.tvSource.setVisibility(View.GONE);
        }

        binding.btnManage.setVisibility(View.GONE);
    }
}