package com.fizzycoyote.qusetroll.feature_spell.adapter;

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
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_custom_school, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), onEdit, onDelete);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_school_name);
            tvDesc = itemView.findViewById(R.id.tv_school_desc);
            btnEdit = itemView.findViewById(R.id.btn_edit_school);
            btnDelete = itemView.findViewById(R.id.btn_delete_school);
        }

        void bind(CustomSpellSchoolEntity school,
                  OnEditListener onEdit, OnDeleteListener onDelete) {
            tvName.setText(school.name);
            tvDesc.setText(school.description != null
                    && !school.description.isEmpty() ? school.description : "");
            tvDesc.setVisibility(school.description != null
                    && !school.description.isEmpty() ? View.VISIBLE : View.GONE);
            btnEdit.setOnClickListener(v -> onEdit.onEdit(school));
            btnDelete.setOnClickListener(v -> onDelete.onDelete(school));
        }
    }
}
