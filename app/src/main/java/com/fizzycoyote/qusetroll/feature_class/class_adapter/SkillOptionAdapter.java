package com.fizzycoyote.qusetroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;

import java.util.ArrayList;
import java.util.List;

public class SkillOptionAdapter extends RecyclerView.Adapter<SkillOptionAdapter.ViewHolder> {
    private List<String> items = new ArrayList<>();
    private final OnItemRemoveListener removeListener;
    private final OnAddClickListener addListener;

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }
    public interface OnAddClickListener {
        void onAdd();
    }

    public SkillOptionAdapter(OnItemRemoveListener removeListener, OnAddClickListener addListener) {
        this.removeListener = removeListener;
        this.addListener = addListener;
    }

    public void submitList(List<String> list) {
        this.items = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String skillKey = items.get(position);
        holder.tvSkillKey.setText(skillKey);
        holder.btnDelete.setOnClickListener(v -> removeListener.onRemove(position));
        holder.btnAdd.setOnClickListener(v -> addListener.onAdd());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSkillKey;
        View btnDelete, btnAdd;
        ViewHolder(View itemView) {
            super(itemView);
            tvSkillKey = itemView.findViewById(R.id.tv_skill_key);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }
}