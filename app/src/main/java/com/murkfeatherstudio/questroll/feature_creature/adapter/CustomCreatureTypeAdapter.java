package com.murkfeatherstudio.questroll.feature_creature.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;
import com.murkfeatherstudio.questroll.databinding.ItemCustomSchoolBinding;

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
        ItemCustomSchoolBinding binding = ItemCustomSchoolBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onEdit, onDelete);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCustomSchoolBinding binding;

        ViewHolder(ItemCustomSchoolBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CustomCreatureTypeEntity t, OnEditListener onEdit, OnDeleteListener onDelete) {
            binding.tvSchoolName.setText(t.name);
            if (t.description != null && !t.description.isEmpty()) {
                binding.tvSchoolDesc.setText(t.description);
                binding.tvSchoolDesc.setVisibility(View.VISIBLE);
            } else {
                binding.tvSchoolDesc.setVisibility(View.GONE);
            }
            binding.btnEditSchool.setOnClickListener(v -> onEdit.onEdit(t));
            binding.btnDeleteSchool.setOnClickListener(v -> onDelete.onDelete(t));
        }
    }
}