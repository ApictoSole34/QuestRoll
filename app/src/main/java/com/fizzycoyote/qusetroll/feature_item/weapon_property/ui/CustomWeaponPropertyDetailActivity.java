package com.fizzycoyote.qusetroll.feature_item.weapon_property.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;

public class CustomWeaponPropertyDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "CUSTOM_WEAPON_PROPERTY_ID";
    private long id;
    private CustomWeaponPropertyDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weapon_property_detail);

        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }

        dao = UserContentDatabase.getInstance(this).customWeaponPropertyDao();
        dao.getById(id).observe(this, prop -> {
            if (prop != null) populateUI(prop);
        });
    }

    private void populateUI(CustomWeaponPropertyEntity p) {
        ((TextView) findViewById(R.id.tv_name)).setText(p.name);
        ((TextView) findViewById(R.id.tv_type)).setText(p.type != null ? p.type : "Property");
        ((TextView) findViewById(R.id.tv_desc)).setText(p.desc != null ? p.desc : "");

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
                Intent i = new Intent(this, CustomWeaponPropertyCreateActivity.class);
                i.putExtra(CustomWeaponPropertyCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Property")
                        .setMessage("Are you sure you want to delete this property?")
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
