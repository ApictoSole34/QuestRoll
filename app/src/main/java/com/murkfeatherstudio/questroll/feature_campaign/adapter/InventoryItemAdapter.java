package com.murkfeatherstudio.questroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
import com.murkfeatherstudio.questroll.databinding.ItemInventoryRowBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the campaign equipment screen's inventory list ({@code item_inventory_row.xml}).
 * <p>
 * Each row exposes two distinct interactions:
 * <ul>
 *     <li>Tapping the row itself → {@link OnItemClickListener#onItemClick(InventoryItemEntity)},
 *         intended for management actions (equip/unequip, change slot, delete).</li>
 *     <li>Tapping the "Details" button → {@link OnItemClickListener#onItemDetailsClick(InventoryItemEntity)},
 *         intended to open a read-only info dialog about the item.</li>
 * </ul>
 */
public class InventoryItemAdapter extends RecyclerView.Adapter<InventoryItemAdapter.ViewHolder> {

    /** Callback for the two supported row interactions. */
    public interface OnItemClickListener {
        /** Row tapped — used for equip/unequip/slot/delete management. */
        void onItemClick(InventoryItemEntity item);

        /** "Details" button tapped — used to show read-only item info. */
        void onItemDetailsClick(InventoryItemEntity item);
    }

    private List<InventoryItemEntity> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public InventoryItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Replaces the currently displayed items and refreshes the list.
     *
     * @param newItems the new inventory snapshot; {@code null} is treated as empty.
     */
    public void setItems(List<InventoryItemEntity> newItems) {
        this.items = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInventoryRowBinding binding = ItemInventoryRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemInventoryRowBinding binding;

        ViewHolder(ItemInventoryRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(InventoryItemEntity item, OnItemClickListener listener) {
            binding.tvItemName.setText(item.customName != null ? item.customName : "Unknown item");
            binding.tvItemQuantity.setText("x" + item.quantity);

            // Weight: total (unit × quantity)
            float totalWeight = item.customWeight * item.quantity;
            binding.tvItemWeight.setText(String.format("%.1f lb", totalWeight));

            // Category (if no custom description — just shows "Item")
            binding.tvItemCategory.setText(item.customDescription != null && !item.customDescription.isEmpty()
                    ? "Custom" : "Item");

            // Slot badge — visible only if equipped
            if (item.isEquipped && item.slot != null && !item.slot.isEmpty()) {
                binding.tvItemSlot.setText("• " + slotDisplayName(item.slot));
                binding.tvItemSlot.setVisibility(View.VISIBLE);
                binding.viewEquippedIndicator.setBackgroundColor(0xFF1976D2); // blue — equipped
            } else {
                binding.tvItemSlot.setVisibility(View.GONE);
                binding.viewEquippedIndicator.setBackgroundColor(0xFFBDBDBD); // grey — not equipped
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });

            binding.btnItemDetails.setOnClickListener(v -> {
                if (listener != null) listener.onItemDetailsClick(item);
            });
        }

        /** Maps slot key → human-readable label. */
        private String slotDisplayName(String slot) {
            switch (slot) {
                case "main_hand":  return "Main Hand";
                case "off_hand":   return "Off Hand";
                case "body":       return "Armor";
                case "head":       return "Head";
                case "neck":       return "Neck";
                case "cloak":      return "Cloak";
                case "hands":      return "Gloves";
                case "ring_left":  return "Ring L";
                case "ring_right": return "Ring R";
                case "waist":      return "Waist";
                case "feet":       return "Boots";
                default:           return slot;
            }
        }
    }
}