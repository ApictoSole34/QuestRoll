package com.fizzycoyote.qusetroll.feature_item.item_category.ui;

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
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;

public class CustomItemCategoryDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "CUSTOM_CATEGORY_ID";
    private long id;
    private CustomItemCategoryDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_category_detail);
        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) { finish(); return; }
        dao = UserContentDatabase.getInstance(this).customItemCategoryDao();
        dao.getById(id).observe(this, cat -> {
            if (cat != null) populateUI(cat);
        });
    }
    private void populateUI(CustomItemCategoryEntity cat) {
        ((TextView) findViewById(R.id.tv_name)).setText(cat.name);
        ((TextView) findViewById(R.id.tv_desc)).setText(cat.description != null ? cat.description : "");
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
                Intent i = new Intent(this, CustomItemCategoryCreateActivity.class);
                i.putExtra(CustomItemCategoryCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Category")
                        .setMessage("Are you sure you want to delete this category?")
                        .setPositiveButton("Delete", (d, w) -> {
                            UserContentDatabase.getInstance(this).getQueryExecutor()
                                    .execute(() -> {
                                        dao.delete(id);
                                        runOnUiThread(this::finish);
                                    });
                        })
                        .setNegativeButton("Cancel", null).show();
                return true;
            }
            return false;
        });
        popup.show();
    }
}
