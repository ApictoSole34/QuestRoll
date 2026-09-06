package com.fizzycoyote.qusetroll.feature_class.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.ClassAdapter;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;
import com.fizzycoyote.qusetroll.feature_class.repository.ClassRepository;
import com.fizzycoyote.qusetroll.feature_class.ui.wizard.ClassWizardActivity;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassListViewModel;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassListViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executors;

public class ClassListActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private ClassListViewModel viewModel;
    private AlertDialog createClassDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        recyclerView = findViewById(R.id.recycler_view);
        setupRecyclerView();
        setupViewModel();
        observeData();

        // 🔥 FAB – otwiera dialog wyboru
        FloatingActionButton fabCreate = findViewById(R.id.fab_create_class);
        if (fabCreate != null) {
            fabCreate.setOnClickListener(v -> showCreateClassDialog());
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(new ClassAdapter.OnItemClickListener() {
            @Override
            public void onClassClick(CombinedClass classEntity) {
                Intent intent = new Intent(ClassListActivity.this, ClassDetailActivity.class);
                intent.putExtra("CLASS_KEY", classEntity.id);
                startActivity(intent);
            }

            @Override
            public void onCreateClick() {
                // 🔥 Zmienione – teraz pokazuje dialog zamiast otwierać Wizarda
                showCreateClassDialog();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showCreateClassDialog() {
        if (createClassDialog != null && createClassDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_class_choice, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        createClassDialog = builder.create();
        createClassDialog.show();

        // Przyciski
        Button btnNewClass = dialogView.findViewById(R.id.btn_new_class);
        Button btnNewSubclass = dialogView.findViewById(R.id.btn_new_subclass);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnNewClass.setOnClickListener(v -> {
            createClassDialog.dismiss();
            openWizard(false); // false = klasa bazowa
        });

        btnNewSubclass.setOnClickListener(v -> {
            createClassDialog.dismiss();
            openWizard(true); // true = subklasa
        });

        btnCancel.setOnClickListener(v -> createClassDialog.dismiss());
    }

    private void openWizard(boolean isSubclass) {
        Intent intent = new Intent(this, ClassWizardActivity.class);
        intent.putExtra("is_subclass", isSubclass);
        startActivity(intent);
    }

    private void setupViewModel() {
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        ClassRepository repository = new ClassRepository(
                open5eDb.characterClassDao(),
                customDb.customCharacterClassDao(),
                Executors.newSingleThreadExecutor()
        );

        ClassListViewModel.Factory factory = new ClassListViewModel.Factory(repository);
        viewModel = new ViewModelProvider(this, factory).get(ClassListViewModel.class);
    }

    private void observeData() {
        viewModel.getCombinedClasses().observe(this, classes -> {
            if (classes != null) {
                adapter.submitList(classes);
            }
        });
    }
}