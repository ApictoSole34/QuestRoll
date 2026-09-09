package com.murkfeatherstudio.questroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.murkfeatherstudio.questroll.databinding.ItemProgressionBinding;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        ItemProgressionBinding binding = ItemProgressionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(progression.get(position), features, casterType, position, listener);
    }

    @Override
    public int getItemCount() {
        return progression.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProgressionBinding binding;

        ViewHolder(ItemProgressionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassWizardViewModel.ClassProgressionRow row,
                  List<CustomFeatureEntity> allFeatures,
                  String casterType,
                  int position,
                  OnLevelClickListener listener) {

            binding.tvLevel.setText("Level " + row.level);

            StringBuilder summary = new StringBuilder();
            summary.append("Prof +").append(row.proficiencyBonus);
            if (!"NONE".equals(casterType)) {
                summary.append(" | Cantrips: ").append(row.cantripsKnown);
            }
            binding.tvSummary.setText(summary.toString());

            List<String> levelFeatures = new ArrayList<>();
            for (CustomFeatureEntity f : allFeatures) {
                if (f.customGainedAt != null) {
                    for (CustomGainedAt g : f.customGainedAt) {
                        if (g.level == row.level) {
                            levelFeatures.add(f.name);
                        }
                    }
                }
            }

            if (levelFeatures.isEmpty()) {
                binding.tvLevelFeatures.setText("No features");
                binding.tvLevelFeatures.setAlpha(0.5f);
            } else {
                binding.tvLevelFeatures.setText(String.join(", ", levelFeatures));
                binding.tvLevelFeatures.setAlpha(1.0f);
            }

            itemView.setOnClickListener(v -> listener.onEdit(position, row));
            itemView.setOnLongClickListener(v -> {
                listener.onDelete(position);
                return true;
            });
        }
    }
}
