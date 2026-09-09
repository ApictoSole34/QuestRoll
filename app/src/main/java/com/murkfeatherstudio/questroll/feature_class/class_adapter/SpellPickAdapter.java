package com.murkfeatherstudio.questroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemSpellPickBinding;
import com.murkfeatherstudio.questroll.feature_class.model.CombinedSpell;

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
        ItemSpellPickBinding binding = ItemSpellPickBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CombinedSpell spell = visibleSpells.get(position);

        holder.binding.tvSpellLevel.setText(spell.getLevel() == 0 ? "C" : String.valueOf(spell.getLevel()));
        holder.binding.tvSpellName.setText(spell.getName() + (spell.isCustom() ? " (custom)" : ""));
        holder.binding.cbSpellSelected.setChecked(selectedKeys.contains(spell.getKey()));

        holder.itemView.setOnClickListener(v -> {
            boolean newState = !selectedKeys.contains(spell.getKey());
            if (newState) {
                selectedKeys.add(spell.getKey());
            } else {
                selectedKeys.remove(spell.getKey());
            }
            holder.binding.cbSpellSelected.setChecked(newState);
            if (listener != null) listener.onToggle(spell.getKey(), newState);
        });
    }

    @Override
    public int getItemCount() {
        return visibleSpells.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSpellPickBinding binding;

        ViewHolder(ItemSpellPickBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}