package com.fizzycoyote.qusetroll.feature_background.ui;

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
import com.fizzycoyote.qusetroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class CustomBackgroundDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "CUSTOM_BACKGROUND_ID";
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_detail);
        markwon = Markwon.create(this);

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) { finish(); return; }

        UserContentDatabase.getInstance(this).customBackgroundDao()
                .getById(id).observe(this, b -> { if (b != null) populateUI(b, id); });
    }

    private void populateUI(CustomBackgroundEntity b, long id) {
        ((TextView) findViewById(R.id.tv_background_name)).setText(b.name);

        TextView tvSource = findViewById(R.id.tv_source);
        tvSource.setText("Custom");
        tvSource.setVisibility(View.VISIBLE);

        TextView tvDesc = findViewById(R.id.tv_desc);
        if (b.desc != null && !b.desc.isEmpty()) {
            markwon.setMarkdown(tvDesc, b.desc);
            tvDesc.setVisibility(View.VISIBLE);
        } else {
            tvDesc.setVisibility(View.GONE);
        }

        buildBenefits(b.benefitsJson);

        View btnManage = findViewById(R.id.btnManage);
        if (btnManage != null) {
            btnManage.setVisibility(View.VISIBLE);
            btnManage.setOnClickListener(v -> showManageMenu(v, id));
        }
    }

    private void buildBenefits(String json) {
        LinearLayout container = findViewById(R.id.benefits_container);
        container.removeAllViews();
        if (json == null || json.isEmpty()) return;

        try {
            Type type = new TypeToken<List<BackgroundDto.BenefitDto>>(){}.getType();
            List<BackgroundDto.BenefitDto> benefits = new Gson().fromJson(json, type);
            if (benefits == null) return;
            for (BackgroundDto.BenefitDto benefit : benefits) {
                addBenefitView(container, benefit.name, benefit.desc);
            }
        } catch (Exception ignored) {}
    }

    private void addBenefitView(LinearLayout container, String name, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(10), 0, dp(4));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextSize(15);
        row.addView(tvName);

        if (desc != null && !desc.isEmpty()) {
            TextView tvDesc = new TextView(this);
            markwon.setMarkdown(tvDesc, desc);
            tvDesc.setTextSize(14);
            tvDesc.setPadding(0, dp(4), 0, 0);
            row.addView(tvDesc);
        }

        View divider = new View(this);
        divider.setBackgroundColor(0x1A000000);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.topMargin = dp(8);
        divider.setLayoutParams(params);
        row.addView(divider);

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
                startActivity(i); return true;
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
                        .setNegativeButton("Cancel", null).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}