package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterWithRelations;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentCampaignEquipmentBinding;
import com.murkfeatherstudio.questroll.databinding.DialogQuickGoldBinding;
import com.murkfeatherstudio.questroll.feature_campaign.adapter.InventoryItemAdapter;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.CampaignDetailViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Equipment tab of the campaign character sheet.
 */
public class CampaignEquipmentFragment extends Fragment {

    private static final String ARG_CAMPAIGN_ID = "campaign_id";

    private static final String[] SLOT_KEYS = {
            "body", "main_hand", "off_hand", "head", "neck",
            "cloak", "hands", "ring_left", "ring_right", "waist", "feet"
    };
    private static final String[] SLOT_LABELS = {
            "Armor", "Main Hand", "Off Hand", "Head", "Neck",
            "Cloak", "Gloves", "Ring L", "Ring R", "Waist", "Boots"
    };

    private long campaignId;
    private CampaignDetailViewModel viewModel;
    private InventoryItemAdapter inventoryAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FragmentCampaignEquipmentBinding binding;

    private final Map<String, TextView> slotTextViews = new HashMap<>();
    private final Map<String, View> slotRows = new HashMap<>();
    private final Map<String, ImageButton> slotInfoButtons = new HashMap<>();
    private final Map<String, InventoryItemEntity> equippedItems = new HashMap<>();

    public static CampaignEquipmentFragment newInstance(long campaignId) {
        Bundle args = new Bundle();
        args.putLong(ARG_CAMPAIGN_ID, campaignId);
        CampaignEquipmentFragment f = new CampaignEquipmentFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) campaignId = getArguments().getLong(ARG_CAMPAIGN_ID);
        viewModel = new ViewModelProvider(requireActivity()).get(CampaignDetailViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCampaignEquipmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initSlotMappings();
        setupRecyclerView();

        binding.layoutGoldClick.setOnClickListener(v -> showQuickGoldDialog());
        binding.btnAddItemInline.setOnClickListener(v -> openShopDialog());

        viewModel.characterWithRelations.observe(getViewLifecycleOwner(), this::onInventoryChanged);
        viewModel.getEffectiveAc().observe(getViewLifecycleOwner(), ac -> {
            binding.tvAcValue.setText("AC: " + (ac != null ? ac : "—"));
        });
    }

    /**
     * JAVADOC: This method manually maps View Binding fields to helper Maps.
     * Traditional loop with findViewById(resId) was replaced with direct binding references 
     * to ensure type safety and better performance, while still allowing the fragment 
     * to manage equipment slots generically via keys.
     */
    private void initSlotMappings() {
        slotRows.put("body", binding.rowSlotBody);
        slotTextViews.put("body", binding.tvSlotBody);
        slotInfoButtons.put("body", binding.btnSlotBodyInfo);

        slotRows.put("main_hand", binding.rowSlotMainHand);
        slotTextViews.put("main_hand", binding.tvSlotMainHand);
        slotInfoButtons.put("main_hand", binding.btnSlotMainHandInfo);

        slotRows.put("off_hand", binding.rowSlotOffHand);
        slotTextViews.put("off_hand", binding.tvSlotOffHand);
        slotInfoButtons.put("off_hand", binding.btnSlotOffHandInfo);

        slotRows.put("head", binding.rowSlotHead);
        slotTextViews.put("head", binding.tvSlotHead);
        slotInfoButtons.put("head", binding.btnSlotHeadInfo);

        slotRows.put("neck", binding.rowSlotNeck);
        slotTextViews.put("neck", binding.tvSlotNeck);
        slotInfoButtons.put("neck", binding.btnSlotNeckInfo);

        slotRows.put("cloak", binding.rowSlotCloak);
        slotTextViews.put("cloak", binding.tvSlotCloak);
        slotInfoButtons.put("cloak", binding.btnSlotCloakInfo);

        slotRows.put("hands", binding.rowSlotHands);
        slotTextViews.put("hands", binding.tvSlotHands);
        slotInfoButtons.put("hands", binding.btnSlotHandsInfo);

        slotRows.put("ring_left", binding.rowSlotRingLeft);
        slotTextViews.put("ring_left", binding.tvSlotRingLeft);
        slotInfoButtons.put("ring_left", binding.btnSlotRingLeftInfo);

        slotRows.put("ring_right", binding.rowSlotRingRight);
        slotTextViews.put("ring_right", binding.tvSlotRingRight);
        slotInfoButtons.put("ring_right", binding.btnSlotRingRightInfo);

        slotRows.put("waist", binding.rowSlotWaist);
        slotTextViews.put("waist", binding.tvSlotWaist);
        slotInfoButtons.put("waist", binding.btnSlotWaistInfo);

        slotRows.put("feet", binding.rowSlotFeet);
        slotTextViews.put("feet", binding.tvSlotFeet);
        slotInfoButtons.put("feet", binding.btnSlotFeetInfo);
    }

