package com.fizzycoyote.qusetroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.character.CharacterCreationDTO;

import java.util.ArrayList;
import java.util.List;

public class StartingItemAdapter extends RecyclerView.Adapter<StartingItemAdapter.ViewHolder> {
    private List<CharacterCreationDTO.InventoryItemDTO> items = new ArrayList<>();
    private final OnItemRemoveListener removeListener;

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }

    public StartingItemAdapter(OnItemRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void submitList(List<CharacterCreationDTO.InventoryItemDTO> list) {
        this.items = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_starting_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CharacterCreationDTO.InventoryItemDTO item = items.get(position);
        holder.tvItemName.setText(item.customName);
        holder.tvItemDetails.setText("x" + item.quantity + ", " + item.customWeight + " lb");
        holder.btnDelete.setOnClickListener(v -> removeListener.onRemove(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemDetails;
        View btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemDetails = itemView.findViewById(R.id.tv_item_details);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}