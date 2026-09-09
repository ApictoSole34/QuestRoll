package com.murkfeatherstudio.questroll.feature_creature.creature_type.ui;

import android.os.Bundle;
import android.view.View;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCreatureTypeDetailBinding;

import io.noties.markwon.Markwon;

public class CreatureTypeDetailActivity extends BaseActivity {
    private Markwon markwon;
    private ActivityCreatureTypeDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatureTypeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("CREATURE_TYPE_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).creatureTypeDao().getByKey(key).observe(this, type -> {
            if (type != null) populateUI(type);
        });
    }

    private void populateUI(CreatureTypeEntity t) {
        binding.tvName.setText(t.name);
        markwon.setMarkdown(binding.tvDesc, t.description != null ? t.description : "");
        binding.tvSource.setText("Source: " + (t.documentName != null ? t.documentName : ""));
        binding.btnManage.setVisibility(View.GONE);
    }
}