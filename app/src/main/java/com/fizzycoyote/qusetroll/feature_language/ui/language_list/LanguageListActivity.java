package com.fizzycoyote.qusetroll.feature_language.ui.language_list;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;
import com.fizzycoyote.qusetroll.feature_language.ui.language_create.CustomLanguageCreateActivity;
import com.fizzycoyote.qusetroll.feature_language.ui.language_details.LanguageDetailActivity;
import com.fizzycoyote.qusetroll.feature_language.ui.language_list.adapter.LanguageListAdapter;
import com.fizzycoyote.qusetroll.feature_language.model.CombinedLanguage;
import com.fizzycoyote.qusetroll.feature_language.view_model.LanguageListViewModel;
import com.fizzycoyote.qusetroll.feature_language.view_model.ViewModelFactory;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class LanguageListActivity extends BaseActivity {
    private LanguageListAdapter adapter;
    private LanguageListViewModel viewModel;
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_list);

        LanguageRepository repo = new LanguageRepository(
                Open5eDatabase.getInstance(this).documentDao(),
                Open5eDatabase.getInstance(this).languageDao(),
                UserContentDatabase.getInstance(this).customLanguageDao(),
                Executors.newSingleThreadExecutor()
        );

        viewModel = new ViewModelProvider(this, new ViewModelFactory(repo))
                .get(LanguageListViewModel.class);

        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_languages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LanguageListAdapter(
                new ArrayList<>(),
                this::openDetails,
                this::openCreateLanguage
        );
        rv.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getCombinedLanguages().observe(this, list -> {
            adapter.updateList(list);

            if (isFirstLoad && !list.isEmpty()) {
                scrollToTop();
                isFirstLoad = false;
            }
        });
    }

    private void scrollToTop() {
        RecyclerView rv = findViewById(R.id.rv_languages);
        rv.scrollToPosition(0);
    }

    private void openDetails(CombinedLanguage lang) {
        Intent i = new Intent(this, LanguageDetailActivity.class);
        i.putExtra(LanguageDetailActivity.EXTRA_LANGUAGE, lang);
        startActivity(i);
    }

    private void openCreateLanguage() {
        startActivity(new Intent(this, CustomLanguageCreateActivity.class));
    }
}