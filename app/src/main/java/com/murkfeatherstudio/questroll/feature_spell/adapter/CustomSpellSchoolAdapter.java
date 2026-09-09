package com.murkfeatherstudio.questroll.feature_spell.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.murkfeatherstudio.questroll.databinding.ItemCustomSchoolBinding;

public class CustomSpellSchoolAdapter
        extends ListAdapter<CustomSpellSchoolEntity, CustomSpellSchoolAdapter.ViewHolder> {

    private final OnEditListener onEdit;
    private final OnDeleteListener onDelete;

    public interface OnEditListener {
        void onEdit(CustomSpellSchoolEntity school);
    }

    public interface OnDeleteListener {
        void onDelete(CustomSpellSchoolEntity school);
    }

    public CustomSpellSchoolAdapter(OnEditListener onEdit, OnDeleteListener onDelete) {
        super(DIFF);
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    private static final DiffUtil.ItemCallback<CustomSpellSchoolEntity> DIFF =
            new DiffUtil.ItemCallback<CustomSpellSchoolEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull CustomSpellSchoolEntity a,
                                               @NonNull CustomSpellSchoolEntity b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull CustomSpellSchoolEntity a,
                                                  @NonNull CustomSpellSchoolEntity b) {
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

        void bind(CustomSpellSchoolEntity school,
                  OnEditListener onEdit, OnDeleteListener onDelete) {
            binding.tvSchoolName.setText(school.name);
            binding.tvSchoolDesc.setText(school.description != null
                    && !school.description.isEmpty() ? school.description : "");
            binding.tvSchoolDesc.setVisibility(school.description != null
                    && !school.description.isEmpty() ? View.VISIBLE : View.GONE);
            binding.btnEditSchool.setOnClickListener(v -> onEdit.onEdit(school));
            binding.btnDeleteSchool.setOnClickListener(v -> onDelete.onDelete(school));
        }
    }
}
