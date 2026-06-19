package com.fizzycoyote.qusetroll.feature_item.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemDto;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;
import com.fizzycoyote.qusetroll.feature_item.weapon_property.ui.WeaponPropertyDetailActivity;
import com.google.gson.Gson;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;

public class ItemDetailActivity extends BaseActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .build();

        String key = getIntent().getStringExtra("ITEM_KEY");
        Open5eDatabase.getInstance(this).itemDao()
                .getByKey(key).observe(this, item -> {
                    if (item != null) populateUI(item);
                });
    }

    private void populateUI(ItemEntity item) {
        ((TextView) findViewById(R.id.tv_item_name)).setText(item.name);
        ((TextView) findViewById(R.id.tv_item_category)).setText(item.categoryName != null ? item.categoryName : "Misc");
        TextView tvDesc = findViewById(R.id.tv_item_desc);
        markwon.setMarkdown(tvDesc, item.desc != null ? item.desc : "No description.");

        TextView tvRarity = findViewById(R.id.tv_item_rarity);
        if (item.rarityName != null && !item.rarityName.isEmpty()) {
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

        if (item.requiresAttunement) {
            TextView tvAttune = findViewById(R.id.tv_attunement);
            tvAttune.setText("Requires Attunement" + (item.attunementDetail != null ? ": " + item.attunementDetail : ""));
            tvAttune.setVisibility(View.VISIBLE);
        }

        TextView tvSource = findViewById(R.id.tv_source);
        String sourceText = "Source: " + (item.documentName != null ? item.documentName : "Unknown");
        tvSource.setText(sourceText);
        tvSource.setVisibility(View.VISIBLE);
        tvSource.setClickable(true);
        tvSource.setFocusable(true);
        tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        tvSource.setOnClickListener(v -> {
            if (item.documentKey != null && !item.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(item.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });

        LinearLayout weaponSection = findViewById(R.id.weapon_section);
        if (item.weaponJson != null && !item.weaponJson.isEmpty()) {
            weaponSection.setVisibility(View.VISIBLE);
            try {
                ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                ((TextView) findViewById(R.id.tv_weapon_name)).setText(weapon.name);
                ((TextView) findViewById(R.id.tv_weapon_damage)).setText("Damage: " + weapon.damageDice + " " +
                        (weapon.damageType != null ? weapon.damageType.name : ""));
                ((TextView) findViewById(R.id.tv_weapon_range)).setText("Range: " +
                        (weapon.range > 0 ? (int) weapon.range + "/" + (int) weapon.longRange + " ft." : "Melee"));
                ((TextView) findViewById(R.id.tv_weapon_type)).setText(weapon.isSimple ? "Simple" : "Martial");

                String firstPropertyName = null;
                if (weapon.properties != null && !weapon.properties.isEmpty()) {
                    ItemDto.WeaponEmbedDto.WeaponPropertyDto firstProp = weapon.properties.get(0);
                    if (firstProp.property != null) {
                        firstPropertyName = firstProp.property.name;
                    }
                }
                final String finalName = firstPropertyName;
                weaponSection.setOnClickListener(v -> {
                    if (finalName != null) {
                        Intent intent = new Intent(this, WeaponPropertyDetailActivity.class);
                        intent.putExtra("PROPERTY_NAME", finalName);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "No weapon properties available", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
            }
        } else {
            weaponSection.setVisibility(View.GONE);
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
