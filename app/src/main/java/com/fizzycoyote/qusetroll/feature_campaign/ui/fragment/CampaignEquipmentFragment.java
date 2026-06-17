package com.fizzycoyote.qusetroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterWithRelations;
import com.fizzycoyote.qusetroll.core.models.character.InventoryItemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;
import com.fizzycoyote.qusetroll.feature_campaign.adapter.InventoryItemAdapter;
import com.fizzycoyote.qusetroll.feature_campaign.adapter.SimpleTextAdapter;
import com.fizzycoyote.qusetroll.feature_campaign.view_model.CampaignDetailViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CampaignEquipmentFragment extends Fragment {

    // ── Slot constants ──────────────────────────────────────────────────────
    public static final String[] SLOT_KEYS = {
            "main_hand", "off_hand", "body", "head",
            "neck", "cloak", "hands",
            "ring_left", "ring_right", "waist", "feet"
    };
    public static final String[] SLOT_LABELS = {
            "Main Hand", "Off Hand", "Body (Armor)", "Head",
            "Neck", "Cloak", "Hands (Gloves)",
            "Ring (Left)", "Ring (Right)", "Waist", "Feet"
    };

    // ── Fragment ─────────────────────────────────────────────────────────────
    private static final String ARG_CAMPAIGN_ID = "campaign_id";
    private long campaignId;
    private CampaignDetailViewModel viewModel;
    private InventoryItemAdapter inventoryAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<InventoryItemEntity> currentInventory = new ArrayList<>();

    // ── Views ────────────────────────────────────────────────────────────────
    private TextView tvNoCharacter, tvTotalWeight, tvCarryCapacity,
            tvEmptyInventory, tvAcValue, tvGold;
    private View cardSlots, cardBackpack, cardGold;
    private FloatingActionButton fabShop;

    private final Map<String, TextView> slotTextViews = new HashMap<>();
    private final Map<String, Button>   unequipButtons = new HashMap<>();

    // ─────────────────────────────────────────────────────────────────────────

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
        viewModel.setCampaignId(campaignId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_campaign_equipment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupSlotClickListeners();
        setupRecyclerView(view);
        setupGoldButtons();
        fabShop.setOnClickListener(v -> openShopDialog());
        viewModel.characterWithRelations.observe(getViewLifecycleOwner(), this::onInventoryChanged);
        viewModel.getEffectiveAc().observe(getViewLifecycleOwner(), ac -> {
            if (ac != null) tvAcValue.setText("AC: " + ac);
            else tvAcValue.setText("AC: —");
        });
    }

    // ── View binding ─────────────────────────────────────────────────────────

    private void bindViews(View v) {
        tvNoCharacter   = v.findViewById(R.id.tv_no_character);
        tvTotalWeight   = v.findViewById(R.id.tv_total_weight);
        tvCarryCapacity = v.findViewById(R.id.tv_carry_capacity);
        tvEmptyInventory= v.findViewById(R.id.tv_empty_inventory);
        cardSlots       = v.findViewById(R.id.card_slots);
        cardBackpack    = v.findViewById(R.id.card_backpack);
        cardGold        = v.findViewById(R.id.card_gold);
        tvAcValue       = v.findViewById(R.id.tv_ac_value);
        tvGold          = v.findViewById(R.id.tv_gold);
        fabShop         = v.findViewById(R.id.fab_shop);

        slotTextViews.put("main_hand",  v.findViewById(R.id.tv_slot_main_hand));
        slotTextViews.put("off_hand",   v.findViewById(R.id.tv_slot_off_hand));
        slotTextViews.put("body",       v.findViewById(R.id.tv_slot_body));
        slotTextViews.put("head",       v.findViewById(R.id.tv_slot_head));
        slotTextViews.put("neck",       v.findViewById(R.id.tv_slot_neck));
        slotTextViews.put("cloak",      v.findViewById(R.id.tv_slot_cloak));
        slotTextViews.put("hands",      v.findViewById(R.id.tv_slot_hands));
        slotTextViews.put("ring_left",  v.findViewById(R.id.tv_slot_ring_left));
        slotTextViews.put("ring_right", v.findViewById(R.id.tv_slot_ring_right));
        slotTextViews.put("waist",      v.findViewById(R.id.tv_slot_waist));
        slotTextViews.put("feet",       v.findViewById(R.id.tv_slot_feet));

        unequipButtons.put("main_hand",  v.findViewById(R.id.btn_unequip_main_hand));
        unequipButtons.put("off_hand",   v.findViewById(R.id.btn_unequip_off_hand));
        unequipButtons.put("body",       v.findViewById(R.id.btn_unequip_body));
        unequipButtons.put("head",       v.findViewById(R.id.btn_unequip_head));
        unequipButtons.put("neck",       v.findViewById(R.id.btn_unequip_neck));
        unequipButtons.put("cloak",      v.findViewById(R.id.btn_unequip_cloak));
        unequipButtons.put("hands",      v.findViewById(R.id.btn_unequip_hands));
        unequipButtons.put("ring_left",  v.findViewById(R.id.btn_unequip_ring_left));
        unequipButtons.put("ring_right", v.findViewById(R.id.btn_unequip_ring_right));
        unequipButtons.put("waist",      v.findViewById(R.id.btn_unequip_waist));
        unequipButtons.put("feet",       v.findViewById(R.id.btn_unequip_feet));
    }

    private void setupGoldButtons() {
        requireView().findViewById(R.id.btn_gold_minus).setOnClickListener(v ->
                viewModel.adjustGold(-1f));
        requireView().findViewById(R.id.btn_gold_plus).setOnClickListener(v ->
                viewModel.adjustGold(1f));
        requireView().findViewById(R.id.btn_gold_set).setOnClickListener(v ->
                showSetGoldDialog());
    }

    private void setupSlotClickListeners() {
        Map<String, Integer> rowIds = new HashMap<>();
        rowIds.put("main_hand",  R.id.slot_main_hand);
        rowIds.put("off_hand",   R.id.slot_off_hand);
        rowIds.put("body",       R.id.slot_body);
        rowIds.put("head",       R.id.slot_head);
        rowIds.put("neck",       R.id.slot_neck);
        rowIds.put("cloak",      R.id.slot_cloak);
        rowIds.put("hands",      R.id.slot_hands);
        rowIds.put("ring_left",  R.id.slot_ring_left);
        rowIds.put("ring_right", R.id.slot_ring_right);
        rowIds.put("waist",      R.id.slot_waist);
        rowIds.put("feet",       R.id.slot_feet);

        for (Map.Entry<String, Integer> e : rowIds.entrySet()) {
            String slot = e.getKey();
            View row = requireView().findViewById(e.getValue());
            if (row != null) {
                row.setOnClickListener(v -> {
                    InventoryItemEntity equipped = findEquippedInSlot(slot);
                    if (equipped != null) showItemDialog(equipped);
                });
            }
            Button btnUnequip = unequipButtons.get(slot);
            if (btnUnequip != null) {
                btnUnequip.setOnClickListener(v -> {
                    InventoryItemEntity equipped = findEquippedInSlot(slot);
                    if (equipped != null) viewModel.unequipItem(equipped.id);
                });
            }
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_inventory);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        inventoryAdapter = new InventoryItemAdapter(this::showItemDialog);
        rv.setAdapter(inventoryAdapter);
    }

    // ── Data update ──────────────────────────────────────────────────────────

    private void onInventoryChanged(CharacterWithRelations cwr) {
        if (cwr == null || cwr.character == null) {
            tvNoCharacter.setVisibility(View.VISIBLE);
            cardGold.setVisibility(View.GONE);
            cardSlots.setVisibility(View.GONE);
            cardBackpack.setVisibility(View.GONE);
            fabShop.setVisibility(View.GONE);
            return;
        }

        tvNoCharacter.setVisibility(View.GONE);
        cardGold.setVisibility(View.VISIBLE);
        cardSlots.setVisibility(View.VISIBLE);
        cardBackpack.setVisibility(View.VISIBLE);
        fabShop.setVisibility(View.VISIBLE);

        // Gold display
        tvGold.setText(String.format("%.1f gp", cwr.character.currentGold));

        List<InventoryItemEntity> inventory = cwr.inventory != null
                ? cwr.inventory : new ArrayList<>();
        currentInventory = inventory;

        updateSlotDisplays(inventory);
        inventoryAdapter.setItems(inventory);
        tvEmptyInventory.setVisibility(inventory.isEmpty() ? View.VISIBLE : View.GONE);

        // Weight
        float totalWeight = 0f;
        for (InventoryItemEntity item : inventory) totalWeight += item.customWeight * item.quantity;
        tvTotalWeight.setText(String.format("%.1f lb", totalWeight));

        if (cwr.attributes != null) {
            tvCarryCapacity.setText(String.format(" / %.0f lb", cwr.attributes.strength * 15f));
            int ac = 10 + cwr.attributes.dexterityMod;
            InventoryItemEntity armor = findEquippedInSlot("body");
            tvAcValue.setText("AC: " + ac + (armor != null ? "*" : ""));
        }

    }

    private void updateSlotDisplays(List<InventoryItemEntity> inventory) {
        for (String slot : SLOT_KEYS) {
            TextView tv = slotTextViews.get(slot);
            Button btn = unequipButtons.get(slot);
            if (tv != null) {
                tv.setText("—");
                tv.setTextColor(0xFF9E9E9E);
            }
            if (btn != null) btn.setVisibility(View.GONE);
        }
        for (InventoryItemEntity item : inventory) {
            if (item.isEquipped && item.slot != null) {
                TextView tv = slotTextViews.get(item.slot);
                Button btn = unequipButtons.get(item.slot);
                if (tv != null) {
                    tv.setText(item.customName != null ? item.customName : "?");
                    tv.setTextColor(0xFF1565C0);
                }
                if (btn != null) btn.setVisibility(View.VISIBLE);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // SHOP SYSTEM
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Opens the item search dialog.
     * Loads all items from Open5eDB in background, then shows search + list.
     */
    private void openShopDialog() {
        if (!isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_item_shop, null);
        EditText etSearch = dialogView.findViewById(R.id.et_shop_search);
        RecyclerView rv = dialogView.findViewById(R.id.rv_shop_items);
        TextView tvLoading = dialogView.findViewById(R.id.tv_shop_loading);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        ShopItemAdapter shopAdapter = new ShopItemAdapter(item -> {
        });
        rv.setAdapter(shopAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add item")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        shopAdapter.setListener(item -> {
            dialog.dismiss();
            showBuyOrReceiveDialog(item);
        });

        dialog.show();

        viewModel.getShopItems().observe(getViewLifecycleOwner(), new Observer<List<ItemEntity>>() {
            @Override
            public void onChanged(List<ItemEntity> items) {
                if (items == null) return;
                tvLoading.setVisibility(View.GONE);
                shopAdapter.setAllItems(items);
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
                    @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                        shopAdapter.filter(s.toString());
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });
                viewModel.getShopItems().removeObserver(this);
            }
        });

        viewModel.loadShopItemsIfNeeded();
    }

    // ── Buy or Receive dialog ────────────────────────────────────────────────

    private void showBuyOrReceiveDialog(ItemEntity open5eItem) {
        if (!isAdded()) return;

        CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
        float currentGold = (cwr != null && cwr.character != null)
                ? cwr.character.currentGold : 0f;

        // Build dialog view
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = dpToPx(20);
        layout.setPadding(p, dpToPx(12), p, dpToPx(4));

        // Item name
        TextView tvName = new TextView(requireContext());
        tvName.setText(open5eItem.name != null ? open5eItem.name : "Unknown");
        tvName.setTextSize(17f);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvName);

        // Category + rarity
        if (open5eItem.categoryName != null || open5eItem.rarityName != null) {
            TextView tvMeta = new TextView(requireContext());
            StringBuilder meta = new StringBuilder();
            if (open5eItem.categoryName != null) meta.append(open5eItem.categoryName);
            if (open5eItem.rarityName != null && !"None".equals(open5eItem.rarityName))
                meta.append("  •  ").append(open5eItem.rarityName);
            tvMeta.setText(meta.toString());
            tvMeta.setTextSize(12f);
            tvMeta.setTextColor(0xFF888888);
            layout.addView(tvMeta);
        }

        addSpacer(layout, 10);

        // Cost info
        addInfoRow(layout, "Cost", String.format("%.2f gp", open5eItem.cost));
        addInfoRow(layout, "Weight", String.format("%.2f lb", open5eItem.weight));
        if (open5eItem.requiresAttunement) {
            addInfoRow(layout, "Attunement",
                    open5eItem.attunementDetail != null ? open5eItem.attunementDetail : "Required");
        }

        addSpacer(layout, 10);

        // Gold status
        TextView tvGoldStatus = new TextView(requireContext());
        boolean canAfford = currentGold >= open5eItem.cost;
        tvGoldStatus.setText(String.format("Your gold: %.1f gp  |  Cost: %.2f gp",
                currentGold, open5eItem.cost));
        tvGoldStatus.setTextSize(13f);
        tvGoldStatus.setTextColor(canAfford ? 0xFF388E3C : 0xFFD32F2F);
        tvGoldStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvGoldStatus);

        // Quantity picker
        addSpacer(layout, 12);
        LinearLayout qtyRow = new LinearLayout(requireContext());
        qtyRow.setOrientation(LinearLayout.HORIZONTAL);
        qtyRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvQtyLabel = new TextView(requireContext());
        tvQtyLabel.setText("Quantity: ");
        tvQtyLabel.setTextSize(14f);
        qtyRow.addView(tvQtyLabel);

        EditText etQty = new EditText(requireContext());
        etQty.setText("1");
        etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        etQty.setWidth(dpToPx(64));
        qtyRow.addView(etQty);
        layout.addView(qtyRow);

        // Description (short excerpt)
        if (open5eItem.desc != null && !open5eItem.desc.isEmpty()) {
            addSpacer(layout, 10);
            TextView tvDesc = new TextView(requireContext());
            String desc = open5eItem.desc.length() > 200
                    ? open5eItem.desc.substring(0, 200) + "…"
                    : open5eItem.desc;
            tvDesc.setText(desc);
            tvDesc.setTextSize(12f);
            tvDesc.setTextColor(0xFF555555);
            layout.addView(tvDesc);
        }

        // Wrap in ScrollView in case content is long
        ScrollView sv = new ScrollView(requireContext());
        sv.addView(layout);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(sv)
                .setNeutralButton("Cancel", null);

        // RECEIVE button — always available
        builder.setNegativeButton("Receive (free)", (d, w) -> {
            int qty = parseQty(etQty.getText().toString());
            receiveItem(open5eItem, qty);
        });

        // BUY button
        builder.setPositiveButton(
                canAfford
                        ? String.format("Buy (%.2f gp)", open5eItem.cost)
                        : "Not enough gold",
                canAfford
                        ? (d, w) -> {
                    int qty = parseQty(etQty.getText().toString());
                    buyItem(open5eItem, qty);
                }
                        : null
        );

        AlertDialog d = builder.create();
        d.show();

        // Disable Buy button visually if can't afford
        if (!canAfford) {
            Button buyBtn = d.getButton(AlertDialog.BUTTON_POSITIVE);
            if (buyBtn != null) {
                buyBtn.setEnabled(false);
                buyBtn.setAlpha(0.4f);
            }
        }

        // Live quantity update for cost display
        etQty.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                int qty = parseQty(s.toString());
                float totalCost = open5eItem.cost * qty;
                boolean afford = currentGold >= totalCost;
                tvGoldStatus.setText(String.format(
                        "Your gold: %.1f gp  |  Total cost: %.2f gp", currentGold, totalCost));
                tvGoldStatus.setTextColor(afford ? 0xFF388E3C : 0xFFD32F2F);
                Button buyBtn = d.getButton(AlertDialog.BUTTON_POSITIVE);
                if (buyBtn != null) {
                    buyBtn.setEnabled(afford);
                    buyBtn.setAlpha(afford ? 1f : 0.4f);
                    buyBtn.setText(afford
                            ? String.format("Buy (%.2f gp)", totalCost)
                            : "Not enough gold");
                }
            }
        });
    }

    // ── Transaction methods ──────────────────────────────────────────────────

    private void buyItem(ItemEntity open5eItem, int quantity) {
        CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
        if (cwr == null || cwr.character == null) return;

        float totalCost = open5eItem.cost * quantity;
        if (cwr.character.currentGold < totalCost) {
            Toast.makeText(getContext(), "Not enough gold!", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.adjustGold(-totalCost);

        InventoryItemEntity entity = buildInventoryItem(open5eItem, quantity, cwr.character.id);
        viewModel.addItemToInventory(entity);

        Toast.makeText(getContext(),
                String.format("Bought %s for %.2f gp", open5eItem.name, totalCost),
                Toast.LENGTH_SHORT).show();
    }

    private void receiveItem(ItemEntity open5eItem, int quantity) {
        CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
        if (cwr == null || cwr.character == null) return;

        InventoryItemEntity entity = buildInventoryItem(open5eItem, quantity, cwr.character.id);
        viewModel.addItemToInventory(entity);

        Toast.makeText(getContext(), "Received: " + open5eItem.name, Toast.LENGTH_SHORT).show();
    }

    private InventoryItemEntity buildInventoryItem(ItemEntity src, int qty, long characterId) {
        InventoryItemEntity e = new InventoryItemEntity();
        e.characterId     = characterId;
        e.itemKey         = src.key;
        e.customName      = src.name;
        e.customDescription = src.desc;
        e.customWeight    = src.weight;
        e.customCost      = src.cost;
        e.quantity        = qty;
        e.isEquipped      = false;
        e.slot            = null;
        return e;
    }

    // ── Item detail dialog (existing inventory item) ─────────────────────────

    private void showItemDialog(InventoryItemEntity item) {
        if (!isAdded()) return;

        ScrollView sv = new ScrollView(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = dpToPx(16);
        layout.setPadding(p, p, p, p);
        sv.addView(layout);

        TextView tvName = new TextView(requireContext());
        tvName.setText(item.customName != null ? item.customName : "Unknown item");
        tvName.setTextSize(18f);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvName);

        if (item.isEquipped && item.slot != null) {
            TextView tvSlot = new TextView(requireContext());
            tvSlot.setText("Equipped: " + slotLabel(item.slot));
            tvSlot.setTextSize(12f);
            tvSlot.setTextColor(0xFF1565C0);
            layout.addView(tvSlot);
        }

        addSpacer(layout, 10);
        addInfoRow(layout, "Weight", String.format("%.2f lb × %d = %.2f lb",
                item.customWeight, item.quantity, item.customWeight * item.quantity));
        addInfoRow(layout, "Cost", String.format("%.2f gp", item.customCost));
        addInfoRow(layout, "Quantity", String.valueOf(item.quantity));
        if (item.slot != null && !item.slot.isEmpty())
            addInfoRow(layout, "Slot", slotLabel(item.slot));

        if (item.customDescription != null && !item.customDescription.isEmpty()) {
            addSpacer(layout, 10);
            TextView tvDesc = new TextView(requireContext());
            tvDesc.setText(item.customDescription);
            tvDesc.setTextSize(13f);
            layout.addView(tvDesc);
        }

        // Load extra Open5e data async
        if (item.itemKey != null) {
            addSpacer(layout, 10);
            TextView tvExtra = new TextView(requireContext());
            tvExtra.setText("Loading details…");
            tvExtra.setTextSize(12f);
            tvExtra.setTextColor(0xFF888888);
            layout.addView(tvExtra);

            executor.execute(() -> {
                if (!isAdded()) return;
                ItemEntity ie = Open5eDatabase.getInstance(requireContext())
                        .itemDao().getByKeySync(item.itemKey);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    if (ie != null) {
                        StringBuilder sb = new StringBuilder();
                        if (ie.categoryName != null) sb.append("Category: ").append(ie.categoryName).append("\n");
                        if (ie.rarityName != null && !"None".equals(ie.rarityName))
                            sb.append("Rarity: ").append(ie.rarityName).append("\n");
                        if (ie.requiresAttunement) sb.append("Requires attunement\n");
                        tvExtra.setText(sb.length() > 0 ? sb.toString().trim() : "");
                    } else {
                        tvExtra.setText("");
                    }
                });
            });
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(sv)
                .setNeutralButton("Close", null)
                .setNegativeButton("Delete", (d, w) -> confirmDelete(item))
                .setPositiveButton(item.isEquipped ? "Unequip" : "Equip", (d, w) -> {
                    if (item.isEquipped) viewModel.unequipItem(item.id);
                    else showSlotPickerDialog(item);
                })
                .show();
    }

    private void showSlotPickerDialog(InventoryItemEntity item) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Equip to slot")
                .setItems(SLOT_LABELS, (d, which) -> {
                    String slot = SLOT_KEYS[which];
                    InventoryItemEntity current = findEquippedInSlot(slot);
                    if (current != null && current.id != item.id) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Slot occupied")
                                .setMessage("\"" + current.customName + "\" is already here. Replace?")
                                .setPositiveButton("Replace", (dd, ww) -> {
                                    viewModel.unequipItem(current.id);
                                    viewModel.equipItem(item.id, slot);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    } else {
                        viewModel.equipItem(item.id, slot);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(InventoryItemEntity item) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete item")
                .setMessage("Remove \"" + item.customName + "\" from inventory?")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteItem(item.id))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Gold dialog ──────────────────────────────────────────────────────────

    private void showSetGoldDialog() {
        if (!isAdded()) return;
        EditText et = new EditText(requireContext());
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
        if (cwr != null && cwr.character != null)
            et.setText(String.format("%.1f", cwr.character.currentGold));

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setPadding(dpToPx(20), dpToPx(8), dpToPx(20), 0);
        layout.addView(et);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set gold")
                .setView(layout)
                .setPositiveButton("Set", (d, w) -> {
                    try {
                        float val = Float.parseFloat(et.getText().toString().trim());
                        viewModel.setGold(val);
                    } catch (NumberFormatException ignored) {
                        Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private InventoryItemEntity findEquippedInSlot(String slot) {
        for (InventoryItemEntity item : currentInventory) {
            if (item.isEquipped && slot.equals(item.slot)) return item;
        }
        return null;
    }

    private String slotLabel(String slot) {
        for (int i = 0; i < SLOT_KEYS.length; i++) {
            if (SLOT_KEYS[i].equals(slot)) return SLOT_LABELS[i];
        }
        return slot;
    }

    private int parseQty(String s) {
        try {
            int q = Integer.parseInt(s.trim());
            return Math.max(1, q);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void addInfoRow(LinearLayout layout, String label, String value) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView tvL = new TextView(requireContext());
        tvL.setText(label + ": ");
        tvL.setTextSize(13f);
        tvL.setTextColor(0xFF666666);
        tvL.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(tvL);
        TextView tvV = new TextView(requireContext());
        tvV.setText(value);
        tvV.setTextSize(13f);
        row.addView(tvV);
        layout.addView(row);
    }

    private void addSpacer(LinearLayout layout, int dp) {
        View s = new View(requireContext());
        s.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(dp)));
        layout.addView(s);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    // SHOP ITEM ADAPTER
    private static class ShopItemAdapter
            extends RecyclerView.Adapter<ShopItemAdapter.VH> {

        interface OnItemSelected { void onSelect(ItemEntity item); }

        private List<ItemEntity> allItems    = new ArrayList<>();
        private List<ItemEntity> filtered    = new ArrayList<>();
        private OnItemSelected listener;

        ShopItemAdapter(OnItemSelected listener) { this.listener = listener; }

        void setListener(OnItemSelected listener) { this.listener = listener; }

        void setAllItems(List<ItemEntity> items) {
            allItems = items != null ? items : new ArrayList<>();
            filtered = new ArrayList<>(allItems);
            notifyDataSetChanged();
        }

        void filter(String query) {
            if (query == null || query.trim().isEmpty()) {
                filtered = new ArrayList<>(allItems);
            } else {
                String q = query.toLowerCase().trim();
                filtered = new ArrayList<>();
                for (ItemEntity item : allItems) {
                    if (item.name != null && item.name.toLowerCase().contains(q))
                        filtered.add(item);
                }
            }
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Build row view programmatically
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(32, 20, 32, 20);
            android.util.TypedValue tv = new android.util.TypedValue();
            parent.getContext().getTheme()
                    .resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
            row.setBackgroundResource(tv.resourceId);

            TextView tvName = new TextView(parent.getContext());
            tvName.setTag("name");
            tvName.setTextSize(14f);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tvName);

            TextView tvMeta = new TextView(parent.getContext());
            tvMeta.setTag("meta");
            tvMeta.setTextSize(12f);
            tvMeta.setTextColor(0xFF888888);
            row.addView(tvMeta);

            row.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ItemEntity item = filtered.get(position);
            ((TextView) holder.itemView.findViewWithTag("name"))
                    .setText(item.name != null ? item.name : "?");
            String meta = (item.categoryName != null ? item.categoryName : "")
                    + (item.cost > 0 ? String.format("  •  %.2f gp", item.cost) : "")
                    + (item.weight > 0 ? String.format("  •  %.2f lb", item.weight) : "");
            ((TextView) holder.itemView.findViewWithTag("meta")).setText(meta);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSelect(item);
            });
        }

        @Override public int getItemCount() { return filtered.size(); }

        static class VH extends RecyclerView.ViewHolder {
            VH(View v) { super(v); }
        }
    }
}