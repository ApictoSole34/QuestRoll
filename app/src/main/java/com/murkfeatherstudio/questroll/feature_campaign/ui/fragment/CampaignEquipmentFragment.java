package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
 * Equipment tab of the campaign character sheet: shows the assigned character's gold,
 * effective AC and inventory, and lets the player buy items, receive loot from the DM,
 * manage what's equipped in which slot, and inspect item details.
 */
public class CampaignEquipmentFragment extends Fragment {

    private static final String ARG_CAMPAIGN_ID = "campaign_id";

    /** Slot keys understood by {@link InventoryItemAdapter} / equip UI, in display order. */
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

    private TextView tvNoCharacter, tvEmptyInventory, tvAcValue, tvGold, tvTotalWeight;
    private View cardSlots, cardBackpack, cardGold;
    
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
        return inflater.inflate(R.layout.fragment_campaign_equipment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupRecyclerView(view);

        view.findViewById(R.id.layout_gold_click).setOnClickListener(v -> showQuickGoldDialog());
        view.findViewById(R.id.btn_add_item_inline).setOnClickListener(v -> openShopDialog());

        viewModel.characterWithRelations.observe(getViewLifecycleOwner(), this::onInventoryChanged);
        viewModel.getEffectiveAc().observe(getViewLifecycleOwner(), ac -> {
            tvAcValue.setText("AC: " + (ac != null ? ac : "—"));
        });
    }

    private void bindViews(View v) {
        tvNoCharacter = v.findViewById(R.id.tv_no_character);
        cardGold = v.findViewById(R.id.card_gold);
        cardSlots = v.findViewById(R.id.card_slots);
        cardBackpack = v.findViewById(R.id.card_backpack);
        tvGold = v.findViewById(R.id.tv_gold);
        tvAcValue = v.findViewById(R.id.tv_ac_value);
        tvEmptyInventory = v.findViewById(R.id.tv_empty_inventory);
        tvTotalWeight = v.findViewById(R.id.tv_total_weight);

        // Bind slots
        bindSlot(v, "body", R.id.row_slot_body, R.id.tv_slot_body, R.id.btn_slot_body_info);
        bindSlot(v, "main_hand", R.id.row_slot_main_hand, R.id.tv_slot_main_hand, R.id.btn_slot_main_hand_info);
        bindSlot(v, "off_hand", R.id.row_slot_off_hand, R.id.tv_slot_off_hand, R.id.btn_slot_off_hand_info);
        bindSlot(v, "head", R.id.row_slot_head, R.id.tv_slot_head, R.id.btn_slot_head_info);
        bindSlot(v, "neck", R.id.row_slot_neck, R.id.tv_slot_neck, R.id.btn_slot_neck_info);
        bindSlot(v, "cloak", R.id.row_slot_cloak, R.id.tv_slot_cloak, R.id.btn_slot_cloak_info);
        bindSlot(v, "hands", R.id.row_slot_hands, R.id.tv_slot_hands, R.id.btn_slot_hands_info);
        bindSlot(v, "ring_left", R.id.row_slot_ring_left, R.id.tv_slot_ring_left, R.id.btn_slot_ring_left_info);
        bindSlot(v, "ring_right", R.id.row_slot_ring_right, R.id.tv_slot_ring_right, R.id.btn_slot_ring_right_info);
        bindSlot(v, "waist", R.id.row_slot_waist, R.id.tv_slot_waist, R.id.btn_slot_waist_info);
        bindSlot(v, "feet", R.id.row_slot_feet, R.id.tv_slot_feet, R.id.btn_slot_feet_info);
    }

    private void bindSlot(View v, String key, int rowId, int textId, int infoBtnId) {
        slotRows.put(key, v.findViewById(rowId));
        slotTextViews.put(key, v.findViewById(textId));
        slotInfoButtons.put(key, v.findViewById(infoBtnId));
    }

    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_inventory);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        inventoryAdapter = new InventoryItemAdapter(new InventoryItemAdapter.OnItemClickListener() {
            @Override public void onItemClick(InventoryItemEntity item) { showItemDialog(item); }
            @Override public void onItemDetailsClick(InventoryItemEntity item) { showItemDetailsDialog(item); }
        });
        rv.setAdapter(inventoryAdapter);
    }

    private void onInventoryChanged(CharacterWithRelations cwr) {
        if (cwr == null || cwr.character == null) {
            tvNoCharacter.setVisibility(View.VISIBLE);
            cardGold.setVisibility(View.GONE);
            cardSlots.setVisibility(View.GONE);
            cardBackpack.setVisibility(View.GONE);
            return;
        }

        tvNoCharacter.setVisibility(View.GONE);
        cardGold.setVisibility(View.VISIBLE);
        cardSlots.setVisibility(View.VISIBLE);
        cardBackpack.setVisibility(View.VISIBLE);

        tvGold.setText(String.format("%.1f gp", cwr.character.currentGold));
        List<InventoryItemEntity> inventory = cwr.inventory != null ? cwr.inventory : new ArrayList<>();
        inventoryAdapter.setItems(inventory);
        tvEmptyInventory.setVisibility(inventory.isEmpty() ? View.VISIBLE : View.GONE);

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
        if (tvTotalWeight != null) {
            tvTotalWeight.setText(String.format("Total Weight: %.1f lb", total));
        }
    }

    private void showQuickGoldDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_quick_gold, null);
        TextView tvSign = dialogView.findViewById(R.id.tv_gold_sign);
        EditText etAmount = dialogView.findViewById(R.id.et_gold_amount);

        final boolean[] isAddition = {true};

        tvSign.setOnClickListener(v -> {
            isAddition[0] = !isAddition[0];
            tvSign.setText(isAddition[0] ? "+" : "−");
            tvSign.setTextColor(isAddition[0] ? 0xFF388E3C : 0xFFD32F2F);
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Adjust Gold")
                .setView(dialogView)
                .setPositiveButton("Apply", (d, w) -> {
                    try {
                        float amount = Float.parseFloat(etAmount.getText().toString().trim());
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
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_item_shop, null);
        EditText etSearch = dialogView.findViewById(R.id.et_shop_search);
        TextView tvLoading = dialogView.findViewById(R.id.tv_shop_loading);
        RecyclerView rv = dialogView.findViewById(R.id.rv_shop_items);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isBuy ? "Buy item" : "Add loot")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        ShopItemAdapter adapter = new ShopItemAdapter(isBuy, chosen -> {
            dialog.dismiss();
            promptQuantityAndAdd(chosen, isBuy);
        });
        rv.setAdapter(adapter);

        executor.execute(() -> {
            String gameSystem = viewModel.getCurrentGameSystem();
            List<ItemEntity> items = Open5eDatabase.getInstance(requireContext()).itemDao().getAllByGameSystem(gameSystem);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                tvLoading.setVisibility(View.GONE);
                adapter.setAllItems(items);
            });
        });

        etSearch.addTextChangedListener(new TextWatcher() {
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            ItemEntity item = shown.get(pos);
            h.name.setText(item.name);
            h.desc.setText(isBuy
                    ? String.format("%.1f gp • %.1f lb", item.cost, item.weight)
                    : String.format("%.1f lb", item.weight));
            h.select.setText(isBuy ? "Buy" : "Get");
            h.select.setOnClickListener(v -> onPick.onPick(item));
        }

        @Override
        public int getItemCount() { return shown.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView name, desc;
            Button select;

            VH(View v) {
                super(v);
                name = v.findViewById(R.id.item_name);
                desc = v.findViewById(R.id.item_desc);
                select = v.findViewById(R.id.select_button);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}