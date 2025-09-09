package com.fizzycoyote.qusetroll.feature_class.class_adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;

import java.util.List;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.ViewHolder> {
    private List<CustomGainedAt> levels;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public LevelAdapter(List<CustomGainedAt> levels, OnItemClickListener listener) {
        this.levels = levels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_level_editor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomGainedAt level = levels.get(position);

        // Usuń stare TextWatchery aby uniknąć wycieków pamięci
        if (holder.etLevel.getTag() instanceof TextWatcher) {
            holder.etLevel.removeTextChangedListener((TextWatcher) holder.etLevel.getTag());
        }
        if (holder.etDetail.getTag() instanceof TextWatcher) {
            holder.etDetail.removeTextChangedListener((TextWatcher) holder.etDetail.getTag());
        }

        holder.etLevel.setText(String.valueOf(level.level));
        holder.etDetail.setText(level.details);

        TextWatcher levelWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    level.level = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    level.level = 0;
                }
            }
        };

        TextWatcher detailWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                level.details = s.toString();
            }
        };

        holder.etLevel.addTextChangedListener(levelWatcher);
        holder.etDetail.addTextChangedListener(detailWatcher);

        holder.etLevel.setTag(levelWatcher);
        holder.etDetail.setTag(detailWatcher);

        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onDeleteClick(currentPosition);
            }
        });
    }

    public void removeItem(int position) {
        if (position >= 0 && position < levels.size()) {
            levels.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, levels.size());
        }
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText etLevel, etDetail;
        ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            etLevel = itemView.findViewById(R.id.etLevel);
            etDetail = itemView.findViewById(R.id.etDetail);
            btnDelete = itemView.findViewById(R.id.btnDelete); // Dodaj to!
        }
    }
}