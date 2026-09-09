package com.murkfeatherstudio.questroll.feature_item.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemDto;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomItemDetailBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.concurrent.Executors;

public class CustomItemDetailActivity extends BaseActivity {

    private ActivityCustomItemDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long itemId = getIntent().getLongExtra("CUSTOM_ITEM_ID", -1);
        if (itemId == -1) {
            finish();
            return;
        }

        UserContentDatabase.getInstance(this).customItemDao().getById(itemId)
                .observe(this, item -> {
                    if (item != null) populateUI(item);
                });
    }

    /**
     * NOTE: weapon_properties_container is managed dynamically (addView()).
     * TextView views for individual weapon properties are created at runtime
     * based on JSON data. ViewBinding does not apply to these
     * dynamically generated children.
     */
    private void populateUI(com.murkfeatherstudio.questroll.core.models.custom.custom_item.CustomItemEntity item) {
        binding.tvItemName.setText(item.name);
        binding.tvItemCategory.setText(item.categoryName != null ? item.categoryName : "Custom Item");
        binding.tvItemDesc.setText(item.desc != null ? item.desc : "No description.");

        if (item.rarityName != null && !item.rarityName.isEmpty()) {
            binding.tvItemRarity.setText("Rarity: " + item.rarityName);
            binding.tvItemRarity.setVisibility(View.VISIBLE);
        } else {
            binding.tvItemRarity.setVisibility(View.GONE);
        }

        binding.tvMagic.setText(item.isMagicItem ? "Magic Item" : "Non-magical");
        binding.tvMagic.setVisibility(View.VISIBLE);

        binding.tvWeight.setText("Weight: " + item.weight + " lb");
        binding.tvWeight.setVisibility(View.VISIBLE);
        binding.tvCost.setText("Cost: " + item.cost + " gp");
        binding.tvCost.setVisibility(View.VISIBLE);

        if (item.requiresAttunement) {
            binding.tvAttunement.setText("Requires Attunement" + (item.attunementDetail != null ? ": " + item.attunementDetail : ""));
            binding.tvAttunement.setVisibility(View.VISIBLE);
        } else {
            binding.tvAttunement.setVisibility(View.GONE);
        }

        if (item.categoryName != null && item.categoryName.equalsIgnoreCase("Weapon") && item.weaponJson != null) {
            binding.weaponSection.setVisibility(View.VISIBLE);
            try {
                ItemDto.WeaponEmbedDto weapon = new Gson().fromJson(item.weaponJson, ItemDto.WeaponEmbedDto.class);
                
                binding.tvWeaponRange.setText("Range: " + (weapon.range > 0 ? (int) weapon.range + "/" + (int) weapon.longRange + " ft." : "Melee"));
                binding.tvWeaponType.setText(weapon.isSimple ? "Simple" : "Martial");
                binding.tvWeaponDamage.setText("Damage: " + (weapon.damageDice != null ? weapon.damageDice : "") + " " + (weapon.damageType != null ? weapon.damageType.name : ""));

                binding.weaponPropertiesContainer.removeAllViews();
                if (weapon.properties != null) {
                    for (ItemDto.WeaponEmbedDto.WeaponPropertyDto wp : weapon.properties) {
                        if (wp.property != null && wp.property.name != null) {
                            TextView ptv = new TextView(this);
                            ptv.setText("• " + wp.property.name);
                            ptv.setPadding(0, 4, 0, 4);
                            binding.weaponPropertiesContainer.addView(ptv);
                        }
                    }
                }
            } catch (Exception ignored) {}
        } else {
            binding.weaponSection.setVisibility(View.GONE);
        }

        if (item.categoryName != null && item.categoryName.equalsIgnoreCase("Armor") && item.armorJson != null) {
            binding.armorSection.setVisibility(View.VISIBLE);
            try {
                ItemDto.ArmorEmbedDto armor = new Gson().fromJson(item.armorJson, ItemDto.ArmorEmbedDto.class);
                
                binding.tvArmorAc.setText("AC: " + armor.acDisplay);
                binding.tvArmorStealth.setVisibility(armor.grantsStealthDisadvantage ? View.VISIBLE : View.GONE);
                if (armor.strengthScoreRequired != null) {
                    binding.tvArmorStr.setText("Strength Required: " + armor.strengthScoreRequired);
                    binding.tvArmorStr.setVisibility(View.VISIBLE);
                } else {
                    binding.tvArmorStr.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {}
        } else {
            binding.armorSection.setVisibility(View.GONE);
        }

        binding.btnManage.setVisibility(View.VISIBLE);
        binding.btnManage.setOnClickListener(v -> showManageOptions(item));
    }

    private void showManageOptions(com.murkfeatherstudio.questroll.core.models.custom.custom_item.CustomItemEntity item) {
        String[] options = {"Edit", "Delete"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Manage Item")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent i = new Intent(this, CustomItemCreateActivity.class);
                        i.putExtra(CustomItemCreateActivity.EXTRA_EDIT_ID, item.id);
                        startActivity(i);
                    } else {
                        deleteItem(item);
                    }
                })
                .show();
    }

    private void deleteItem(com.murkfeatherstudio.questroll.core.models.custom.custom_item.CustomItemEntity item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete \"" + item.name + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        UserContentDatabase.getInstance(this).customItemDao().delete(item.id);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
