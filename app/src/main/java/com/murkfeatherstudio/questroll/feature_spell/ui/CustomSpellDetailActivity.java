package com.murkfeatherstudio.questroll.feature_spell.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomCastingOption;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.murkfeatherstudio.questroll.databinding.ActivitySpellDetailBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.noties.markwon.Markwon;

public class CustomSpellDetailActivity extends BaseActivity {

    public static final String EXTRA_SPELL_ID = "CUSTOM_SPECELL_ID";

    private Markwon markwon;
    private ActivitySpellDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpellDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.tvSpellName.setText(spell.name);

        String levelSchool = (spell.level == 0 ? "Cantrip" : "Level " + spell.level)
                + (spell.schoolName != null && !spell.schoolName.isEmpty()
                ? " • " + spell.schoolName : "");
        binding.tvLevelSchool.setText(levelSchool);

        binding.chipRitual.setVisibility(spell.ritual ? View.VISIBLE : View.GONE);
        binding.chipConcentration.setVisibility(spell.concentration ? View.VISIBLE : View.GONE);

        setTextView(binding.tvCastingTime, "Casting Time", spell.castingTime);
        setTextView(binding.tvRange, "Range", spell.rangeText);
        setTextView(binding.tvDuration, "Duration", spell.duration);
        binding.tvTarget.setVisibility(View.GONE);

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
        setTextView(binding.tvComponents, "Components", components.toString());

        if (spell.savingThrowAbility != null && !spell.savingThrowAbility.isEmpty()) {
            setTextView(binding.tvSavingThrow, "Saving Throw",
                    capitalize(spell.savingThrowAbility));
            binding.tvSavingThrow.setVisibility(View.VISIBLE);
        } else {
            binding.tvSavingThrow.setVisibility(View.GONE);
        }

        if (spell.damageRoll != null && !spell.damageRoll.isEmpty()) {
            String dmg = spell.damageRoll;
            if (spell.damageTypes != null && !spell.damageTypes.isEmpty()) {
                dmg += " " + String.join(", ", spell.damageTypes);
            }
            setTextView(binding.tvDamage, "Damage", dmg);
            binding.tvDamage.setVisibility(View.VISIBLE);
        } else {
            binding.tvDamage.setVisibility(View.GONE);
        }

        binding.tvClasses.setVisibility(View.GONE);

        if (spell.desc != null && !spell.desc.isEmpty()) {
            markwon.setMarkdown(binding.tvDesc, spell.desc);
        } else {
            binding.tvDesc.setText("");
        }

        if (spell.higherLevel != null && !spell.higherLevel.isEmpty()) {
            markwon.setMarkdown(binding.tvHigherLevel, spell.higherLevel);
            binding.higherLevelSection.setVisibility(View.VISIBLE);
        } else {
            binding.higherLevelSection.setVisibility(View.GONE);
        }

        buildCastingOptions(spell);

        setTextView(binding.tvSource, "Source", "Custom");

        setupEditButton(spell.id);
    }

    /**
     * NOTE: casting_options_container is built dynamically at runtime (number of options depends on JSON data),
     * there is no static XML layout for individual rows - in this place ViewBinding does not apply
     * for elements inside castingOptionsContainer.
     */
    private void buildCastingOptions(CustomSpellEntity spell) {
        if (spell.castingOptionsJson == null || spell.castingOptionsJson.isEmpty()) {
            binding.castingOptionsSection.setVisibility(View.GONE);
            return;
        }

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<CustomCastingOption>>(){}.getType();
            List<CustomCastingOption> options = gson.fromJson(spell.castingOptionsJson, type);

            if (options == null || options.isEmpty()) {
                binding.castingOptionsSection.setVisibility(View.GONE);
                return;
            }

            binding.castingOptionsSection.setVisibility(View.VISIBLE);
            binding.castingOptionsContainer.removeAllViews();

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

                binding.castingOptionsContainer.addView(layout);
            }
        } catch (Exception e) {
            binding.castingOptionsSection.setVisibility(View.GONE);
        }
    }

    private void setupEditButton(long spellId) {
        binding.btnManage.setVisibility(View.VISIBLE);
        binding.btnManage.setOnClickListener(v -> showManageMenu(v, spellId));
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

    private void setTextView(TextView tv, String label, String value) {
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
