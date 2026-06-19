package com.fizzycoyote.qusetroll.feature_creature.creature_type.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.creature_type.CreatureTypeEntity;

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