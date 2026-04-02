package com.fizzycoyote.qusetroll.feature_service.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.service.ServiceEntity;

public class ServiceDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);
        String key = getIntent().getStringExtra("SERVICE_KEY");
        if (key == null) { finish(); return; }
        Open5eDatabase.getInstance(this).serviceDao().getByKey(key).observe(this, service -> {
            if (service != null) populateUI(service);
        });
    }

    private void populateUI(ServiceEntity s) {
        ((TextView) findViewById(R.id.tv_name)).setText(s.name);
        ((TextView) findViewById(R.id.tv_cost_detail)).setText("Cost: " + s.cost + " gp / " + s.detail);
        ((TextView) findViewById(R.id.tv_desc)).setText(s.desc != null ? s.desc : "");
        ((TextView) findViewById(R.id.tv_source)).setText("Source: " + (s.documentName != null ? s.documentName : ""));
    }
}
