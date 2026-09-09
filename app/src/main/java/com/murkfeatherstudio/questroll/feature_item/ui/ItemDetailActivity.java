package com.murkfeatherstudio.questroll.feature_item.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemDto;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityItemDetailBinding;
import com.murkfeatherstudio.questroll.feature_item.weapon_property.ui.WeaponPropertyDetailActivity;
import com.google.gson.Gson;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;

public class ItemDetailActivity extends BaseActivity {

    private Markwon markwon;
    private ActivityItemDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
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
        binding.tvItemName.setText(item.name);
        binding.tvItemCategory.setText(item.categoryName != null ? item.categoryName : "Misc");
        markwon.setMarkdown(binding.tvItemDesc, item.desc != null ? item.desc : "No description.");

        if (item.rarityName != null && !item.rarityName.isEmpty()) {
            binding.tvItemRarity.setText("Rarity: " + item.rarityName);
            binding.tvItemRarity.setVisibility(View.VISIBLE);
        } else {
            binding.tvItemRarity.setVisibility(View.GONE);
        }

        binding.tvMagic.setText(item.isMagicItem ? "Magic Item" : "Non-magical");
        binding.tvMagic.setVisibility(View.VISIBLE);

        binding.tvWeight.setText("Weight: " + item.weight + " " + item.weightUnit);
        binding.tvWeight.setVisibility(View.VISIBLE);
        binding.tvCost.setText("Cost: " + item.cost + " gp");
        binding.tvCost.setVisibility(View.VISIBLE);

        if (item.requiresAttunement) {
            binding.tvAttunement.setText("Requires Attunement" + (item.attunementDetail != null ? ": " + item.attunementDetail : ""));
            binding.tvAttunement.setVisibility(View.VISIBLE);
        } else {
            binding.tvAttunement.setVisibility(View.GONE);
        }

        String sourceText = "Source: " + (item.documentName != null ? item.documentName : "Unknown");
        binding.tvSource.setText(sourceText);
        binding.tvSource.setVisibility(View.VISIBLE);
        binding.tvSource.setClickable(true);
        binding.tvSource.setFocusable(true);
        binding.tvSource.setBackgroundResource(android.R.drawable.list_selector_background);
        binding.tvSource.setOnClickListener(v -> {
            if (item.documentKey != null && !item.documentKey.isEmpty()) {
                DocumentDetailDialogFragment fragment = DocumentDetailDialogFragment.newInstance(item.documentKey);
                fragment.show(getSupportFragmentManager(), "document_detail");
            }
        });

        if (item.weaponJson != null && !item.weaponJson.isEmpty()) {
            binding.weaponSection.setVisibility(View.VISIBLE);
            try {
                ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                
                binding.tvWeaponName.setText(weapon.name);
                binding.tvWeaponDamage.setText("Damage: " + weapon.damageDice + " " +
                        (weapon.damageType != null ? weapon.damageType.name : ""));
                binding.tvWeaponRange.setText("Range: " +
                        (weapon.range > 0 ? (int) weapon.range + "/" + (int) weapon.longRange + " ft." : "Melee"));
                binding.tvWeaponType.setText(weapon.isSimple ? "Simple" : "Martial");

                String firstPropertyName = null;
                if (weapon.properties != null && !weapon.properties.isEmpty()) {
                    ItemDto.WeaponEmbedDto.WeaponPropertyDto firstProp = weapon.properties.get(0);
                    if (firstProp.property != null) {
                        firstPropertyName = firstProp.property.name;
                    }
                }
                final String finalName = firstPropertyName;
                binding.weaponSection.setOnClickListener(v -> {
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
            binding.weaponSection.setVisibility(View.GONE);
        }

        if (item.armorJson != null && !item.armorJson.isEmpty()) {
            binding.armorSection.setVisibility(View.VISIBLE);
            try {
                ItemDto.ArmorEmbedDto armor = new Gson().fromJson(item.armorJson, ItemDto.ArmorEmbedDto.class);
                
                binding.tvArmorAc.setText("AC: " + armor.acDisplay);
                if (armor.grantsStealthDisadvantage) {
                    binding.tvArmorStealth.setText("Stealth Disadvantage");
                    binding.tvArmorStealth.setVisibility(View.VISIBLE);
                } else {
                    binding.tvArmorStealth.setVisibility(View.GONE);
                }
                
                if (armor.strengthScoreRequired != null) {
                    binding.tvArmorStr.setText("Strength Required: " + armor.strengthScoreRequired);
                    binding.tvArmorStr.setVisibility(View.VISIBLE);
                } else {
                    binding.tvArmorStr.setVisibility(View.GONE);
                }
            } catch (Exception e) {}
        } else {
            binding.armorSection.setVisibility(View.GONE);
        }
    }
}