    private void setupRecyclerView() {
        binding.rvInventory.setLayoutManager(new LinearLayoutManager(requireContext()));
        inventoryAdapter = new InventoryItemAdapter(new InventoryItemAdapter.OnItemClickListener() {
            @Override public void onItemClick(InventoryItemEntity item) { showItemDialog(item); }
            @Override public void onItemDetailsClick(InventoryItemEntity item) { showItemDetailsDialog(item); }
        });
        binding.rvInventory.setAdapter(inventoryAdapter);
    }

    private void onInventoryChanged(CharacterWithRelations cwr) {
        if (cwr == null || cwr.character == null) {
            binding.tvNoCharacter.setVisibility(View.VISIBLE);
            binding.cardGold.setVisibility(View.GONE);
            binding.cardSlots.setVisibility(View.GONE);
            binding.cardBackpack.setVisibility(View.GONE);
            return;
        }

        binding.tvNoCharacter.setVisibility(View.GONE);
        binding.cardGold.setVisibility(View.VISIBLE);
        binding.cardSlots.setVisibility(View.VISIBLE);
        binding.cardBackpack.setVisibility(View.VISIBLE);

        binding.tvGold.setText(String.format("%.1f gp", cwr.character.currentGold));
        List<InventoryItemEntity> inventory = cwr.inventory != null ? cwr.inventory : new ArrayList<>();
        inventoryAdapter.setItems(inventory);
        binding.tvEmptyInventory.setVisibility(inventory.isEmpty() ? View.VISIBLE : View.GONE);

        updateSlots(inventory);
        updateTotalWeight(inventory);
    }

    private void updateSlots(List<InventoryItemEntity> inventory) {
        equippedItems.clear();
        for (InventoryItemEntity item : inventory) {
            if (item.isEquipped && item.slot != null) {
                equippedItems.put(item.slot, item);
            }
        }

        for (String slotKey : SLOT_KEYS) {
            InventoryItemEntity item = equippedItems.get(slotKey);
            TextView tv = slotTextViews.get(slotKey);
            View row = slotRows.get(slotKey);
            ImageButton infoBtn = slotInfoButtons.get(slotKey);

            if (item != null) {
                tv.setText(item.customName);
                infoBtn.setVisibility(View.VISIBLE);
                row.setOnClickListener(v -> showItemDialog(item));
                infoBtn.setOnClickListener(v -> showItemDetailsDialog(item));
            } else {
                tv.setText("—");
                infoBtn.setVisibility(View.GONE);
                row.setOnClickListener(null);
            }
        }
    }

    private void updateTotalWeight(List<InventoryItemEntity> inventory) {
        float total = 0;
        for (InventoryItemEntity item : inventory) {
            total += (item.customWeight * item.quantity);
        }
        if (binding != null) {
            binding.tvTotalWeight.setText(String.format("Total Weight: %.1f lb", total));
        }
    }

