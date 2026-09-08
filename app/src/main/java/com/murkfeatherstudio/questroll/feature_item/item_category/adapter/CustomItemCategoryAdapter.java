package com.murkfeatherstudio.questroll.feature_item.item_category.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.custom.custom_item_category.CustomItemCategoryEntity;

public class CustomItemCategoryAdapter
        extends ListAdapter<CustomItemCategoryEntity, CustomItemCategoryAdapter.ViewHolder> {

    private final OnEditListener onEdit;
    private final OnDeleteListener onDelete;

    public interface OnEditListener { void onEdit(CustomItemCategoryEntity category); }
    public interface OnDeleteListener { void onDelete(CustomItemCategoryEntity category); }

    public CustomItemCategoryAdapter(OnEditListener onEdit, OnDeleteListener onDelete) {
        super(DIFF);
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    private static final DiffUtil.ItemCallback<CustomItemCategoryEntity> DIFF =
            new DiffUtil.ItemCallback<CustomItemCategoryEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull CustomItemCategoryEntity a,
                                               @NonNull CustomItemCategoryEntity b) {
                    return a.id == b.id;
                }
                @Override
                public boolean areContentsTheSame(@NonNull CustomItemCategoryEntity a,
                                                  @NonNull CustomItemCategoryEntity b) {
                    return a.name.equals(b.name);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_custom_school, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomItemCategoryEntity c = getItem(position);
        holder.tvName.setText(c.name);
        if (c.description != null && !c.description.isEmpty()) {
            holder.tvDesc.setText(c.description);
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }
        holder.btnEdit.setOnClickListener(v -> onEdit.onEdit(c));
        holder.btnDelete.setOnClickListener(v -> onDelete.onDelete(c));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_school_name);
            tvDesc = v.findViewById(R.id.tv_school_desc);
            btnEdit = v.findViewById(R.id.btn_edit_school);
            btnDelete = v.findViewById(R.id.btn_delete_school);
        }
    }
}