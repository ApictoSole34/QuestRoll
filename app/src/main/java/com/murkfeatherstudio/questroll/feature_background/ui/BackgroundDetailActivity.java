package com.murkfeatherstudio.questroll.feature_background.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundDto;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class BackgroundDetailActivity extends BaseActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_detail);
        markwon = Markwon.builder(this)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(TablePlugin.create(this))
                .build();

        String key = getIntent().getStringExtra("BACKGROUND_KEY");
        Open5eDatabase.getInstance(this).backgroundDao()
                .getByKey(key).observe(this, b -> { if (b != null) populateUI(b); });
    }

    private void populateUI(BackgroundEntity b) {
        ((TextView) findViewById(R.id.tv_background_name)).setText(b.name);

        TextView tvSource = findViewById(R.id.tv_source);
        if (b.documentName != null && !b.documentName.isEmpty()) {
            tvSource.setText(b.documentName);
            tvSource.setVisibility(View.VISIBLE);
            tvSource.setClickable(true);
            tvSource.setFocusable(true);
            tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
            tvSource.setOnClickListener(v -> {
                if (b.documentKey != null && !b.documentKey.isEmpty()) {
                    DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(b.documentKey);
                    fragment.show(getSupportFragmentManager(), "document_detail");
                }
            });
        } else {
            tvSource.setVisibility(View.GONE);
        }

        TextView tvDesc = findViewById(R.id.tv_desc);
        if (b.desc != null && !b.desc.isEmpty()) {
            markwon.setMarkdown(tvDesc, b.desc);
            tvDesc.setVisibility(View.VISIBLE);
        } else {
            tvDesc.setVisibility(View.GONE);
        }

        buildBenefits(b.benefitsJson);
    }

    private void buildBenefits(String json) {
        LinearLayout container = findViewById(R.id.benefits_container);
        container.removeAllViews();

        if (json == null || json.isEmpty()) return;

        try {
            Type type = new TypeToken<List<BackgroundDto.BenefitDto>>(){}.getType();
            List<BackgroundDto.BenefitDto> benefits = new Gson().fromJson(json, type);
            if (benefits == null || benefits.isEmpty()) return;

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
        tvName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold), Typeface.BOLD);
        tvName.setTextSize(15);
        tvName.setTextColor(getColor(R.color.threads_text_primary));
        row.addView(tvName);

        if (desc != null && !desc.isEmpty()) {
            TextView tvDesc = new TextView(this);
            markwon.setMarkdown(tvDesc, desc);
            tvDesc.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
            tvDesc.setTextSize(14);
            tvDesc.setTextColor(getColor(R.color.threads_text_primary));
            tvDesc.setPadding(0, dp(4), 0, 0);
            row.addView(tvDesc);
        }

        View divider = new View(this);
        divider.setBackgroundColor(0x1AFFFFFF);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.topMargin = dp(8);
        divider.setLayoutParams(params);
        row.addView(divider);

        container.addView(row);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}