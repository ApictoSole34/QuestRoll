package com.murkfeatherstudio.questroll.feature_language.ui.language_create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.databinding.ActivityCustomLanguageCreateBinding;
import com.murkfeatherstudio.questroll.feature_language.data.repository.LanguageRepository;
import com.murkfeatherstudio.questroll.feature_language.model.item.ScriptItem;
import com.murkfeatherstudio.questroll.feature_language.view_model.CustomLanguageCreateViewModel;
import com.murkfeatherstudio.questroll.feature_language.view_model.ViewModelFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CustomLanguageCreateActivity extends BaseActivity {
    private ActivityCustomLanguageCreateBinding binding;
    private CustomLanguageCreateViewModel viewModel;
    private ArrayAdapter<ScriptItem> scriptAdapter;
    private long existingLanguageId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomLanguageCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LanguageRepository repo = new LanguageRepository(
                Open5eDatabase.getInstance(this).documentDao(),
                Open5eDatabase.getInstance(this).languageDao(),
                UserContentDatabase.getInstance(this).customLanguageDao(),
                Executors.newSingleThreadExecutor()
        );
        viewModel = new ViewModelProvider(this, new ViewModelFactory(repo))
                .get(CustomLanguageCreateViewModel.class);

        setupUI();
        setupObservers();

        existingLanguageId = getIntent().getLongExtra("custom_id", -1);
        if (existingLanguageId != -1) {
            // Edit Mode: Load existing language and exclude self from script options
            viewModel.loadExistingLanguage(existingLanguageId);
            viewModel.loadScriptOptions(existingLanguageId);
            binding.btnSave.setText("Update");
        } else {
            // Create Mode: Show all script options
            viewModel.loadScriptOptions(-1);
        }
    }

    private void setupUI() {
        scriptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        scriptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerScript.setAdapter(scriptAdapter);

        binding.btnSave.setOnClickListener(v -> attemptSave());
    }

    private void attemptSave() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.etName.setError("Required");
            return;
        }

        String desc = binding.etDesc.getText() != null ? binding.etDesc.getText().toString().trim() : "";
        boolean exotic = binding.cbExotic.isChecked();
        boolean secret = binding.cbSecret.isChecked();
        ScriptItem selectedScript = (ScriptItem) binding.spinnerScript.getSelectedItem();
        String scriptId = selectedScript != null ? selectedScript.scriptIdentifier : null;

        viewModel.saveLanguage(
                name,
                desc,
                exotic,
                secret,
                scriptId,
                existingLanguageId
        );
    }

    private void setupObservers() {
        viewModel.getScriptOptions().observe(this, items -> {
            scriptAdapter.clear();
            scriptAdapter.addAll(items);
        });

        viewModel.getExistingLanguage().observe(this, entity -> {
            if (entity != null) {
                binding.etName.setText(entity.name);
                binding.etDesc.setText(entity.desc);
                binding.cbExotic.setChecked(entity.isExotic);
                binding.cbSecret.setChecked(entity.isSecret);
                selectScriptInSpinner(entity.scriptLanguageId);
            }
        });

        viewModel.getSaveResult().observe(this, success -> {
            if (success != null) {
                if (success) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Language name already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void selectScriptInSpinner(String scriptId) {
        List<ScriptItem> items = viewModel.getScriptOptions().getValue();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                if (Objects.equals(items.get(i).scriptIdentifier, scriptId)) {
                    binding.spinnerScript.setSelection(i);
                    return;
                }
            }
        }
        binding.spinnerScript.setSelection(0);
    }

    public static Intent getEditIntent(Context context, long languageId) {
        Intent intent = new Intent(context, CustomLanguageCreateActivity.class);
        intent.putExtra("custom_id", languageId);
        return intent;
    }
}