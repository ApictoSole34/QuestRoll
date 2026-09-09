package com.murkfeatherstudio.questroll.feature_tools.pdf.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.custom.pdf.CustomPdfEntity;
import com.murkfeatherstudio.questroll.databinding.ItemPdfBinding;

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
        ItemPdfBinding binding = ItemPdfBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PdfViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener, deleteListener);
    }

    static class PdfViewHolder extends RecyclerView.ViewHolder {
        private final ItemPdfBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        public PdfViewHolder(@NonNull ItemPdfBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CustomPdfEntity pdf, OnPdfClickListener clickListener, OnPdfDeleteListener deleteListener) {
            binding.tvPdfName.setText(pdf.name);
            binding.tvPdfDate.setText("Added: " + dateFormat.format(new Date(pdf.dateAdded)));
            
            itemView.setOnClickListener(v -> clickListener.onPdfClick(pdf));
            binding.btnDeletePdf.setOnClickListener(v -> deleteListener.onPdfDelete(pdf));
        }
    }
}
