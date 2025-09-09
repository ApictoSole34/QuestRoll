package com.fizzycoyote.qusetroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_table_data.CustomTableData;
import com.fizzycoyote.qusetroll.core.models.open5e.Converters;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.table_data.TableDataDto;

import java.util.ArrayList;
import java.util.List;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.ViewHolder> {
    private List<CustomFeatureEntity> features = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class_feature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomFeatureEntity feature = features.get(position);
        holder.bind(feature);
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    public void submitList(List<CustomFeatureEntity> newFeatures) {
        features = new ArrayList<>(newFeatures);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView featureName, featureDesc;
        TableLayout featureTable;

        ViewHolder(View itemView) {
            super(itemView);
            featureName = itemView.findViewById(R.id.tv_feature_name);
            featureDesc = itemView.findViewById(R.id.tv_feature_desc);
            featureTable = itemView.findViewById(R.id.table_layout);
        }

        void bind(CustomFeatureEntity feature) {
            featureName.setText(feature.name);
            featureDesc.setText(feature.description);
            featureTable.removeAllViews();

            // Bez konwersji przez JSON - bezpośrednio z customTableData
            for (CustomTableData tableData : feature.customTableData) {
                TableRow tableRow = new TableRow(itemView.getContext());

                TextView level = new TextView(itemView.getContext());
                level.setText(String.valueOf(tableData.level));
                level.setPadding(8, 2, 8, 2);

                TextView value = new TextView(itemView.getContext());
                value.setText(tableData.columnValue);
                value.setPadding(8, 2, 8, 2);

                tableRow.addView(level);
                tableRow.addView(value);
                featureTable.addView(tableRow);
            }
        }
    }
}