package com.murkfeatherstudio.questroll.feature_environment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_environment.CustomEnvironmentDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_environment.CustomEnvironmentEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityEnvironmentDetailBinding;

public class CustomEnvironmentDetailActivity extends BaseActivity {
    public static final String EXTRA_ID = "CUSTOM_ENVIRONMENT_ID";
    private long id;
    private CustomEnvironmentDao dao;
    private ActivityEnvironmentDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnvironmentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }
        dao = UserContentDatabase.getInstance(this).customEnvironmentDao();
        dao.getById(id).observe(this, env -> {
            if (env != null) populateUI(env);
        });
    }

    private void populateUI(CustomEnvironmentEntity e) {
        binding.tvName.setText(e.name);
        String type;
        if (e.aquatic) type = "Aquatic";
        else if (e.planar) type = "Planar";
        else if (e.interior) type = "Interior";
        else type = "Land";

        binding.tvType.setText("Type: " + type);
        binding.tvDesc.setText(e.desc != null ? e.desc : "");
        binding.tvSource.setVisibility(View.GONE);
        binding.btnManage.setVisibility(View.VISIBLE);
        binding.btnManage.setOnClickListener(v -> showManageMenu(v, id));
    }

    private void showManageMenu(View anchor, long id) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent i = new Intent(this, CustomEnvironmentCreateActivity.class);
                i.putExtra(CustomEnvironmentCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Environment")
                        .setMessage("Are you sure you want to delete this environment?")
                        .setPositiveButton("Delete", (d, w) -> {
                            UserContentDatabase.getInstance(this).getQueryExecutor()
                                    .execute(() -> {
                                        dao.delete(id);
                                        runOnUiThread(this::finish);
                                    });
                        }).setNegativeButton("Cancel", null).show();
                return true;
            }
            return false;
        });
        popup.show();
    }
}