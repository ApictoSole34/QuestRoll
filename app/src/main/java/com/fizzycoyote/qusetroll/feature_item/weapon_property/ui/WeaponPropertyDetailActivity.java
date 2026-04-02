package com.fizzycoyote.qusetroll.feature_item.weapon_property.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon_property.WeaponPropertyEntity;

public class WeaponPropertyDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weapon_property_detail);
        String key = getIntent().getStringExtra("PROPERTY_KEY");
        if (key == null) { finish(); return; }
        Open5eDatabase.getInstance(this).weaponPropertyDao().getByKey(key).observe(this, property -> {
            if (property != null) populateUI(property);
        });
    }

    private void populateUI(WeaponPropertyEntity p) {
        ((TextView) findViewById(R.id.tv_name)).setText(p.name);
        ((TextView) findViewById(R.id.tv_type)).setText(p.type != null ? p.type : "Property");
        ((TextView) findViewById(R.id.tv_desc)).setText(p.desc != null ? p.desc : "");
        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}