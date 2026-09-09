package com.murkfeatherstudio.questroll.feature_language.ui.language_details;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.feature_document.fragment.DocumentDetailDialogFragment;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityLanguageDetailBinding;
import com.murkfeatherstudio.questroll.feature_language.data.repository.LanguageRepository;
import com.murkfeatherstudio.questroll.feature_language.ui.language_create.CustomLanguageCreateActivity;
import com.murkfeatherstudio.questroll.feature_language.model.CombinedLanguage;
import com.murkfeatherstudio.questroll.feature_language.view_model.LanguageDetailViewModel;
import com.murkfeatherstudio.questroll.feature_language.view_model.ViewModelFactory;

import java.util.Objects;
import java.util.concurrent.Executors;


public class LanguageDetailActivity extends BaseActivity {

    public static final int REQUEST_EDIT = 1001;
    public static final String EXTRA_LANGUAGE = "combinedLanguage";

    private LanguageDetailViewModel viewModel;
    private ActivityLanguageDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguageDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LanguageRepository repo = new LanguageRepository(
                Open5eDatabase.getInstance(this).documentDao(),
                Open5eDatabase.getInstance(this).languageDao(),
                UserContentDatabase.getInstance(this).customLanguageDao(),
                Executors.newSingleThreadExecutor()
        );

        viewModel = new ViewModelProvider(this, new ViewModelFactory(repo))
                .get(LanguageDetailViewModel.class);

        setupObservers();
        handleInitialIntent();
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
        binding.tvDetailName.setText(language.getName());
        binding.tvDetailDesc.setText(language.getDesc());

        String flags = "Exotic: " + (language.isExotic() ? "Yes" : "No") +
                "\nSecret: " + (language.isSecret() ? "Yes" : "No");
        binding.tvDetailFlags.setText(flags);
    }

    private void updateScriptSection(CombinedLanguage language) {
        if (language.getScriptLanguageName() != null && !isSelfReference(language)) {
            binding.tvDetailScript.setText("Script: " + language.getScriptLanguageName());
            binding.tvDetailScript.setOnClickListener(v -> handleScriptClick(language));
            binding.tvDetailScript.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailScript.setVisibility(View.GONE);
        }
    }

    private void updateManageButton(CombinedLanguage language) {
        if (language.getCustomId() != null) {
            binding.btnManage.setVisibility(View.VISIBLE);
            binding.btnManage.setOnClickListener(v -> showManageDialog(language));
        } else {
            binding.btnManage.setVisibility(View.GONE);
        }
    }

    private void updateLicenseInfo(CombinedLanguage language, DocumentEntity doc) {
        if (language.isCustom()) {
            binding.tvDetailLicense.setVisibility(View.GONE);
            binding.tvDetailLicense.setOnClickListener(null);
            return;
        }

        if (doc != null) {
            binding.tvDetailLicense.setText("License: " + doc.name);
            binding.tvDetailLicense.setOnClickListener(v -> showLicenseDialog(doc.key));
            binding.tvDetailLicense.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailLicense.setText("License");
            binding.tvDetailLicense.setOnClickListener(null);
            binding.tvDetailLicense.setVisibility(View.VISIBLE);
        }
        binding.tvDetailLicense.setClickable(true);
        binding.tvDetailLicense.setFocusable(true);
    }

    private boolean isSelfReference(CombinedLanguage language) {
        String currentId = language.isCustom() ?
                String.valueOf(language.getCustomId()) : language.getOpen5eKey();
        return Objects.equals(language.getScriptKey(), currentId);
    }

    private void handleScriptClick(CombinedLanguage language) {
        viewModel.getScriptLanguage(language.getScriptKey(),
                this::openLanguageDetails,
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
