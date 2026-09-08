package com.murkfeatherstudio.questroll.feature_creature.creature_type.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;

public class CustomCreatureTypeDetailActivity extends BaseActivity {
    public static final String EXTRA_ID = "CUSTOM_CREATURE_TYPE_ID";
    private long id;
    private CustomCreatureTypeDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creature_type_detail);

        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }

        dao = (CustomCreatureTypeDao) UserContentDatabase.getInstance(this).customCreatureTypeDao();
        dao.getById(id).observe(this, type -> {
            if (type != null) populateUI(type);
        });
    }

    private void populateUI(CustomCreatureTypeEntity t) {
        ((TextView) findViewById(R.id.tv_name)).setText(t.name);
        ((TextView) findViewById(R.id.tv_desc)).setText(t.description != null ? t.description : "");
        findViewById(R.id.tv_source).setVisibility(View.GONE);

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
                Intent i = new Intent(this, CustomCreatureTypeCreateActivity.class);
                i.putExtra(CustomCreatureTypeCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Creature Type")
                        .setMessage("Are you sure you want to delete this creature type?")
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