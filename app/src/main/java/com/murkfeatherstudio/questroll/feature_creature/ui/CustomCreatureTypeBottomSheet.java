package com.murkfeatherstudio.questroll.feature_creature.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_creature_type.CustomCreatureTypeEntity;
import com.murkfeatherstudio.questroll.databinding.BottomSheetCustomCreatureTypesBinding;
import com.murkfeatherstudio.questroll.databinding.DialogCustomSchoolBinding;
import com.murkfeatherstudio.questroll.feature_creature.adapter.CustomCreatureTypeAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CustomCreatureTypeBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetCustomCreatureTypesBinding binding;
    private CustomCreatureTypeDao typeDao;
    private CustomCreatureDao creatureDao;
    private Executor executor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetCustomCreatureTypesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserContentDatabase db = UserContentDatabase.getInstance(requireContext());
        typeDao = db.customCreatureTypeDao();
        creatureDao = db.customCreatureDao();
        executor = Executors.newSingleThreadExecutor();

        binding.rvCreatureTypes.setLayoutManager(new LinearLayoutManager(requireContext()));

        CustomCreatureTypeAdapter adapter = new CustomCreatureTypeAdapter(
                this::showEditDialog,
                this::confirmDelete
        );
        binding.rvCreatureTypes.setAdapter(adapter);

        typeDao.getAll().observe(getViewLifecycleOwner(), adapter::submitList);

        binding.btnAddType.setOnClickListener(v -> showAddDialog());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showAddDialog() {
        DialogCustomSchoolBinding dialogBinding = DialogCustomSchoolBinding.inflate(LayoutInflater.from(requireContext()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Creature Type")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add", (d, w) -> {
                    String name = dialogBinding.etSchoolName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    executor.execute(() -> {
                        if (typeDao.countByName(name) > 0) return;
                        CustomCreatureTypeEntity t = new CustomCreatureTypeEntity();
                        t.name = name;
                        t.description = dialogBinding.etSchoolDesc.getText().toString().trim();
                        typeDao.insert(t);
                    });
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showEditDialog(CustomCreatureTypeEntity type) {
        DialogCustomSchoolBinding dialogBinding = DialogCustomSchoolBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.etSchoolName.setText(type.name);
        dialogBinding.etSchoolDesc.setText(type.description);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Creature Type")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (d, w) -> {
                    String name = dialogBinding.etSchoolName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    type.name = name;
                    type.description = dialogBinding.etSchoolDesc.getText().toString().trim();
                    executor.execute(() -> typeDao.update(type));
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void confirmDelete(CustomCreatureTypeEntity type) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Type")
                .setMessage("All creatures with type \"" + type.name
                        + "\" will be set to \"No Type\". Continue?")
                .setPositiveButton("Delete", (d, w) -> executor.execute(() -> {
                    List<CustomCreatureEntity> creatures =
                            creatureDao.getByTypeNameSync(type.name);
                    if (creatures != null) {
                        for (CustomCreatureEntity c : creatures) {
                            c.typeName = "No Type";
                            creatureDao.update(c);
                        }
                    }
                    typeDao.delete(type.id);
                }))
                .setNegativeButton("Cancel", null).show();
    }
}