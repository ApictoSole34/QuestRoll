package com.fizzycoyote.qusetroll.feature_creature.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;

public class CustomCreatureTypeAdapter
        extends ListAdapter<CustomCreatureTypeEntity, CustomCreatureTypeAdapter.ViewHolder> {

    private final OnEditListener onEdit;
    private final OnDeleteListener onDelete;

    public interface OnEditListener { void onEdit(CustomCreatureTypeEntity type); }
    public interface OnDeleteListener { void onDelete(CustomCreatureTypeEntity type); }

    public CustomCreatureTypeAdapter(OnEditListener onEdit, OnDeleteListener onDelete) {
        super(DIFF);
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    private static final DiffUtil.ItemCallback<CustomCreatureTypeEntity> DIFF =
            new DiffUtil.ItemCallback<CustomCreatureTypeEntity>() {
                @Override public boolean areItemsTheSame(@NonNull CustomCreatureTypeEntity a,
                                                         @NonNull CustomCreatureTypeEntity b) {
                    return a.id == b.id;
                }
                @Override public boolean areContentsTheSame(@NonNull CustomCreatureTypeEntity a,
                                                            @NonNull CustomCreatureTypeEntity b) {
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
        CustomCreatureTypeEntity t = getItem(position);
        holder.tvName.setText(t.name);
        if (t.description != null && !t.description.isEmpty()) {
            holder.tvDesc.setText(t.description);
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }
        holder.btnEdit.setOnClickListener(v -> onEdit.onEdit(t));
        holder.btnDelete.setOnClickListener(v -> onDelete.onDelete(t));
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