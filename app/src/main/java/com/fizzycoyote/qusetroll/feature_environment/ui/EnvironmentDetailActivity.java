package com.fizzycoyote.qusetroll.feature_environment.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.environment.EnvironmentEntity;

public class EnvironmentDetailActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_detail);
        String key = getIntent().getStringExtra("ENVIRONMENT_KEY");
        if (key == null) { finish(); return; }
        Open5eDatabase.getInstance(this).environmentDao().getByKey(key).observe(this, env -> {
            if (env != null) populateUI(env);
        });
    }
    private void populateUI(EnvironmentEntity e) {
        ((TextView) findViewById(R.id.tv_name)).setText(e.name);
        String type = "";
        if (e.aquatic) type = "Aquatic";
        else if (e.planar) type = "Planar";
        else if (e.interior) type = "Interior";
        else type = "Land";
        ((TextView) findViewById(R.id.tv_type)).setText("Type: " + type);
        ((TextView) findViewById(R.id.tv_desc)).setText(e.desc != null ? e.desc : "No description.");
        ((TextView) findViewById(R.id.tv_source)).setText("Source: " + (e.document != null ? e.document : ""));
        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}
