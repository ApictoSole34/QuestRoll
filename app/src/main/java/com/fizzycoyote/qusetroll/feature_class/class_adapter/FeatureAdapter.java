package com.fizzycoyote.qusetroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_table_data.CustomTableData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.ViewHolder> {

    private static final Set<String> HIDDEN_TYPES = new HashSet<>(Arrays.asList(
            "CLASS_TABLE_DATA",
            "PROFICIENCY_BONUS"
    ));

    private List<CustomFeatureEntity> features = new ArrayList<>();
    private final OnFeatureClickListener listener;
    private boolean deleteEnabled = false;
    private Markwon markwon;

    public FeatureAdapter(OnFeatureClickListener listener) {
        this.listener = listener;
    }

    public interface OnFeatureClickListener {
        void onEdit(CustomFeatureEntity feature, int index);
        void onDelete(int index);
    }

    public void setDeleteEnabled(boolean enabled) {
        this.deleteEnabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (markwon == null) {
            markwon = Markwon.builder(parent.getContext())
                    .usePlugin(TablePlugin.create(parent.getContext()))
                    .usePlugin(HtmlPlugin.create())
                    .build();
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class_feature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(features.get(position), position, listener, markwon, deleteEnabled);
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    public void submitList(List<CustomFeatureEntity> newFeatures) {
        if (newFeatures == null) {
            features = new ArrayList<>();
        } else {
            features = newFeatures.stream()
                    .filter(f -> f.type == null || !HIDDEN_TYPES.contains(f.type))
                    .collect(Collectors.toList());
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView featureName, featureDesc;
        ImageButton btnDelete;
        TableLayout featureTable;

        ViewHolder(View itemView) {
            super(itemView);
            featureName = itemView.findViewById(R.id.tv_feature_name);
            featureDesc = itemView.findViewById(R.id.tv_feature_desc);
            btnDelete = itemView.findViewById(R.id.btn_delete_feature);
            featureTable = itemView.findViewById(R.id.table_layout);
        }

        void bind(CustomFeatureEntity feature, int position,
                  OnFeatureClickListener listener, Markwon markwon, boolean deleteEnabled) {

            featureName.setText(feature.name != null ? feature.name : "Unnamed feature");
            featureName.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.cinzel_bold));
            featureName.setTextColor(itemView.getContext().getResources().getColor(R.color.threads_text_primary, null));

            if (feature.description != null && !feature.description.isEmpty()) {
                featureDesc.setVisibility(View.VISIBLE);
                String fixedDescription = fixMarkdownHeader(feature.description);
                markwon.setMarkdown(featureDesc, fixedDescription);
                featureDesc.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.inter_regular));
                featureDesc.setTextColor(itemView.getContext().getResources().getColor(R.color.threads_text_primary, null));
            } else {
                featureDesc.setVisibility(View.GONE);
            }

            if (btnDelete != null) {
                btnDelete.setVisibility(deleteEnabled ? View.VISIBLE : View.GONE);
                if (deleteEnabled) {
                    btnDelete.setOnClickListener(v -> {
                        if (listener != null) listener.onDelete(position);
                    });
                }
            }

            featureTable.removeAllViews();
            if (feature.customTableData != null && !feature.customTableData.isEmpty()) {
                featureTable.setVisibility(View.VISIBLE);

                TableRow header = new TableRow(itemView.getContext());
                TextView lvlHeader = new TextView(itemView.getContext());
                TextView valHeader = new TextView(itemView.getContext());

                lvlHeader.setText("Level");
                valHeader.setText("Value");

                lvlHeader.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.cinzel_semibold));
                lvlHeader.setTextColor(itemView.getContext().getResources().getColor(R.color.threads_gold, null));
                valHeader.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.cinzel_semibold));
                valHeader.setTextColor(itemView.getContext().getResources().getColor(R.color.threads_gold, null));

                lvlHeader.setPadding(dp(8), dp(4), dp(8), dp(4));
                valHeader.setPadding(dp(8), dp(4), dp(8), dp(4));

                header.addView(lvlHeader);
                header.addView(valHeader);
                featureTable.addView(header);

                for (CustomTableData tableData : feature.customTableData) {
                    TableRow row = new TableRow(itemView.getContext());
                    TextView lvl = new TextView(itemView.getContext());
                    TextView val = new TextView(itemView.getContext());

                    lvl.setText(String.valueOf(tableData.level));
                    val.setText(tableData.columnValue);

                    lvl.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.inter_regular));
                    lvl.setTextColor(itemView.getContext().getResources().getColor(R.color.threads_text_primary, null));
                    val.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.inter_regular));
                    val.setTextColor(itemView.getContext().getResources().getColor(R.color.threads_text_primary, null));

                    lvl.setPadding(dp(8), dp(2), dp(8), dp(2));
                    val.setPadding(dp(8), dp(2), dp(8), dp(2));

                    row.addView(lvl);
                    row.addView(val);
                    featureTable.addView(row);
                }
            } else {
                featureTable.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(feature, position);
            });
        }

        private String fixMarkdownHeader(String text) {
            if (text == null) return null;
            return text.replaceAll("(#{1,3})(?=\\S)", "$1 ");
        }

        private int dp(int v) {
            return (int) (v * itemView.getContext().getResources().getDisplayMetrics().density);
        }
    }
}