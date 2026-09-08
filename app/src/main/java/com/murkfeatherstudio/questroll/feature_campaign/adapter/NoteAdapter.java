package com.murkfeatherstudio.questroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignNoteEntity;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CampaignNoteEntity note = notes.get(position);
        holder.tvTitle.setText(note.title);
        holder.tvContentPreview.setText(note.content != null ? note.content : "");
        
        if (note.createdAt != null) {
            holder.tvDate.setText(dateFormat.format(note.createdAt));
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(note, position));
    }

    @Override
    public int getItemCount() { return notes.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvContentPreview;
        View btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_note_title);
            tvDate = itemView.findViewById(R.id.tv_note_date);
            tvContentPreview = itemView.findViewById(R.id.tv_note_preview);
            btnDelete = itemView.findViewById(R.id.btn_delete_note);
        }
    }
}