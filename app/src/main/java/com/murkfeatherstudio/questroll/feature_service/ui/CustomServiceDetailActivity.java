package com.murkfeatherstudio.questroll.feature_service.ui;

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
import com.murkfeatherstudio.questroll.core.models.custom.custom_service.CustomServiceDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_service.CustomServiceEntity;

public class CustomServiceDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "CUSTOM_SERVICE_ID";
    private long id;
    private CustomServiceDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }

        dao = UserContentDatabase.getInstance(this).customServiceDao();
        dao.getById(id).observe(this, service -> {
            if (service != null) populateUI(service);
        });
    }

    private void populateUI(CustomServiceEntity s) {
        ((TextView) findViewById(R.id.tv_name)).setText(s.name);
        ((TextView) findViewById(R.id.tv_cost_detail)).setText("Cost: " + s.cost + " gp / " + s.detail);
        ((TextView) findViewById(R.id.tv_desc)).setText(s.desc != null ? s.desc : "");
        ((TextView) findViewById(R.id.tv_source)).setVisibility(View.GONE);

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
                Intent i = new Intent(this, CustomServiceCreateActivity.class);
                i.putExtra(CustomServiceCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Service")
                        .setMessage("Are you sure you want to delete this service?")
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