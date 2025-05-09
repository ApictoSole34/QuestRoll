package com.fizzycoyote.qusetroll.feature_language.ui.language_create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;
import com.fizzycoyote.qusetroll.feature_language.model.item.ScriptItem;
import com.fizzycoyote.qusetroll.feature_language.view_model.CustomLanguageCreateViewModel;
import com.fizzycoyote.qusetroll.feature_language.view_model.ViewModelFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CustomLanguageCreateActivity extends AppCompatActivity {
    private EditText etName, etDesc;
    private CheckBox cbExotic, cbSecret;
    private Spinner spinnerScript;
    private Button btnSave;
    private CustomLanguageCreateViewModel viewModel;
    private ArrayAdapter<ScriptItem> scriptAdapter;
    private long existingLanguageId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_language_create);

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
            btnSave.setText("Update");
        } else {
            // Create Mode: Show all script options
            viewModel.loadScriptOptions(-1);
        }
    }

    private void setupUI() {
        etName = findViewById(R.id.et_name);
        etDesc = findViewById(R.id.et_desc);
        cbExotic = findViewById(R.id.cb_exotic);
        cbSecret = findViewById(R.id.cb_secret);
        spinnerScript = findViewById(R.id.spinner_script);
        btnSave = findViewById(R.id.btn_save);

        scriptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        scriptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerScript.setAdapter(scriptAdapter);

        btnSave.setOnClickListener(v -> attemptSave());
    }

    private void attemptSave() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }

        String desc = etDesc.getText().toString().trim();
        boolean exotic = cbExotic.isChecked();
        boolean secret = cbSecret.isChecked();
        ScriptItem selectedScript = (ScriptItem) spinnerScript.getSelectedItem();
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
                etName.setText(entity.name);
                etDesc.setText(entity.desc);
                cbExotic.setChecked(entity.isExotic);
                cbSecret.setChecked(entity.isSecret);
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
                    spinnerScript.setSelection(i);
                    return;
                }
            }
        }
        spinnerScript.setSelection(0);
    }

    public static Intent getEditIntent(Context context, long languageId) {
        Intent intent = new Intent(context, CustomLanguageCreateActivity.class);
        intent.putExtra("custom_id", languageId);
        return intent;
    }
}