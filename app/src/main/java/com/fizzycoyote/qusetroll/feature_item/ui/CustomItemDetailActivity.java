package com.fizzycoyote.qusetroll.feature_item.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_item.CustomItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;
import com.google.gson.Gson;

import io.noties.markwon.Markwon;

public class CustomItemDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "CUSTOM_ITEM_ID";

    private Markwon markwon;
    private CustomItemDao customItemDao;
    private long itemId;
    private CustomItemEntity currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_item_detail);
        markwon = Markwon.create(this);

        itemId = getIntent().getLongExtra(EXTRA_ID, -1);
        if (itemId == -1) {
            finish();
            return;
        }

        customItemDao = UserContentDatabase.getInstance(this).customItemDao();
        customItemDao.getById(itemId).observe(this, item -> {
            if (item != null) {
                currentItem = item;
                populateUI(item);
            }
        });
    }

    private void populateUI(CustomItemEntity item) {
        ((TextView) findViewById(R.id.tv_item_name)).setText(item.name);
        ((TextView) findViewById(R.id.tv_item_category)).setText(
                item.categoryName != null ? item.categoryName : "Misc");
        ((TextView) findViewById(R.id.tv_item_desc)).setText(
                !TextUtils.isEmpty(item.desc) ? item.desc : "No description.");

        TextView tvRarity = findViewById(R.id.tv_item_rarity);
        if (!TextUtils.isEmpty(item.rarityName)) {
            tvRarity.setText("Rarity: " + item.rarityName);
            tvRarity.setVisibility(View.VISIBLE);
        } else {
            tvRarity.setVisibility(View.GONE);
        }

        TextView tvMagic = findViewById(R.id.tv_magic);
        tvMagic.setText(item.isMagicItem ? "Magic Item" : "Non-magical");
        tvMagic.setVisibility(View.VISIBLE);

        TextView tvWeight = findViewById(R.id.tv_weight);
        tvWeight.setText("Weight: " + item.weight + " " + item.weightUnit);
        tvWeight.setVisibility(View.VISIBLE);

        TextView tvCost = findViewById(R.id.tv_cost);
        tvCost.setText("Cost: " + item.cost + " gp");
        tvCost.setVisibility(View.VISIBLE);

        TextView tvAttune = findViewById(R.id.tv_attunement);
        if (item.requiresAttunement) {
            String detail = !TextUtils.isEmpty(item.attunementDetail) ? ": " + item.attunementDetail : "";
            tvAttune.setText("Requires Attunement" + detail);
            tvAttune.setVisibility(View.VISIBLE);
        } else {
            tvAttune.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(item.weaponJson)) {
            LinearLayout weaponSection = findViewById(R.id.weapon_section);
            weaponSection.setVisibility(View.VISIBLE);
            try {
                ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(
                        item.weaponJson, ItemDto.WeaponEmbedDto.class);
                if (weapon != null) {
                    String damage = (weapon.damageDice != null ? weapon.damageDice : "—") + " " +
                            (weapon.damageType != null ? weapon.damageType.name : "");
                    ((TextView) findViewById(R.id.tv_weapon_damage)).setText("Damage: " + damage.trim());
                    String rangeText = weapon.range > 0 ? (int) weapon.range + "/" + (int) weapon.longRange + " ft." : "Melee";
                    ((TextView) findViewById(R.id.tv_weapon_range)).setText("Range: " + rangeText);
                    ((TextView) findViewById(R.id.tv_weapon_type)).setText(weapon.isSimple ? "Simple" : "Martial");
                }
            } catch (Exception e) {
            }
        } else {
            findViewById(R.id.weapon_section).setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(item.armorJson)) {
            LinearLayout armorSection = findViewById(R.id.armor_section);
            armorSection.setVisibility(View.VISIBLE);
            try {
                ItemDto.ArmorEmbedDto armor = new Gson().fromJson(
                        item.armorJson, ItemDto.ArmorEmbedDto.class);
                if (armor != null) {
                    ((TextView) findViewById(R.id.tv_armor_ac)).setText("AC: " + armor.acDisplay);
                    if (armor.grantsStealthDisadvantage) {
                        TextView tvStealth = findViewById(R.id.tv_armor_stealth);
                        tvStealth.setText("Stealth Disadvantage");
                        tvStealth.setVisibility(View.VISIBLE);
                    }
                    if (armor.strengthScoreRequired != null && armor.strengthScoreRequired > 0) {
                        TextView tvStr = findViewById(R.id.tv_armor_str);
                        tvStr.setText("Strength Required: " + armor.strengthScoreRequired);
                        tvStr.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
            }
        } else {
            findViewById(R.id.armor_section).setVisibility(View.GONE);
        }


        Button btnManage = findViewById(R.id.btnManage);
        btnManage.setVisibility(View.VISIBLE);
        btnManage.setOnClickListener(v -> showManageMenu(v, itemId));
    }

    private void showManageMenu(View anchor, long id) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent i = new Intent(this, CustomItemCreateActivity.class);
                i.putExtra(CustomItemCreateActivity.EXTRA_EDIT_ID, id);
                startActivity(i);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Item")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("Delete", (d, w) -> {
                            UserContentDatabase.getInstance(this).getQueryExecutor()
                                    .execute(() -> {
                                        customItemDao.delete(id);
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