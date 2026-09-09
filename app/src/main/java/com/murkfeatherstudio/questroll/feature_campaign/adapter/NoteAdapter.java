package com.murkfeatherstudio.questroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.campaign.CampaignNoteEntity;
import com.murkfeatherstudio.questroll.databinding.ItemNoteBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private List<CampaignNoteEntity> notes = new ArrayList<>();
    private final OnNoteClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnNoteClickListener {
        void onNoteClick(CampaignNoteEntity note);
        void onDeleteClick(CampaignNoteEntity note, int position);
    }

    public NoteAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<CampaignNoteEntity> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoteBinding binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CampaignNoteEntity note = notes.get(position);
        holder.binding.tvNoteTitle.setText(note.title);
        holder.binding.tvNotePreview.setText(note.content != null ? note.content : "");
        
        if (note.createdAt != null) {
            holder.binding.tvNoteDate.setText(dateFormat.format(note.createdAt));
            holder.binding.tvNoteDate.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.tvNoteDate.setVisibility(android.view.View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));
        holder.binding.btnDeleteNote.setOnClickListener(v -> listener.onDeleteClick(note, position));
    }

    @Override
    public int getItemCount() { return notes.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemNoteBinding binding;
        ViewHolder(ItemNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}