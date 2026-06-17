package com.fizzycoyote.qusetroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.character.InventoryItemEntity;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemAdapter extends RecyclerView.Adapter<InventoryItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(InventoryItemEntity item);
    }

    private List<InventoryItemEntity> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public InventoryItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<InventoryItemEntity> newItems) {
        this.items = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View equippedIndicator;
        final TextView tvName, tvCategory, tvSlot, tvQuantity, tvWeight;

        ViewHolder(View v) {
            super(v);
            equippedIndicator = v.findViewById(R.id.view_equipped_indicator);
            tvName     = v.findViewById(R.id.tv_item_name);
            tvCategory = v.findViewById(R.id.tv_item_category);
            tvSlot     = v.findViewById(R.id.tv_item_slot);
            tvQuantity = v.findViewById(R.id.tv_item_quantity);
            tvWeight   = v.findViewById(R.id.tv_item_weight);
        }

        void bind(InventoryItemEntity item, OnItemClickListener listener) {
            tvName.setText(item.customName != null ? item.customName : "Unknown item");
            tvQuantity.setText("x" + item.quantity);

            // Weight: total (unit × quantity)
            float totalWeight = item.customWeight * item.quantity;
            tvWeight.setText(String.format("%.1f lb", totalWeight));

            // Category (if no custom description — just shows "Item")
            tvCategory.setText(item.customDescription != null && !item.customDescription.isEmpty()
                    ? "Custom" : "Item");

            // Slot badge — visible only if equipped
            if (item.isEquipped && item.slot != null && !item.slot.isEmpty()) {
                tvSlot.setText("• " + slotDisplayName(item.slot));
                tvSlot.setVisibility(View.VISIBLE);
                equippedIndicator.setBackgroundColor(0xFF1976D2); // blue — equipped
            } else {
                tvSlot.setVisibility(View.GONE);
                equippedIndicator.setBackgroundColor(0xFFBDBDBD); // grey — not equipped
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
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