package com.fizzycoyote.qusetroll.feature_condition.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.condition.ConditionEntity;

import io.noties.markwon.Markwon;

public class ConditionDetailActivity extends AppCompatActivity {
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition_detail);
        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("CONDITION_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).conditionDao().getByKey(key).observe(this, condition -> {
            if (condition != null) populateUI(condition);
        });
    }

    private void populateUI(ConditionEntity c) {
        ((TextView) findViewById(R.id.tv_name)).setText(c.name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        markwon.setMarkdown(tvDesc, c.description != null ? c.description : "");
        TextView tvSource = findViewById(R.id.tv_source);
        tvSource.setText("Source: " + (c.documentName != null ? c.documentName : ""));
        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}