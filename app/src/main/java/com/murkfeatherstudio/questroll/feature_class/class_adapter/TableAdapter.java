package com.murkfeatherstudio.questroll.feature_class.class_adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_table_data.CustomTableData;

import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {

    private List<CustomTableData> tableData;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public TableAdapter(List<CustomTableData> tableData, OnItemClickListener listener) {
        this.tableData = tableData;
        this.listener = listener;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < tableData.size()) {
            tableData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, tableData.size());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table_editor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CustomTableData data = tableData.get(position);

        if (holder.etLevel.getTag() instanceof TextWatcher) {
            holder.etLevel.removeTextChangedListener((TextWatcher) holder.etLevel.getTag());
        }
        if (holder.etValue.getTag() instanceof TextWatcher) {
            holder.etValue.removeTextChangedListener((TextWatcher) holder.etValue.getTag());
        }

        holder.etLevel.setText(String.valueOf(data.level));
        holder.etValue.setText(data.columnValue);

        TextWatcher levelWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    data.level = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    data.level = 0;
                }
            }
        };

        TextWatcher valueWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                data.columnValue = s.toString();
            }
        };

        holder.etLevel.addTextChangedListener(levelWatcher);
        holder.etValue.addTextChangedListener(valueWatcher);

        holder.etLevel.setTag(levelWatcher);
        holder.etValue.setTag(valueWatcher);

        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onDeleteClick(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText etLevel, etValue;
        ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            etLevel = itemView.findViewById(R.id.etLevel);
            etValue = itemView.findViewById(R.id.etValue);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}