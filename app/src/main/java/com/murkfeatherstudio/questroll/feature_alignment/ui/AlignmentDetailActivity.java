package com.murkfeatherstudio.questroll.feature_alignment.ui;

import android.os.Bundle;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.alignment.AlignmentEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityAlignmentDetailBinding;
import com.murkfeatherstudio.questroll.feature_alignment.model.CombinedAlignment;

public class AlignmentDetailActivity extends BaseActivity {

    private ActivityAlignmentDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlignmentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.tvName.setText(CombinedAlignment.formatAlignmentKey(a.key));
        binding.tvName.setTypeface(ResourcesCompat.getFont(this, R.font.cinzel_bold));
        binding.tvName.setTextColor(getColor(R.color.threads_text_primary));

        binding.tvShort.setText(a.shortName != null ? "(" + a.shortName + ")" : "");
        binding.tvShort.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvShort.setTextColor(getColor(R.color.threads_text_secondary));

        binding.tvMorality.setText("Morality: " + (a.morality != null ? a.morality : ""));
        binding.tvMorality.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvMorality.setTextColor(getColor(R.color.threads_text_primary));

        binding.tvAttitude.setText("Attitude: " + (a.societalAttitude != null ? a.societalAttitude : ""));
        binding.tvAttitude.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvAttitude.setTextColor(getColor(R.color.threads_text_primary));

        binding.tvDescription.setText(a.description != null ? a.description : "");
        binding.tvDescription.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvDescription.setTextColor(getColor(R.color.threads_text_primary));

        String sourceText = "Source: " + (a.documentName != null ? a.documentName : "Unknown");
        binding.tvSource.setText(sourceText);
        binding.tvSource.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        binding.tvSource.setTextColor(getColor(R.color.threads_text_secondary));
        binding.tvSource.setVisibility(View.VISIBLE);
        binding.tvSource.setClickable(true);
        binding.tvSource.setFocusable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        binding.tvSource.setOnClickListener(v -> {
            if (a.documentKey != null && !a.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(a.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });

        binding.btnManage.setVisibility(View.GONE);
    }
}