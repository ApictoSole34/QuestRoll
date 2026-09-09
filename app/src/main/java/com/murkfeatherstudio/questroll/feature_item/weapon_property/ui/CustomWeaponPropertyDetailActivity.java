package com.murkfeatherstudio.questroll.feature_item.weapon_property.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_weapon_property.CustomWeaponPropertyEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityWeaponPropertyDetailBinding;

public class CustomWeaponPropertyDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "CUSTOM_WEAPON_PROPERTY_ID";
    private long id;
    private CustomWeaponPropertyDao dao;
    private ActivityWeaponPropertyDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeaponPropertyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.tvName.setText(p.name);
        binding.tvType.setText(p.type != null ? p.type : "Property");
        binding.tvDesc.setText(p.desc != null ? p.desc : "");

        binding.btnManage.setVisibility(View.VISIBLE);
        binding.btnManage.setOnClickListener(v -> showManageMenu(v, id));
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
