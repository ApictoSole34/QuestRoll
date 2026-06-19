package com.fizzycoyote.qusetroll.feature_class.ui;

import android.content.Intent;
import android.os.Bundle;

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
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassListViewModel;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassListViewModelFactory;

import java.util.concurrent.Executors;

public class ClassListActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private ClassListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        recyclerView = findViewById(R.id.recycler_view);
        setupRecyclerView();
        setupViewModel();
        observeData();
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
                Intent intent = new Intent(ClassListActivity.this, ClassCreateActivity.class);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void openClassDetails(CombinedClass classEntity) {
        Intent intent = new Intent(this, ClassDetailActivity.class);
        intent.putExtra("CLASS_KEY", classEntity.id);
        startActivity(intent);
    }

    private void createNewClass() {
        Intent intent = new Intent(this, ClassCreateActivity.class);
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
