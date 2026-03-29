package com.fizzycoyote.qusetroll.feature_damage_types.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_damage_types.CustomDamageTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_damage_types.CustomDamageTypeEntity;

public class CustomDamageTypeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "CUSTOM_DAMAGE_TYPE_ID";
    private long id;
    private CustomDamageTypeDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage_type_detail);

        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }

        dao = UserContentDatabase.getInstance(this).customDamageTypeDao();
        dao.getById(id).observe(this, type -> {
            if (type != null) populateUI(type);
        });
    }

    private void populateUI(CustomDamageTypeEntity type) {
        ((TextView) findViewById(R.id.tv_name)).setText(type.name);
        ((TextView) findViewById(R.id.tv_source)).setText("Source: Custom");
        String desc = type.description != null ? type.description : "No description.";
        ((TextView) findViewById(R.id.tv_description)).setText(desc);

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
                Intent i = new Intent(this, CustomDamageTypeCreateActivity.class);
                i.putExtra(CustomDamageTypeCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Damage Type")
                        .setMessage("Are you sure you want to delete this damage type?")
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