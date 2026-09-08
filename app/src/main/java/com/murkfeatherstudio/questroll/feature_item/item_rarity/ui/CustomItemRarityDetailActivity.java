package com.murkfeatherstudio.questroll.feature_item.item_rarity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity.CustomItemRarityDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_rarity.CustomItemRarityEntity;

public class CustomItemRarityDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "CUSTOM_ITEM_RARITY_ID";
    private long id;
    private CustomItemRarityDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_rarity_detail);

        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }

        dao = UserContentDatabase.getInstance(this).customItemRarityDao();
        dao.getById(id).observe(this, rarity -> {
            if (rarity != null) populateUI(rarity);
        });
    }

    private void populateUI(CustomItemRarityEntity r) {
        ((TextView) findViewById(R.id.tv_name)).setText(r.name);
        ((TextView) findViewById(R.id.tv_rank)).setText("Rank: " + r.rank);
        ((TextView) findViewById(R.id.tv_description)).setText(r.description != null ? r.description : "");

        Button btnManage = findViewById(R.id.btnManage);
        btnManage.setVisibility(View.VISIBLE);
        btnManage.setOnClickListener(v -> showManageMenu(v, id));
    }

    private void showManageMenu(View anchor, long id) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent i = new Intent(this, CustomItemRarityCreateActivity.class);
                i.putExtra(CustomItemRarityCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Rarity")
                        .setMessage("Are you sure you want to delete this rarity?")
                        .setPositiveButton("Delete", (d, w) -> {
                            UserContentDatabase.getInstance(this).getQueryExecutor()
                                    .execute(() -> {
                                        dao.delete(id);
                                        runOnUiThread(this::finish);
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }
}