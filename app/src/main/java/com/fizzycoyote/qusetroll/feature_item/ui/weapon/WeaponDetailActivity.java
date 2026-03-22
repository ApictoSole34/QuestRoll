package com.fizzycoyote.qusetroll.feature_item.ui.weapon;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon.WeaponDto;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon.WeaponEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class WeaponDetailActivity extends AppCompatActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weapon_detail);
        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("WEAPON_KEY");
        Open5eDatabase.getInstance(this).weaponDao()
                .getByKey(key).observe(this, w -> { if (w != null) populateUI(w); });
    }

    private void populateUI(WeaponEntity w) {
        ((TextView) findViewById(R.id.tv_weapon_name)).setText(w.name);

        TextView tvType = findViewById(R.id.tv_weapon_type);
        tvType.setText(w.isSimple ? "Simple Weapon" : "Martial Weapon");

        String dmg = (w.damageDice != null ? w.damageDice : "—") + " "
                + (w.damageTypeName != null ? w.damageTypeName : "");
        ((TextView) findViewById(R.id.tv_damage)).setText("Damage: " + dmg.trim());

        TextView tvRange = findViewById(R.id.tv_range);
        if (w.range > 0) {
            tvRange.setText("Range: " + (int) w.range + "/" + (int) w.longRange + " ft.");
            tvRange.setVisibility(View.VISIBLE);
        } else {
            tvRange.setText("Range: Melee");
            tvRange.setVisibility(View.VISIBLE);
        }

        TextView tvSource = findViewById(R.id.tv_source);
        if (w.documentName != null && !w.documentName.isEmpty()) {
            tvSource.setText("Source: " + w.documentName);
            tvSource.setVisibility(View.VISIBLE);
        } else {
            tvSource.setVisibility(View.GONE);
        }

        buildProperties(w.propertiesJson);
    }

    private void buildProperties(String json) {
        LinearLayout container = findViewById(R.id.properties_container);
        container.removeAllViews();

        if (json == null || json.isEmpty()) {
            findViewById(R.id.properties_section).setVisibility(View.GONE);
            return;
        }

        try {
            Type type = new TypeToken<List<WeaponDto.WeaponPropertyDto>>(){}.getType();
            List<WeaponDto.WeaponPropertyDto> props = new Gson().fromJson(json, type);
            if (props == null || props.isEmpty()) {
                findViewById(R.id.properties_section).setVisibility(View.GONE);
                return;
            }

            findViewById(R.id.properties_section).setVisibility(View.VISIBLE);
            for (WeaponDto.WeaponPropertyDto prop : props) {
                if (prop.property == null) continue;

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0, dp(8), 0, dp(4));

                TextView tvName = new TextView(this);
                String propName = prop.property.name;
                if (prop.detail != null && !prop.detail.isEmpty())
                    propName += " (" + prop.detail + ")";
                if (prop.property.type != null && !prop.property.type.isEmpty())
                    propName += " [" + prop.property.type + "]";
                tvName.setText(propName);
                tvName.setTypeface(null, Typeface.BOLD);
                tvName.setTextSize(14);
                row.addView(tvName);

                if (prop.property.desc != null && !prop.property.desc.isEmpty()) {
                    TextView tvDesc = new TextView(this);
                    markwon.setMarkdown(tvDesc, prop.property.desc);
                    tvDesc.setTextSize(13);
                    tvDesc.setPadding(0, dp(4), 0, 0);
                    row.addView(tvDesc);
                }

                container.addView(row);
            }
        } catch (Exception e) {
            findViewById(R.id.properties_section).setVisibility(View.GONE);
        }
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}