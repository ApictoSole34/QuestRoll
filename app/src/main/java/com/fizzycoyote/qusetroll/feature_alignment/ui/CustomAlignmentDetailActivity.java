package com.fizzycoyote.qusetroll.feature_alignment.ui;

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
import com.fizzycoyote.qusetroll.core.models.custom.custom_alignment.CustomAlignmentDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_alignment.CustomAlignmentEntity;

public class CustomAlignmentDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "CUSTOM_ALIGNMENT_ID";
    private long id;
    private CustomAlignmentDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alignment_detail);

        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }

        dao = UserContentDatabase.getInstance(this).customAlignmentDao();
        dao.getById(id).observe(this, alignment -> {
            if (alignment != null) populateUI(alignment);
        });
    }

    private void populateUI(CustomAlignmentEntity a) {
        ((TextView) findViewById(R.id.tv_name)).setText(a.name);
        ((TextView) findViewById(R.id.tv_short)).setText(a.shortName != null ? "(" + a.shortName + ")" : "");
        ((TextView) findViewById(R.id.tv_morality)).setText("Morality: " + (a.morality != null ? a.morality : ""));
        ((TextView) findViewById(R.id.tv_attitude)).setText("Attitude: " + (a.societalAttitude != null ? a.societalAttitude : ""));
        ((TextView) findViewById(R.id.tv_description)).setText(a.description != null ? a.description : "");

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
                Intent i = new Intent(this, CustomAlignmentCreateActivity.class);
                i.putExtra(CustomAlignmentCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Alignment")
                        .setMessage("Are you sure you want to delete this alignment?")
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