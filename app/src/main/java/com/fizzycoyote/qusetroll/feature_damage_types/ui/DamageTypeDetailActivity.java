package com.fizzycoyote.qusetroll.feature_damage_types.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.damage_type.DamageTypeEntity;

public class DamageTypeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage_type_detail);

        String key = getIntent().getStringExtra("DAMAGE_TYPE_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).damageTypeDao()
                .getByKey(key).observe(this, type -> {
                    if (type != null) populateUI(type);
                });
    }

    private void populateUI(DamageTypeEntity type) {
        ((TextView) findViewById(R.id.tv_name)).setText(type.name);
        ((TextView) findViewById(R.id.tv_source)).setText("Source: " + (type.document != null ? type.document : "SRD"));
        ((TextView) findViewById(R.id.tv_description)).setText(type.description != null ? type.description : "No description.");
        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}