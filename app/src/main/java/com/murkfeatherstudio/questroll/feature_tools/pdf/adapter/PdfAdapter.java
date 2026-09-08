package com.murkfeatherstudio.questroll.feature_tools.pdf.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.custom.pdf.CustomPdfEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfAdapter extends ListAdapter<CustomPdfEntity, PdfAdapter.PdfViewHolder> {

    private final OnPdfClickListener clickListener;
    private final OnPdfDeleteListener deleteListener;

    public interface OnPdfClickListener {
        void onPdfClick(CustomPdfEntity pdf);
    }

    public interface OnPdfDeleteListener {
        void onPdfDelete(CustomPdfEntity pdf);
    }

    public PdfAdapter(OnPdfClickListener clickListener, OnPdfDeleteListener deleteListener) {
        super(new DiffUtil.ItemCallback<CustomPdfEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull CustomPdfEntity oldItem, @NonNull CustomPdfEntity newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull CustomPdfEntity oldItem, @NonNull CustomPdfEntity newItem) {
                return oldItem.name.equals(newItem.name) && oldItem.uri.equals(newItem.uri);
            }
        });
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener, deleteListener);
    }

    static class PdfViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvDate;
        private final ImageButton btnDelete;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_pdf_name);
            tvDate = itemView.findViewById(R.id.tv_pdf_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_pdf);
        }

        public void bind(CustomPdfEntity pdf, OnPdfClickListener clickListener, OnPdfDeleteListener deleteListener) {
            tvName.setText(pdf.name);
            tvDate.setText("Added: " + dateFormat.format(new Date(pdf.dateAdded)));
            
            itemView.setOnClickListener(v -> clickListener.onPdfClick(pdf));
            btnDelete.setOnClickListener(v -> deleteListener.onPdfDelete(pdf));
        }
    }
}
