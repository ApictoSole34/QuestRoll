package com.murkfeatherstudio.questroll.feature_item.item_set.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_set.CustomItemSetDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityItemSetDetailBinding;
import com.murkfeatherstudio.questroll.databinding.ItemItemSetItemBinding;
import com.murkfeatherstudio.questroll.feature_item.ui.CustomItemDetailActivity;
import com.murkfeatherstudio.questroll.feature_item.ui.ItemDetailActivity;
import io.noties.markwon.Markwon;

public class CustomItemSetDetailActivity extends BaseActivity {
    public static final String EXTRA_ID = "CUSTOM_ITEM_SET_ID";
    private long id;
    private CustomItemSetDao dao;
    private Markwon markwon;
    private ActivityItemSetDetailBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemSetDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        markwon = Markwon.create(this);
        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) { finish(); return; }
        dao = UserContentDatabase.getInstance(this).customItemSetDao();
        dao.getById(id).observe(this, set -> {
            if (set != null) populateUI(set);
        });
    }

    /**
     * NOTE: Elements inside binding.itemsContainer are built dynamically at runtime.
     * We use ItemItemSetItemBinding for each row added via addView().
     */
    private void populateUI(CustomItemSetEntity set) {
        binding.tvName.setText(set.name);
        markwon.setMarkdown(binding.tvDesc, set.desc != null ? set.desc : "");
        binding.tvSource.setVisibility(View.GONE);
        
        binding.itemsContainer.removeAllViews();
        if (set.itemKeys != null) {
            for (String itemKey : set.itemKeys) {
                ItemItemSetItemBinding itemBinding = ItemItemSetItemBinding.inflate(getLayoutInflater(), binding.itemsContainer, false);
                itemBinding.tvItemName.setText(itemKey);
                itemBinding.getRoot().setOnClickListener(v -> {
                    if (itemKey.startsWith("custom_")) {
                        Intent i = new Intent(this, CustomItemDetailActivity.class);
                        i.putExtra("CUSTOM_ITEM_ID", Long.parseLong(itemKey.substring(7)));
                        startActivity(i);
                    } else {
                        Intent i = new Intent(this, ItemDetailActivity.class);
                        i.putExtra("ITEM_KEY", itemKey);
                        startActivity(i);
                    }
                });
                binding.itemsContainer.addView(itemBinding.getRoot());
            }
        }
        binding.btnManage.setVisibility(View.VISIBLE);
        binding.btnManage.setOnClickListener(v -> showManageMenu(v, id));
    }

    private void showManageMenu(View anchor, long id) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent i = new Intent(this, CustomItemSetCreateActivity.class);
                i.putExtra(CustomItemSetCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Item Set")
                        .setMessage("Are you sure?")
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
