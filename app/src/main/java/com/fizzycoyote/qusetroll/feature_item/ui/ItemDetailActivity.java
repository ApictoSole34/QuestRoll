package com.fizzycoyote.qusetroll.feature_item.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;
import com.google.gson.Gson;

import io.noties.markwon.Markwon;

public class ItemDetailActivity extends AppCompatActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("ITEM_KEY");
        Open5eDatabase.getInstance(this).itemDao()
                .getByKey(key).observe(this, item -> {
                    if (item != null) populateUI(item);
                });
    }

    private void populateUI(ItemEntity item) {
        ((TextView) findViewById(R.id.tv_item_name)).setText(item.name);
        ((TextView) findViewById(R.id.tv_item_category)).setText(item.categoryName != null ? item.categoryName : "Misc");
        ((TextView) findViewById(R.id.tv_item_desc)).setText(item.desc != null ? item.desc : "No description.");

        TextView tvRarity = findViewById(R.id.tv_item_rarity);
        if (item.rarityName != null && !item.rarityName.isEmpty()) {
            tvRarity.setText("Rarity: " + item.rarityName);
            tvRarity.setVisibility(View.VISIBLE);
        } else tvRarity.setVisibility(View.GONE);

        TextView tvMagic = findViewById(R.id.tv_magic);
        tvMagic.setText(item.isMagicItem ? "Magic Item" : "Non-magical");
        tvMagic.setVisibility(View.VISIBLE);

        TextView tvWeight = findViewById(R.id.tv_weight);
        tvWeight.setText("Weight: " + item.weight + " " + item.weightUnit);
        tvWeight.setVisibility(View.VISIBLE);

        TextView tvCost = findViewById(R.id.tv_cost);
        tvCost.setText("Cost: " + item.cost + " gp");
        tvCost.setVisibility(View.VISIBLE);

        if (item.requiresAttunement) {
            TextView tvAttune = findViewById(R.id.tv_attunement);
            tvAttune.setText("Requires Attunement" + (item.attunementDetail != null ? ": " + item.attunementDetail : ""));
            tvAttune.setVisibility(View.VISIBLE);
        }

        TextView tvSource = findViewById(R.id.tv_source);
        tvSource.setText("Source: " + (item.documentName != null ? item.documentName : "Unknown"));
        tvSource.setVisibility(View.VISIBLE);

        if (item.weaponJson != null && !item.weaponJson.isEmpty()) {
            LinearLayout weaponSection = findViewById(R.id.weapon_section);
            weaponSection.setVisibility(View.VISIBLE);
            try {
                ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                ((TextView) findViewById(R.id.tv_weapon_damage)).setText("Damage: " + weapon.damageDice + " " +
                        (weapon.damageType != null ? weapon.damageType.name : ""));
                ((TextView) findViewById(R.id.tv_weapon_range)).setText("Range: " +
                        (weapon.range > 0 ? (int)weapon.range + "/" + (int)weapon.longRange + " ft." : "Melee"));
                ((TextView) findViewById(R.id.tv_weapon_type)).setText(weapon.isSimple ? "Simple" : "Martial");
            } catch (Exception e) {}
        }

        if (item.armorJson != null && !item.armorJson.isEmpty()) {
            LinearLayout armorSection = findViewById(R.id.armor_section);
            armorSection.setVisibility(View.VISIBLE);
            try {
                ItemDto.ArmorEmbedDto armor = new Gson().fromJson(item.armorJson, ItemDto.ArmorEmbedDto.class);
                ((TextView) findViewById(R.id.tv_armor_ac)).setText("AC: " + armor.acDisplay);
                if (armor.grantsStealthDisadvantage) {
                    ((TextView) findViewById(R.id.tv_armor_stealth)).setText("Stealth Disadvantage");
                    ((TextView) findViewById(R.id.tv_armor_stealth)).setVisibility(View.VISIBLE);
                }
                if (armor.strengthScoreRequired != null) {
                    ((TextView) findViewById(R.id.tv_armor_str)).setText("Strength Required: " + armor.strengthScoreRequired);
                    ((TextView) findViewById(R.id.tv_armor_str)).setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {}
        }
    }
}
