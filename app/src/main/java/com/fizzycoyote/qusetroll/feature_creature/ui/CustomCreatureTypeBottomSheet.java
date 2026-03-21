package com.fizzycoyote.qusetroll.feature_creature.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureTypeDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_creature.CustomCreatureTypeEntity;
import com.fizzycoyote.qusetroll.feature_creature.adapter.CustomCreatureTypeAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CustomCreatureTypeBottomSheet extends BottomSheetDialogFragment {

    private CustomCreatureTypeDao typeDao;
    private CustomCreatureDao creatureDao;
    private Executor executor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_custom_creature_types, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserContentDatabase db = UserContentDatabase.getInstance(requireContext());
        typeDao = db.customCreatureTypeDao();
        creatureDao = db.customCreatureDao();
        executor = Executors.newSingleThreadExecutor();

        RecyclerView rv = view.findViewById(R.id.rv_creature_types);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        CustomCreatureTypeAdapter adapter = new CustomCreatureTypeAdapter(
                this::showEditDialog,
                this::confirmDelete
        );
        rv.setAdapter(adapter);

        typeDao.getAll().observe(getViewLifecycleOwner(), adapter::submitList);

        view.findViewById(R.id.btnAddType).setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View dv = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_custom_school, null);
        EditText etName = dv.findViewById(R.id.etSchoolName);
        EditText etDesc = dv.findViewById(R.id.etSchoolDesc);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Creature Type")
                .setView(dv)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    executor.execute(() -> {
                        if (typeDao.countByName(name) > 0) return;
                        CustomCreatureTypeEntity t = new CustomCreatureTypeEntity();
                        t.name = name;
                        t.description = etDesc.getText().toString().trim();
                        typeDao.insert(t);
                    });
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showEditDialog(CustomCreatureTypeEntity type) {
        View dv = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_custom_school, null);
        EditText etName = dv.findViewById(R.id.etSchoolName);
        EditText etDesc = dv.findViewById(R.id.etSchoolDesc);
        etName.setText(type.name);
        etDesc.setText(type.description);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Creature Type")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    type.name = name;
                    type.description = etDesc.getText().toString().trim();
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