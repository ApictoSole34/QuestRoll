package com.murkfeatherstudio.questroll.feature_item.item_rarity.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.item_rarity.ItemRarityEntity;

public class ItemRarityDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_rarity_detail);

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
        ((TextView) findViewById(R.id.tv_name)).setText(r.name);
        ((TextView) findViewById(R.id.tv_rank)).setText("Rank: " + r.rank);
        ((TextView) findViewById(R.id.tv_description)).setText("");
        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}