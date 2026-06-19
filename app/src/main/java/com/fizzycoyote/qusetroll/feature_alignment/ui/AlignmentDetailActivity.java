package com.fizzycoyote.qusetroll.feature_alignment.ui;

import static com.fizzycoyote.qusetroll.feature_alignment.model.CombinedAlignment.formatAlignmentKey;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.alignment.AlignmentEntity;
import com.fizzycoyote.qusetroll.feature_alignment.model.CombinedAlignment;

public class AlignmentDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alignment_detail);

        String key = getIntent().getStringExtra("ALIGNMENT_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).alignmentDao()
                .getByKey(key).observe(this, alignment -> {
                    if (alignment != null) populateUI(alignment);
                });
    }

    private void populateUI(AlignmentEntity a) {
        TextView tvName = findViewById(R.id.tv_name);
        tvName.setText(CombinedAlignment.formatAlignmentKey(a.key));
        tvName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
        tvName.setTextColor(getColor(R.color.threads_text_primary));

        TextView tvShort = findViewById(R.id.tv_short);
        tvShort.setText(a.shortName != null ? "(" + a.shortName + ")" : "");
        tvShort.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        tvShort.setTextColor(getColor(R.color.threads_text_secondary));

        TextView tvMorality = findViewById(R.id.tv_morality);
        tvMorality.setText("Morality: " + (a.morality != null ? a.morality : ""));
        tvMorality.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        tvMorality.setTextColor(getColor(R.color.threads_text_primary));

        TextView tvAttitude = findViewById(R.id.tv_attitude);
        tvAttitude.setText("Attitude: " + (a.societalAttitude != null ? a.societalAttitude : ""));
        tvAttitude.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        tvAttitude.setTextColor(getColor(R.color.threads_text_primary));

        TextView tvDescription = findViewById(R.id.tv_description);
        tvDescription.setText(a.description != null ? a.description : "");
        tvDescription.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        tvDescription.setTextColor(getColor(R.color.threads_text_primary));

        TextView tvSource = findViewById(R.id.tv_source);
        String sourceText = "Source: " + (a.documentName != null ? a.documentName : "Unknown");
        tvSource.setText(sourceText);
        tvSource.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        tvSource.setTextColor(getColor(R.color.threads_text_secondary));
        tvSource.setVisibility(View.VISIBLE);
        tvSource.setClickable(true);
        tvSource.setFocusable(true);
        tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        tvSource.setOnClickListener(v -> {
            if (a.documentKey != null && !a.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(a.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });

        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}