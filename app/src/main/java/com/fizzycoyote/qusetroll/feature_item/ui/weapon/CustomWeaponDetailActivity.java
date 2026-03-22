package com.fizzycoyote.qusetroll.feature_item.ui.weapon;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureAction;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon.CustomWeaponEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class CustomWeaponDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "CUSTOM_WEAPON_ID";
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weapon_detail);
        markwon = Markwon.create(this);

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) { finish(); return; }

        UserContentDatabase.getInstance(this).customWeaponDao()
                .getById(id).observe(this, w -> { if (w != null) populateUI(w, id); });
    }

    private void populateUI(CustomWeaponEntity w, long id) {
        ((TextView) findViewById(R.id.tv_weapon_name)).setText(w.name);
        ((TextView) findViewById(R.id.tv_weapon_type)).setText(
                w.isSimple ? "Simple Weapon (Custom)" : "Martial Weapon (Custom)");

        String dmg = (w.damageDice != null && !w.damageDice.isEmpty() ? w.damageDice : "—")
                + " " + (w.damageTypeName != null ? w.damageTypeName : "");
        ((TextView) findViewById(R.id.tv_damage)).setText("Damage: " + dmg.trim());

        TextView tvRange = findViewById(R.id.tv_range);
        if (w.range > 0) {
            tvRange.setText("Range: " + (int) w.range + "/" + (int) w.longRange + " ft.");
        } else {
            tvRange.setText("Range: Melee");
        }
        tvRange.setVisibility(View.VISIBLE);

        TextView tvSource = findViewById(R.id.tv_source);
        tvSource.setText("Source: Custom");
        tvSource.setVisibility(View.VISIBLE);

        buildProperties(w.propertiesJson);

        if (w.notes != null && !w.notes.isEmpty()) {
            LinearLayout container = findViewById(R.id.properties_container);
            TextView tvNotes = new TextView(this);
            tvNotes.setText("Notes: " + w.notes);
            tvNotes.setTextSize(14);
            tvNotes.setPadding(0, dp(12), 0, 0);
            container.addView(tvNotes);
        }

        View btnManage = findViewById(R.id.btnManage);
        if (btnManage != null) {
            btnManage.setVisibility(View.VISIBLE);
            btnManage.setOnClickListener(v -> showManageMenu(v, id));
        }
    }

    private void buildProperties(String json) {
        LinearLayout container = findViewById(R.id.properties_container);
        container.removeAllViews();
        if (json == null || json.isEmpty()) {
            findViewById(R.id.properties_section).setVisibility(View.GONE);
            return;
        }
        try {
            Type type = new TypeToken<List<CustomCreatureAction>>(){}.getType();
            List<CustomCreatureAction> props = new Gson().fromJson(json, type);
            if (props == null || props.isEmpty()) {
                findViewById(R.id.properties_section).setVisibility(View.GONE);
                return;
            }
            findViewById(R.id.properties_section).setVisibility(View.VISIBLE);
            for (CustomCreatureAction p : props) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0, dp(8), 0, dp(4));

                TextView tvName = new TextView(this);
                tvName.setText(p.name);
                tvName.setTypeface(null, Typeface.BOLD);
                tvName.setTextSize(14);
                row.addView(tvName);

                if (p.desc != null && !p.desc.isEmpty()) {
                    TextView tvDesc = new TextView(this);
                    markwon.setMarkdown(tvDesc, p.desc);
                    tvDesc.setTextSize(13);
                    row.addView(tvDesc);
                }
                container.addView(row);
            }
        } catch (Exception e) {
            findViewById(R.id.properties_section).setVisibility(View.GONE);
        }
    }

    private void showManageMenu(View anchor, long id) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent i = new Intent(this, CustomWeaponCreateActivity.class);
                i.putExtra(CustomWeaponCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i); return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Weapon")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (d, w) ->
                                UserContentDatabase.getInstance(this).getQueryExecutor()
                                        .execute(() -> {
                                            UserContentDatabase.getInstance(this)
                                                    .customWeaponDao().delete(id);
                                            runOnUiThread(this::finish);
                                        }))
                        .setNegativeButton("Cancel", null).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}