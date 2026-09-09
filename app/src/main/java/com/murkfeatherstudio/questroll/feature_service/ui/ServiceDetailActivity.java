package com.murkfeatherstudio.questroll.feature_service.ui;

import android.os.Bundle;
import android.view.View;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.service.ServiceEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityServiceDetailBinding;

public class ServiceDetailActivity extends BaseActivity {
    private ActivityServiceDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String key = getIntent().getStringExtra("SERVICE_KEY");
        if (key == null) {
            finish();
            return;
        }
        Open5eDatabase.getInstance(this).serviceDao().getByKey(key).observe(this, service -> {
            if (service != null) populateUI(service);
        });
    }

    private void populateUI(ServiceEntity s) {
        binding.tvName.setText(s.name);
        binding.tvCostDetail.setText("Cost: " + s.cost + " gp / " + s.detail);
        binding.tvDesc.setText(s.desc != null ? s.desc : "");
        
        String sourceText = "Source: " + (s.documentName != null ? s.documentName : "");
        binding.tvSource.setText(sourceText);
        binding.tvSource.setVisibility(View.VISIBLE);
        binding.tvSource.setClickable(true);
        binding.tvSource.setFocusable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        binding.tvSource.setOnClickListener(v -> {
            if (s.documentKey != null && !s.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(s.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });
    }
}
