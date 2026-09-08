package com.murkfeatherstudio.questroll.feature_background.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterCreationDTO;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class CustomBackgroundDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "CUSTOM_BACKGROUND_ID";
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_detail);
        markwon = Markwon.create(this);

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }

        UserContentDatabase.getInstance(this).customBackgroundDao()
                .getById(id).observe(this, entity -> {
                    if (entity != null) populateUI(entity);
                });
    }

    private void populateUI(CustomBackgroundEntity b) {
        TextView tvName = findViewById(R.id.tv_background_name);
        TextView tvSource = findViewById(R.id.tv_source);
        TextView tvDesc = findViewById(R.id.tv_desc);
        LinearLayout container = findViewById(R.id.benefits_container);

        tvName.setText(b.name);
        tvSource.setText("Custom (" + (b.gameSystem != null ? b.gameSystem : "?") + ")");
        tvSource.setVisibility(View.VISIBLE);

        if (b.desc != null && !b.desc.isEmpty()) {
            markwon.setMarkdown(tvDesc, b.desc);
            tvDesc.setVisibility(View.VISIBLE);
        } else {
            tvDesc.setVisibility(View.GONE);
        }

        container.removeAllViews();

        // Starting Gold
        addSimpleRow(container, "Starting Gold", b.startingGold + " gp");

        // Equipment
        if (b.equipmentJson != null && !b.equipmentJson.isEmpty()) {
            Type type = new TypeToken<List<CharacterCreationDTO.InventoryItemDTO>>(){}.getType();
            List<CharacterCreationDTO.InventoryItemDTO> items = new Gson().fromJson(b.equipmentJson, type);
            if (items != null && !items.isEmpty()) {
                addHeader(container, "Equipment");
                for (CharacterCreationDTO.InventoryItemDTO item : items) {
                    addSimpleRow(container, null, "• " + item.customName + " (x" + item.quantity + ", " + item.customWeight + " lb)");
                }
            }
        }

        // Languages
        if (b.languagesJson != null && !b.languagesJson.isEmpty()) {
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> languages = new Gson().fromJson(b.languagesJson, type);
            if (languages != null && !languages.isEmpty()) {
                addHeader(container, "Languages");
                for (String lang : languages) {
                    addSimpleRow(container, null, "• " + lang);
                }
            }
        }

        // Skill Proficiencies
        if (b.skillProficienciesJson != null && !b.skillProficienciesJson.isEmpty()) {
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> skills = new Gson().fromJson(b.skillProficienciesJson, type);
            if (skills != null && !skills.isEmpty()) {
                addHeader(container, "Skill Proficiencies");
                for (String skill : skills) {
                    addSimpleRow(container, null, "• " + skill);
                }
            }
        }

        // Tool Proficiencies
        if (b.toolProficienciesJson != null && !b.toolProficienciesJson.isEmpty()) {
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> tools = new Gson().fromJson(b.toolProficienciesJson, type);
            if (tools != null && !tools.isEmpty()) {
                addHeader(container, "Tool Proficiencies");
                for (String tool : tools) {
                    addSimpleRow(container, null, "• " + tool);
                }
            }
        }

        // Features
        if (b.featuresJson != null && !b.featuresJson.isEmpty()) {
            Type type = new TypeToken<List<CharacterTraitEntity>>(){}.getType();
            List<CharacterTraitEntity> features = new Gson().fromJson(b.featuresJson, type);
            if (features != null && !features.isEmpty()) {
                addHeader(container, "Features");
                for (CharacterTraitEntity feature : features) {
                    addSimpleRow(container, feature.name, feature.description);
                }
            }
        }

        View btnManage = findViewById(R.id.btnManage);
        if (btnManage != null) {
            btnManage.setVisibility(View.VISIBLE);
            btnManage.setOnClickListener(v -> showManageMenu(v, b.id));
        }
    }

    private void addHeader(LinearLayout container, String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextSize(16);
        header.setPadding(0, dp(16), 0, dp(4));
        container.addView(header);
    }

    private void addSimpleRow(LinearLayout container, String label, String value) {
        TextView row = new TextView(this);
        if (label != null) {
            row.setText(label + ": " + value);
            row.setTypeface(null, Typeface.BOLD);
        } else {
            row.setText(value);
        }
        row.setTextSize(14);
        row.setPadding(0, dp(4), 0, dp(4));
        container.addView(row);
    }

    private void showManageMenu(View anchor, long id) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent i = new Intent(this, CustomBackgroundCreateActivity.class);
                i.putExtra(CustomBackgroundCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Background")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (d, w) ->
                                UserContentDatabase.getInstance(this).getQueryExecutor()
                                        .execute(() -> {
                                            UserContentDatabase.getInstance(this)
                                                    .customBackgroundDao().delete(id);
                                            runOnUiThread(this::finish);
                                        }))
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}