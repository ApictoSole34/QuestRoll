package com.murkfeatherstudio.questroll.feature_spell.spell_school.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.feature_spell.spell_school.model.CombinedSpellSchool;

public class SpellSchoolAdapter extends ListAdapter<CombinedSpellSchool, SpellSchoolAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedSpellSchool school);
    }

    public SpellSchoolAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedSpellSchool>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedSpellSchool a, @NonNull CombinedSpellSchool b) {
                return a.id.equals(b.id);
            }
            @Override
            public boolean areContentsTheSame(@NonNull CombinedSpellSchool a, @NonNull CombinedSpellSchool b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spell_school, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedSpellSchool s, OnItemClickListener listener) {
            tvName.setText(s.name);
            tvDesc.setText(s.description != null ? s.description : "");
            tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(s));
        }
    }
}