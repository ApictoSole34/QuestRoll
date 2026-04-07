package com.fizzycoyote.qusetroll.feature_item.item_set.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_set.CustomItemSetEntity;
import com.fizzycoyote.qusetroll.feature_item.ui.CustomItemDetailActivity;
import com.fizzycoyote.qusetroll.feature_item.ui.ItemDetailActivity;
import io.noties.markwon.Markwon;

public class CustomItemSetDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "CUSTOM_ITEM_SET_ID";
    private long id;
    private CustomItemSetDao dao;
    private Markwon markwon;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_set_detail);
        markwon = Markwon.create(this);
        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) { finish(); return; }
        dao = UserContentDatabase.getInstance(this).customItemSetDao();
        dao.getById(id).observe(this, set -> {
            if (set != null) populateUI(set);
        });
    }
    private void populateUI(CustomItemSetEntity set) {
        ((TextView) findViewById(R.id.tv_name)).setText(set.name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        markwon.setMarkdown(tvDesc, set.desc != null ? set.desc : "");
        findViewById(R.id.tv_source).setVisibility(View.GONE);
        LinearLayout itemsContainer = findViewById(R.id.items_container);
        itemsContainer.removeAllViews();
        if (set.itemKeys != null) {
            for (String itemKey : set.itemKeys) {
                View itemView = getLayoutInflater().inflate(R.layout.item_item_set_item, itemsContainer, false);
                TextView tvItem = itemView.findViewById(R.id.tv_item_name);
                tvItem.setText(itemKey);
                itemView.setOnClickListener(v -> {
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
                itemsContainer.addView(itemView);
            }
        }
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