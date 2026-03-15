package com.fizzycoyote.qusetroll.feature_class.class_adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_class.model.ClassTableEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassTableAdapter extends ListAdapter<ClassTableEntry, ClassTableAdapter.ViewHolder> {
    private final List<String> columnHeaders;
    private final Map<String, Integer> columnWidths;

    public ClassTableAdapter(List<String> columnHeaders, Map<String, Integer> columnWidths) {
        super(DIFF_CALLBACK);
        this.columnHeaders = columnHeaders;
        this.columnWidths = columnWidths;
    }

    private static final DiffUtil.ItemCallback<ClassTableEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<ClassTableEntry>() {
        @Override
        public boolean areItemsTheSame(@NonNull ClassTableEntry oldItem, @NonNull ClassTableEntry newItem) {
            return oldItem.level == newItem.level;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ClassTableEntry oldItem, @NonNull ClassTableEntry newItem) {
            return oldItem.columnData.equals(newItem.columnData);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_table_dynamic, parent, false);
        return new ViewHolder(view, columnHeaders, columnWidths);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassTableEntry entry = getItem(position);
        holder.bind(entry);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final List<TextView> columnViews;
        private final List<String> columnHeaders;
        private final Map<String, Integer> columnWidths;

        ViewHolder(View itemView, List<String> columnHeaders, Map<String, Integer> columnWidths) {
            super(itemView);
            this.columnHeaders = columnHeaders;
            this.columnWidths = columnWidths;
            this.columnViews = new ArrayList<>();

            TableRow tableRow = itemView.findViewById(R.id.table_row);
            tableRow.removeAllViews();

            for (int i = 0; i < columnHeaders.size(); i++) {
                String columnName = columnHeaders.get(i);
                TextView textView = new TextView(itemView.getContext());

                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        columnWidths.getOrDefault(columnName, dpToPx(100)),
                        TableRow.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1));

                textView.setLayoutParams(params);
                textView.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
                textView.setBackgroundResource(android.R.drawable.edit_text);
                textView.setTextSize(12);
                textView.setGravity(Gravity.CENTER);
                textView.setSingleLine(false);
                textView.setMaxLines(2);
                textView.setEllipsize(TextUtils.TruncateAt.END);

                if (i == 0) {
                    textView.setTypeface(null, Typeface.BOLD);
                    textView.setBackgroundColor(Color.parseColor("#F0F0F0"));
                }

                tableRow.addView(textView);
                columnViews.add(textView);
            }
        }

        void bind(ClassTableEntry entry) {
            for (int i = 0; i < columnHeaders.size(); i++) {
                String columnName = columnHeaders.get(i);
                String value = entry.columnData.getOrDefault(columnName, "-");
                columnViews.get(i).setText(value);
            }
        }

        private int dpToPx(int dp) {
            return (int) (dp * itemView.getContext().getResources().getDisplayMetrics().density);
        }
    }
}