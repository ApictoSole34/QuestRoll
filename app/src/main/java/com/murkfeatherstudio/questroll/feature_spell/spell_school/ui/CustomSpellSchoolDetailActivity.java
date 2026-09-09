package com.murkfeatherstudio.questroll.feature_spell.spell_school.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.murkfeatherstudio.questroll.databinding.ActivitySpellSchoolDetailBinding;

import io.noties.markwon.Markwon;

public class CustomSpellSchoolDetailActivity extends BaseActivity {

    public static final String EXTRA_ID = "CUSTOM_SPELL_SCHOOL_ID";
    private Markwon markwon;
    private long id;
    private CustomSpellSchoolDao dao;
    private ActivitySpellSchoolDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpellSchoolDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        markwon = Markwon.create(this);

        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            finish();
            return;
        }
        dao = UserContentDatabase.getInstance(this).customSpellSchoolDao();
        dao.getById(id).observe(this, school -> {
            if (school != null) populateUI(school);
        });
    }

    private void populateUI(CustomSpellSchoolEntity s) {
        binding.tvName.setText(s.name);
        markwon.setMarkdown(binding.tvDesc, s.description != null ? s.description : "");

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
                Intent i = new Intent(this, CustomSpellSchoolCreateActivity.class);
                i.putExtra(CustomSpellSchoolCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Spell School")
                        .setMessage("Are you sure you want to delete this spell school?")
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
