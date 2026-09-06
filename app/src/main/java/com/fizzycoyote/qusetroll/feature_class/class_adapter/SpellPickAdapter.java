package com.fizzycoyote.qusetroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedSpell;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SpellPickAdapter extends RecyclerView.Adapter<SpellPickAdapter.ViewHolder> {

    private final List<CombinedSpell> allSpells;
    private List<CombinedSpell> visibleSpells;
    private final Set<String> selectedKeys;
    private final OnSpellToggleListener listener;

    public interface OnSpellToggleListener {
        void onToggle(String key, boolean selected);
    }

    public SpellPickAdapter(List<CombinedSpell> allSpells, Set<String> selectedKeys,
                            OnSpellToggleListener listener) {
        this.allSpells = allSpells;
        this.visibleSpells = new ArrayList<>(allSpells);
        this.selectedKeys = selectedKeys;
        this.listener = listener;
    }

    public void filter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        visibleSpells.clear();
        if (q.isEmpty()) {
            visibleSpells.addAll(allSpells);
        } else {
            for (CombinedSpell spell : allSpells) {
                if (spell.getName().toLowerCase(Locale.ROOT).contains(q)) {
                    visibleSpells.add(spell);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spell_pick, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CombinedSpell spell = visibleSpells.get(position);

        holder.tvLevel.setText(spell.getLevel() == 0 ? "C" : String.valueOf(spell.getLevel()));
        holder.tvName.setText(spell.getName() + (spell.isCustom() ? " (custom)" : ""));
        holder.checkBox.setChecked(selectedKeys.contains(spell.getKey()));

        holder.itemView.setOnClickListener(v -> {
            boolean newState = !selectedKeys.contains(spell.getKey());
            if (newState) {
                selectedKeys.add(spell.getKey());
            } else {
                selectedKeys.remove(spell.getKey());
            }
            holder.checkBox.setChecked(newState);
            if (listener != null) listener.onToggle(spell.getKey(), newState);
        });
    }

    @Override
    public int getItemCount() {
        return visibleSpells.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevel, tvName;
        CheckBox checkBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevel = itemView.findViewById(R.id.tv_spell_level);
            tvName = itemView.findViewById(R.id.tv_spell_name);
            checkBox = itemView.findViewById(R.id.cb_spell_selected);
        }
    }
}