    private void showQuickGoldDialog() {
        DialogQuickGoldBinding dialogBinding = DialogQuickGoldBinding.inflate(getLayoutInflater());

        final boolean[] isAddition = {true};

        dialogBinding.tvGoldSign.setOnClickListener(v -> {
            isAddition[0] = !isAddition[0];
            dialogBinding.tvGoldSign.setText(isAddition[0] ? "+" : "−");
            dialogBinding.tvGoldSign.setTextColor(isAddition[0] ? 0xFF388E3C : 0xFFD32F2F);
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Adjust Gold")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Apply", (d, w) -> {
                    try {
                        float amount = Float.parseFloat(dialogBinding.etGoldAmount.getText().toString().trim());
                        if (!isAddition[0]) amount = -amount;
                        viewModel.adjustGold(amount);
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showItemDialog(InventoryItemEntity item) {
        String[] options = item.isEquipped
                ? new String[]{"Change Slot", "Unequip", "Delete"}
                : new String[]{"Equip", "Delete"};

        new AlertDialog.Builder(requireContext())
                .setTitle(item.customName != null ? item.customName : "Item")
                .setItems(options, (dialog, which) -> {
                    String chosen = options[which];
                    if (chosen.equals("Equip") || chosen.equals("Change Slot")) {
                        showSlotPickerDialog(item);
                    } else if (chosen.equals("Unequip")) {
                        viewModel.toggleEquipItem(item.id, false, null);
                    } else if (chosen.equals("Delete")) {
                        confirmDeleteItem(item);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSlotPickerDialog(InventoryItemEntity item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Choose slot")
                .setItems(SLOT_LABELS, (dialog, which) ->
                        viewModel.toggleEquipItem(item.id, true, SLOT_KEYS[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteItem(InventoryItemEntity item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete item")
                .setMessage("Remove \"" + item.customName + "\" from inventory?")
                .setPositiveButton("Delete", (d, w) -> viewModel.removeItem(item.id))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showItemDetailsDialog(InventoryItemEntity item) {
        if (item.itemKey != null && !item.itemKey.isEmpty()) {
            executor.execute(() -> {
                ItemEntity full = Open5eDatabase.getInstance(requireContext()).itemDao().getByKeySync(item.itemKey);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> renderItemDetailsDialog(item, full));
            });
        } else {
            renderItemDetailsDialog(item, null);
        }
    }

    private void renderItemDetailsDialog(InventoryItemEntity invItem, ItemEntity full) {
        StringBuilder sb = new StringBuilder();
        sb.append("Quantity: ").append(invItem.quantity).append("\n");
        sb.append("Weight (unit): ").append(invItem.customWeight).append(" lb\n");
        sb.append("Value (unit): ").append(invItem.customCost).append(" gp\n");

        if (full != null) {
            if (full.desc != null && !full.desc.isEmpty()) sb.append("\n").append(full.desc).append("\n");
            int acBase = extractAcBase(full.armorJson);
            if (acBase > 0) sb.append("\nBase AC: ").append(acBase);
        } else if (invItem.customDescription != null) {
            sb.append("\n").append(invItem.customDescription);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(invItem.customName != null ? invItem.customName : "Item")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private int extractAcBase(String armorJson) {
        if (armorJson == null || armorJson.isEmpty()) return 0;
        try {
            return new JSONObject(armorJson).optInt("ac_base", 0);
        } catch (JSONException e) {
            return 0;
        }
    }

    private void openShopDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Add item")
                .setItems(new String[]{"Buy", "Get / Loot"},
                        (dialog, which) -> showCatalogItemPickerDialog(which == 0))
                .show();
    }

    private void showCatalogItemPickerDialog(boolean isBuy) {
        com.murkfeatherstudio.questroll.databinding.DialogItemShopBinding shopBinding = com.murkfeatherstudio.questroll.databinding.DialogItemShopBinding.inflate(getLayoutInflater());
        shopBinding.rvShopItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isBuy ? "Buy item" : "Add loot")
                .setView(shopBinding.getRoot())
                .setNegativeButton("Cancel", null)
                .create();

        ShopItemAdapter adapter = new ShopItemAdapter(isBuy, chosen -> {
            dialog.dismiss();
            promptQuantityAndAdd(chosen, isBuy);
        });
        shopBinding.rvShopItems.setAdapter(adapter);

        executor.execute(() -> {
            String gameSystem = viewModel.getCurrentGameSystem();
            List<ItemEntity> items = Open5eDatabase.getInstance(requireContext()).itemDao().getAllByGameSystem(gameSystem);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                shopBinding.tvShopLoading.setVisibility(View.GONE);
                adapter.setAllItems(items);
            });
        });

        shopBinding.etShopSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { adapter.filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void promptQuantityAndAdd(ItemEntity chosen, boolean isBuy) {
        EditText etQty = new EditText(requireContext());
        etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        etQty.setHint("Quantity");
        etQty.setText("1");

        new AlertDialog.Builder(requireContext())
                .setTitle(chosen.name)
                .setMessage(isBuy
                        ? String.format("Price per unit: %.1f gp", chosen.cost)
                        : "Adding for free (loot / from DM)")
                .setView(etQty)
                .setPositiveButton(isBuy ? "Buy" : "Add", (d, w) -> {
                    int qty;
                    try {
                        qty = Math.max(1, Integer.parseInt(etQty.getText().toString().trim()));
                    } catch (Exception e) {
                        qty = 1;
                    }

                    InventoryItemEntity inv = new InventoryItemEntity();
                    inv.itemKey = chosen.key;
                    inv.customName = chosen.name;
                    inv.customDescription = chosen.desc;
                    inv.customWeight = chosen.weight;
                    inv.customCost = chosen.cost;
                    inv.quantity = qty;
                    inv.isEquipped = false;

                    if (isBuy) {
                        float totalCost = chosen.cost * qty;
                        CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
                        if (cwr == null || cwr.character == null || cwr.character.currentGold < totalCost) {
                            Toast.makeText(getContext(), "Not enough gold!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        viewModel.buyItem(inv, totalCost);
                    } else {
                        viewModel.getItem(inv);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.VH> {
        interface OnPick { void onPick(ItemEntity item); }

        private final boolean isBuy;
        private final OnPick onPick;
        private List<ItemEntity> all = new ArrayList<>();
        private List<ItemEntity> shown = new ArrayList<>();

        ShopItemAdapter(boolean isBuy, OnPick onPick) {
            this.isBuy = isBuy;
            this.onPick = onPick;
        }

        void setAllItems(List<ItemEntity> items) {
            all = items;
            shown = new ArrayList<>(items);
            notifyDataSetChanged();
        }

        void filter(String query) {
            shown.clear();
            if (query == null || query.isEmpty()) {
                shown.addAll(all);
            } else {
                String lower = query.toLowerCase();
                for (ItemEntity i : all) {
                    if (i.name != null && i.name.toLowerCase().contains(lower)) shown.add(i);
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            com.murkfeatherstudio.questroll.databinding.ItemSearchRowBinding itemBinding = 
                com.murkfeatherstudio.questroll.databinding.ItemSearchRowBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            ItemEntity item = shown.get(pos);
            h.itemBinding.itemName.setText(item.name);
            h.itemBinding.itemDesc.setText(isBuy
                    ? String.format("%.1f gp • %.1f lb", item.cost, item.weight)
                    : String.format("%.1f lb", item.weight));
            h.itemBinding.selectButton.setText(isBuy ? "Buy" : "Get");
            h.itemBinding.selectButton.setOnClickListener(v -> onPick.onPick(item));
        }

        @Override
        public int getItemCount() { return shown.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final com.murkfeatherstudio.questroll.databinding.ItemSearchRowBinding itemBinding;

            VH(com.murkfeatherstudio.questroll.databinding.ItemSearchRowBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
