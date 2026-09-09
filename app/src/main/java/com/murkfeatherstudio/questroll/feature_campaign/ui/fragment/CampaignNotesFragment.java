package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignNoteEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentCampaignNotesBinding;
import com.murkfeatherstudio.questroll.databinding.DialogAddNoteBinding;
import com.murkfeatherstudio.questroll.feature_campaign.adapter.NoteAdapter;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.CampaignDetailViewModel;

public class CampaignNotesFragment extends Fragment {

    private static final String ARG_CAMPAIGN_ID = "campaign_id";
    private long campaignId;
    private CampaignDetailViewModel viewModel;
    private NoteAdapter adapter;
    private FragmentCampaignNotesBinding binding;

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
        binding = FragmentCampaignNotesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.recyclerNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
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
        binding.recyclerNotes.setAdapter(adapter);

        viewModel.notes.observe(getViewLifecycleOwner(), notes -> {
            if (notes != null) adapter.setNotes(notes);
        });

        binding.btnAddNote.setOnClickListener(v -> showAddNoteDialog());
    }

    private void showAddNoteDialog() {
        DialogAddNoteBinding dialogBinding = DialogAddNoteBinding.inflate(getLayoutInflater());
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Note")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (d, which) -> {
                    String title = dialogBinding.etNoteTitle.getText().toString().trim();
                    String content = dialogBinding.etNoteContent.getText().toString().trim();
                    if (!title.isEmpty()) viewModel.addNote(title, content);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditNoteDialog(CampaignNoteEntity note) {
        DialogAddNoteBinding dialogBinding = DialogAddNoteBinding.inflate(getLayoutInflater());
        dialogBinding.etNoteTitle.setText(note.title);
        dialogBinding.etNoteContent.setText(note.content);
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Note")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Update", (d, which) -> {
                    String newTitle = dialogBinding.etNoteTitle.getText().toString().trim();
                    String newContent = dialogBinding.etNoteContent.getText().toString().trim();
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
