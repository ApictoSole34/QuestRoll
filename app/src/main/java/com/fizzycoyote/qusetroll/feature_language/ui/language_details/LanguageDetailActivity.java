package com.fizzycoyote.qusetroll.feature_language.ui.language_details;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentEntity;
import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;
import com.fizzycoyote.qusetroll.feature_language.ui.language_create.CustomLanguageCreateActivity;
import com.fizzycoyote.qusetroll.feature_language.model.CombinedLanguage;
import com.fizzycoyote.qusetroll.feature_language.view_model.LanguageDetailViewModel;
import com.fizzycoyote.qusetroll.feature_language.view_model.ViewModelFactory;

import java.util.Objects;
import java.util.concurrent.Executors;


public class LanguageDetailActivity extends AppCompatActivity {

    public static final int REQUEST_EDIT = 1001;
    public static final String EXTRA_LANGUAGE = "combinedLanguage";

    private LanguageDetailViewModel viewModel;
    private TextView tvName, tvDesc, tvFlags, tvScript, tvLicense;
    private Button btnManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_detail);

        LanguageRepository repo = new LanguageRepository(
                Open5eDatabase.getInstance(this).documentDao(),
                Open5eDatabase.getInstance(this).languageDao(),
                UserContentDatabase.getInstance(this).customLanguageDao(),
                Executors.newSingleThreadExecutor()
        );

        viewModel = new ViewModelProvider(this, new ViewModelFactory(repo))
                .get(LanguageDetailViewModel.class);

        initViews();
        setupObservers();
        handleInitialIntent();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_detail_name);
        tvDesc = findViewById(R.id.tv_detail_desc);
        tvFlags = findViewById(R.id.tv_detail_flags);
        tvScript = findViewById(R.id.tv_detail_script);
        tvLicense = findViewById(R.id.tv_detail_license);
        btnManage = findViewById(R.id.btn_manage);
    }

    private void setupObservers() {
        viewModel.getLanguage().observe(this, language -> {
            if (language != null) {
                updateMainInfo(language);
                updateScriptSection(language);
                updateManageButton(language);
            }
        });

        viewModel.getDocument().observe(this, doc -> {
            CombinedLanguage currentLanguage = viewModel.getLanguage().getValue();
            if (currentLanguage != null) {
                updateLicenseInfo(currentLanguage, doc);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) showToast(error);
        });

        viewModel.getDeleteSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Language deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void handleInitialIntent() {
        CombinedLanguage initial = getIntent().getParcelableExtra(EXTRA_LANGUAGE);
        if (initial == null) {
            finish();
            return;
        }

        if (initial.isCustom()) {
            viewModel.loadLanguage(null, initial.getCustomId());
        } else {
            viewModel.loadLanguage(initial.getOpen5eKey(), null);

            String documentKey = extractKeyFromUrl(initial.getDocumentUrl());
            if (documentKey != null) {
                viewModel.loadDocument(documentKey);
            } else {
                Log.e("License", "Could not extract document key from URL");
            }
        }
    }

    private String extractKeyFromUrl(String url) {
        if (url == null) return null;

        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = u.split("/");

        if (parts.length > 0) {
            return parts[parts.length - 1];
        }

        return null;
    }

    private void updateMainInfo(CombinedLanguage language) {
        tvName.setText(language.getName());
        tvDesc.setText(language.getDesc());

        String flags = "Exotic: " + (language.isExotic() ? "Yes" : "No") +
                "\nSecret: " + (language.isSecret() ? "Yes" : "No");
        tvFlags.setText(flags);
    }

    private void updateScriptSection(CombinedLanguage language) {
        if (language.getScriptLanguageName() != null && !isSelfReference(language)) {
            tvScript.setText("Script: " + language.getScriptLanguageName());
            tvScript.setOnClickListener(v -> handleScriptClick(language));
            tvScript.setVisibility(View.VISIBLE);
        } else {
            tvScript.setVisibility(View.GONE);
        }
    }

    private void updateManageButton(CombinedLanguage language) {
        if (language.getCustomId() != null) {
            btnManage.setVisibility(View.VISIBLE);
            btnManage.setOnClickListener(v -> showManageDialog(language));
        } else {
            btnManage.setVisibility(View.GONE);
        }
    }

    private void updateLicenseInfo(CombinedLanguage language, DocumentEntity doc) {
        if (language.isCustom()) {
            tvLicense.setVisibility(View.GONE);
            tvLicense.setOnClickListener(null);
            return;
        }

        if (doc != null) {
            tvLicense.setText("License: " + doc.name);
            tvLicense.setOnClickListener(v -> showLicenseDialog(doc.key));
            tvLicense.setVisibility(View.VISIBLE);
        } else {
            tvLicense.setText("License");
            tvLicense.setOnClickListener(null);
            tvLicense.setVisibility(View.VISIBLE);
        }
        tvLicense.setClickable(true);
        tvLicense.setFocusable(true);
    }

    private boolean isSelfReference(CombinedLanguage language) {
        String currentId = language.isCustom() ?
                String.valueOf(language.getCustomId()) : language.getOpen5eKey();
        return Objects.equals(language.getScriptKey(), currentId);
    }

    private void handleScriptClick(CombinedLanguage language) {
        viewModel.getScriptLanguage(language.getScriptKey(),
                script -> openLanguageDetails(script),
                e -> showToast("Error loading script: " + e.getMessage())
        );
    }

    private void showManageDialog(CombinedLanguage language) {
        new AlertDialog.Builder(this)
                .setTitle("Manage Language")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) openEditActivity(language);
                    else showDeleteConfirmation(language);
                })
                .show();
    }

    private void openEditActivity(CombinedLanguage language) {
        if (language.getCustomId() == null) return;

        Intent intent = CustomLanguageCreateActivity.getEditIntent(
                this,
                language.getCustomId()
        );
        startActivityForResult(intent, REQUEST_EDIT);
    }

    private void showDeleteConfirmation(CombinedLanguage language) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Language")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (d, w) -> {
                    if (language.getCustomId() != null) {
                        viewModel.deleteLanguage(language.getCustomId());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            CombinedLanguage current = viewModel.getLanguage().getValue();
            if (current != null) viewModel.loadLanguage(null, current.getCustomId());
        }
    }

    private void openLanguageDetails(CombinedLanguage language) {
        Intent intent = new Intent(this, LanguageDetailActivity.class);
        intent.putExtra(EXTRA_LANGUAGE, language);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLicenseDialog(String key) {
        DocumentDetailDialogFragment.newInstance(key)
                .show(getSupportFragmentManager(), "license_dialog");
    }
}