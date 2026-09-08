package com.murkfeatherstudio.questroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;

import java.util.ArrayList;
import java.util.List;

public class SkillOptionAdapter extends RecyclerView.Adapter<SkillOptionAdapter.ViewHolder> {
    private List<String> items = new ArrayList<>();
    private final OnItemRemoveListener removeListener;

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }

    public SkillOptionAdapter(OnItemRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void submitList(List<String> list) {
        this.items = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_skill_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String skillKey = items.get(position);
        holder.tvSkillKey.setText(skillKey);
        holder.btnDelete.setOnClickListener(v -> removeListener.onRemove(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSkillKey;
        View btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvSkillKey = itemView.findViewById(R.id.tv_skill_key);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}