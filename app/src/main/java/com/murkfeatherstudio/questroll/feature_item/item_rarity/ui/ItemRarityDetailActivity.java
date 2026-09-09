package com.murkfeatherstudio.questroll.feature_item.item_rarity.ui;

import android.os.Bundle;
import android.view.View;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.item_rarity.ItemRarityEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityItemRarityDetailBinding;

public class ItemRarityDetailActivity extends BaseActivity {

    private ActivityItemRarityDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemRarityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String key = getIntent().getStringExtra("ITEM_RARITY_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).itemRarityDao()
                .getByKey(key).observe(this, rarity -> {
                    if (rarity != null) populateUI(rarity);
                });
    }

    private void populateUI(ItemRarityEntity r) {
        binding.tvName.setText(r.name);
        binding.tvRank.setText("Rank: " + r.rank);
        binding.tvDescription.setText("");
        binding.btnManage.setVisibility(View.GONE);
    }
}
