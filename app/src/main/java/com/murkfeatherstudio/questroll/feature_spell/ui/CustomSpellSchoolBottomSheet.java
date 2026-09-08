package com.murkfeatherstudio.questroll.feature_spell.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellSchoolEntity;
import com.murkfeatherstudio.questroll.feature_spell.adapter.CustomSpellSchoolAdapter;
import com.murkfeatherstudio.questroll.feature_spell.view_model.CustomSpellSchoolViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.concurrent.Executors;

public class CustomSpellSchoolBottomSheet extends BottomSheetDialogFragment {

    private CustomSpellSchoolViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_custom_schools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserContentDatabase db = UserContentDatabase.getInstance(requireContext());
        viewModel = new ViewModelProvider(this,
                new CustomSpellSchoolViewModel.Factory(
                        db.customSpellSchoolDao(),
                        db.customSpellDao(),
                        Executors.newSingleThreadExecutor()
                )).get(CustomSpellSchoolViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_custom_schools);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        CustomSpellSchoolAdapter adapter = new CustomSpellSchoolAdapter(
                school -> showEditDialog(school),
                school -> confirmDelete(school)
        );
        recyclerView.setAdapter(adapter);

        viewModel.getAllSchools().observe(getViewLifecycleOwner(), adapter::submitList);

        view.findViewById(R.id.btnAddSchool).setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_custom_school, null);
        EditText etName = dialogView.findViewById(R.id.etSchoolName);
        EditText etDesc = dialogView.findViewById(R.id.etSchoolDesc);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Custom School")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        viewModel.addSchool(name, etDesc.getText().toString().trim());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(CustomSpellSchoolEntity school) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_custom_school, null);
        EditText etName = dialogView.findViewById(R.id.etSchoolName);
        EditText etDesc = dialogView.findViewById(R.id.etSchoolDesc);

        etName.setText(school.name);
        etDesc.setText(school.description);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit School")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        school.name = name;
                        school.description = etDesc.getText().toString().trim();
                        viewModel.updateSchool(school);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(CustomSpellSchoolEntity school) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete School")
                .setMessage("All spells with school \"" + school.name
                        + "\" will be set to \"No School\". Continue?")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteSchool(school))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
