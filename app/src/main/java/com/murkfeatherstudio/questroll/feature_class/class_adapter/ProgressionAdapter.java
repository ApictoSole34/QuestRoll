package com.murkfeatherstudio.questroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.List;

public class ProgressionAdapter extends RecyclerView.Adapter<ProgressionAdapter.ViewHolder> {

    private final List<ClassWizardViewModel.ClassProgressionRow> progression;
    private final List<CustomFeatureEntity> features;
    private final String casterType;
    private final OnLevelClickListener listener;

    public interface OnLevelClickListener {
        void onEdit(int position, ClassWizardViewModel.ClassProgressionRow row);
        void onDelete(int position);
    }

    public ProgressionAdapter(List<ClassWizardViewModel.ClassProgressionRow> progression,
                              List<CustomFeatureEntity> features,
                              String casterType,
                              OnLevelClickListener listener) {
        this.progression = progression;
        this.features = features;
        this.casterType = casterType;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_progression, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassWizardViewModel.ClassProgressionRow row = progression.get(position);

        holder.tvLevel.setText("Level " + row.level);
        holder.tvLevel.setTypeface(ResourcesCompat.getFont(
                holder.itemView.getContext(), R.font.cinzel_bold));

        holder.tvSummary.setText(buildSummary(row));

        String featureNames = getFeatureNamesAtLevel(row.level);
        if (!featureNames.isEmpty()) {
            holder.tvLevelFeatures.setText(featureNames);
            holder.tvLevelFeatures.setVisibility(View.VISIBLE);
        } else {
            holder.tvLevelFeatures.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(holder.getBindingAdapterPosition());
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(holder.getBindingAdapterPosition(), row);
        });
    }

    private String buildSummary(ClassWizardViewModel.ClassProgressionRow row) {
        StringBuilder sb = new StringBuilder("Prof +" + row.proficiencyBonus);

        if (!"NONE".equals(casterType)) {
            if (row.cantripsKnown > 0) {
                sb.append(" · Cantrips ").append(row.cantripsKnown);
            }
            int maxSlotLevel = maxSpellLevelForCasterType();
            StringBuilder slots = new StringBuilder();
            for (int lvl = 1; lvl <= maxSlotLevel; lvl++) {
                int count = row.getSlotForLevel(lvl);
                if (count > 0) {
                    if (slots.length() > 0) slots.append(", ");
                    slots.append(lvl).append("st/nd/rd/th: ").append(count);
                }
            }
            if (slots.length() > 0) {
                sb.append(" · Slots: ").append(slots);
            }
        }

        return sb.toString();
    }

    /**
     * Przybliżenie standardowych zasad 5e: ile maksymalnie poziomów zaklęć
     * dany typ castera może w ogóle posiadać. Czysto kosmetyczne (do podglądu
     * w wierszu) — pełna tabela i tak jest dostępna w dialogu edycji.
     */
    private int maxSpellLevelForCasterType() {
        switch (casterType) {
            case "FULL": return 9;
            case "HALF": return 5;
            case "THIRD": return 4;
            case "WARLOCK": return 5;
            default: return 0;
        }
    }

    /**
     * Zwraca nazwy cech zdobywanych na danym poziomie, czytając je z realnej
     * listy features (CustomFeatureEntity.customGainedAt) zamiast z osobnego,
     * ręcznie wpisywanego pola tekstowego — eliminuje dwa rozjeżdżające się
     * źródła prawdy.
     *
     * UWAGA: zakładam, że CustomGainedAt ma publiczne pole `level` (int),
     * analogicznie do pola "level" w gained_at z API open5e. Jeśli Twoja
     * klasa nazywa to inaczej, podmień tylko `g.level` poniżej.
     */
    private String getFeatureNamesAtLevel(int level) {
        if (features == null) return "";
        StringBuilder sb = new StringBuilder();
        for (CustomFeatureEntity feature : features) {
            if (feature.customGainedAt == null) continue;
            for (CustomGainedAt g : feature.customGainedAt) {
                if (g.level == level) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(feature.name);
                    break;
                }
            }
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return progression.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevel, tvSummary, tvLevelFeatures;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevel = itemView.findViewById(R.id.tv_level);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvLevelFeatures = itemView.findViewById(R.id.tv_level_features);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}