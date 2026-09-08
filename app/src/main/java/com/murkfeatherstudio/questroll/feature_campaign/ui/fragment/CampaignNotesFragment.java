package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignNoteEntity;
import com.murkfeatherstudio.questroll.feature_campaign.adapter.NoteAdapter;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.CampaignDetailViewModel;

public class CampaignNotesFragment extends Fragment {

    private static final String ARG_CAMPAIGN_ID = "campaign_id";
    private long campaignId;
    private CampaignDetailViewModel viewModel;
    private NoteAdapter adapter;

    public static CampaignNotesFragment newInstance(long campaignId) {
        Bundle args = new Bundle();
        args.putLong(ARG_CAMPAIGN_ID, campaignId);
        CampaignNotesFragment fragment = new CampaignNotesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            campaignId = getArguments().getLong(ARG_CAMPAIGN_ID);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(CampaignDetailViewModel.class);
        viewModel.setCampaignId(campaignId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_campaign_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NoteAdapter(new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(CampaignNoteEntity note) {
                showEditNoteDialog(note);
            }
            @Override
            public void onDeleteClick(CampaignNoteEntity note, int position) {
                viewModel.deleteNote(note);
            }
        });
        recyclerView.setAdapter(adapter);

        viewModel.notes.observe(getViewLifecycleOwner(), notes -> {
            if (notes != null) adapter.setNotes(notes);
        });

        Button btnAdd = view.findViewById(R.id.btn_add_note);
        btnAdd.setOnClickListener(v -> showAddNoteDialog());
    }

    private void showAddNoteDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_note, null);
        android.widget.EditText etTitle = dialogView.findViewById(R.id.et_note_title);
        android.widget.EditText etContent = dialogView.findViewById(R.id.et_note_content);
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Note")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (!title.isEmpty()) viewModel.addNote(title, content);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditNoteDialog(CampaignNoteEntity note) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_note, null);
        android.widget.EditText etTitle = dialogView.findViewById(R.id.et_note_title);
        android.widget.EditText etContent = dialogView.findViewById(R.id.et_note_content);
        etTitle.setText(note.title);
        etContent.setText(note.content);
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Note")
                .setView(dialogView)
                .setPositiveButton("Update", (d, which) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newContent = etContent.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        note.title = newTitle;
                        note.content = newContent;
                        viewModel.updateNote(note);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
