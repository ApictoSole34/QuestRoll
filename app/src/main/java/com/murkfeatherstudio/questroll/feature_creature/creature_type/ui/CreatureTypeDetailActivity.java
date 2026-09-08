package com.murkfeatherstudio.questroll.feature_creature.creature_type.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeEntity;

import io.noties.markwon.Markwon;

public class CreatureTypeDetailActivity extends BaseActivity {
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creature_type_detail);
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
        ((TextView) findViewById(R.id.tv_name)).setText(t.name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        markwon.setMarkdown(tvDesc, t.description != null ? t.description : "");
        TextView tvSource = findViewById(R.id.tv_source);
        tvSource.setText("Source: " + (t.documentName != null ? t.documentName : ""));
        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}