package com.murkfeatherstudio.questroll.feature_spell.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomCastingOption;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class CustomSpellDetailActivity extends BaseActivity {

    public static final String EXTRA_SPELL_ID = "CUSTOM_SPELL_ID";

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_detail);

        markwon = Markwon.create(this);

        long spellId = getIntent().getLongExtra(EXTRA_SPELL_ID, -1);
        if (spellId == -1) { finish(); return; }

        UserContentDatabase.getInstance(this)
                .customSpellDao()
                .getById(spellId)
                .observe(this, spell -> {
                    if (spell != null) populateUI(spell);
                });
    }

    private void populateUI(CustomSpellEntity spell) {
        ((TextView) findViewById(R.id.tv_spell_name)).setText(spell.name);

        String levelSchool = (spell.level == 0 ? "Cantrip" : "Level " + spell.level)
                + (spell.schoolName != null && !spell.schoolName.isEmpty()
                ? " • " + spell.schoolName : "");
        ((TextView) findViewById(R.id.tv_level_school)).setText(levelSchool);

        findViewById(R.id.chip_ritual)
                .setVisibility(spell.ritual ? View.VISIBLE : View.GONE);
        findViewById(R.id.chip_concentration)
                .setVisibility(spell.concentration ? View.VISIBLE : View.GONE);

        setTextView(R.id.tv_casting_time, "Casting Time", spell.castingTime);
        setTextView(R.id.tv_range, "Range", spell.rangeText);
        setTextView(R.id.tv_duration, "Duration", spell.duration);
        setTextView(R.id.tv_target, "Target", null); // custom nie ma target_type
        findViewById(R.id.tv_target).setVisibility(View.GONE);

        StringBuilder components = new StringBuilder();
        if (spell.verbal) components.append("V");
        if (spell.somatic) { if (components.length() > 0) components.append(", "); components.append("S"); }
        if (spell.material) {
            if (components.length() > 0) components.append(", ");
            components.append("M");
            if (spell.materialSpecified != null && !spell.materialSpecified.isEmpty()) {
                components.append(" (").append(spell.materialSpecified).append(")");
            }
        }
        setTextView(R.id.tv_components, "Components", components.toString());

        if (spell.savingThrowAbility != null && !spell.savingThrowAbility.isEmpty()) {
            setTextView(R.id.tv_saving_throw, "Saving Throw",
                    capitalize(spell.savingThrowAbility));
            findViewById(R.id.tv_saving_throw).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tv_saving_throw).setVisibility(View.GONE);
        }

        if (spell.damageRoll != null && !spell.damageRoll.isEmpty()) {
            String dmg = spell.damageRoll;
            if (spell.damageTypes != null && !spell.damageTypes.isEmpty()) {
                dmg += " " + String.join(", ", spell.damageTypes);
            }
            setTextView(R.id.tv_damage, "Damage", dmg);
            findViewById(R.id.tv_damage).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tv_damage).setVisibility(View.GONE);
        }

        findViewById(R.id.tv_classes).setVisibility(View.GONE);

        TextView tvDesc = findViewById(R.id.tv_desc);
        if (spell.desc != null && !spell.desc.isEmpty()) {
            markwon.setMarkdown(tvDesc, spell.desc);
        } else {
            tvDesc.setText("");
        }

        LinearLayout higherLevelSection = findViewById(R.id.higher_level_section);
        TextView tvHigherLevel = findViewById(R.id.tv_higher_level);
        if (spell.higherLevel != null && !spell.higherLevel.isEmpty()) {
            markwon.setMarkdown(tvHigherLevel, spell.higherLevel);
            higherLevelSection.setVisibility(View.VISIBLE);
        } else {
            higherLevelSection.setVisibility(View.GONE);
        }

        buildCastingOptions(spell);

        setTextView(R.id.tv_source, "Source", "Custom");

        setupEditButton(spell.id);
    }

    private void buildCastingOptions(CustomSpellEntity spell) {
        LinearLayout castingOptionsSection = findViewById(R.id.casting_options_section);
        LinearLayout castingOptionsContainer = findViewById(R.id.casting_options_container);

        if (spell.castingOptionsJson == null || spell.castingOptionsJson.isEmpty()) {
            castingOptionsSection.setVisibility(View.GONE);
            return;
        }

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<CustomCastingOption>>(){}.getType();
            List<CustomCastingOption> options = gson.fromJson(spell.castingOptionsJson, type);

            if (options == null || options.isEmpty()) {
                castingOptionsSection.setVisibility(View.GONE);
                return;
            }

            castingOptionsSection.setVisibility(View.VISIBLE);
            castingOptionsContainer.removeAllViews();

            for (CustomCastingOption option : options) {
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(0, dpToPx(8), 0, dpToPx(8));

                TextView tvType = new TextView(this);
                tvType.setText(option.type);
                tvType.setTypeface(null, Typeface.BOLD);
                tvType.setTextSize(14);
                layout.addView(tvType);

                StringBuilder details = new StringBuilder();
                if (option.damageRoll != null && !option.damageRoll.isEmpty())
                    details.append("Damage: ").append(option.damageRoll);
                if (option.range != null && !option.range.isEmpty()) {
                    if (details.length() > 0) details.append(" • ");
                    details.append("Range: ").append(option.range);
                }
                if (option.duration != null && !option.duration.isEmpty()) {
                    if (details.length() > 0) details.append(" • ");
                    details.append("Duration: ").append(option.duration);
                }
                if (option.desc != null && !option.desc.isEmpty()) {
                    if (details.length() > 0) details.append("\n");
                    details.append(option.desc);
                }

                if (details.length() > 0) {
                    TextView tvDetails = new TextView(this);
                    tvDetails.setText(details.toString());
                    tvDetails.setTextSize(13);
                    layout.addView(tvDetails);
                }

                castingOptionsContainer.addView(layout);
            }
        } catch (Exception e) {
            castingOptionsSection.setVisibility(View.GONE);
        }
    }

    private void setupEditButton(long spellId) {
        View btnManage = findViewById(R.id.btnManage);
        if (btnManage != null) {
            btnManage.setVisibility(View.VISIBLE);
            btnManage.setOnClickListener(v -> showManageMenu(v, spellId));
        }
    }

    private void showManageMenu(View anchor, long spellId) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit Spell");
        popup.getMenu().add(0, 2, 1, "Delete Spell");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent intent = new Intent(this, CustomSpellCreateActivity.class);
                intent.putExtra(CustomSpellCreateActivity.EXTRA_EDIT_SPELL_ID, spellId);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == 2) {
                confirmDelete(spellId);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void confirmDelete(long spellId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Spell")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (d, w) -> {
                    UserContentDatabase.getInstance(this)
                            .getQueryExecutor()
                            .execute(() -> {
                                UserContentDatabase.getInstance(this)
                                        .customSpellDao()
                                        .delete(spellId);
                                runOnUiThread(this::finish);
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setTextView(int viewId, String label, String value) {
        TextView tv = findViewById(viewId);
        if (value != null && !value.isEmpty()) {
            tv.setText(label + ": " + value);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
