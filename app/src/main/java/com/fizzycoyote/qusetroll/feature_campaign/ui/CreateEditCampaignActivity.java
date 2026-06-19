package com.fizzycoyote.qusetroll.feature_campaign.ui;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.core.local_database.CampaignDatabase;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.campaign.CampaignEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateEditCampaignActivity extends BaseActivity {

    public static final String EXTRA_CAMPAIGN_ID = "campaign_id";

    private EditText etName, etDescription;
    private Spinner spinnerGameSystem;
    private TextView tvSelectedCharacter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<String> gameSystemKeys = new ArrayList<>();
    private final List<String> gameSystemNames = new ArrayList<>();

    private long editId = -1L;
    private long selectedCharacterId = -1L;
    private String selectedCharacterName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_campaign);

        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        spinnerGameSystem = findViewById(R.id.spinner_game_system);
        tvSelectedCharacter = findViewById(R.id.tv_selected_character);
        Button btnSelectCharacter = findViewById(R.id.btn_select_character);
        Button btnSave = findViewById(R.id.btn_save);

        editId = getIntent().getLongExtra(EXTRA_CAMPAIGN_ID, -1L);
        setTitle(editId != -1L ? "Edit Campaign" : "New Campaign");

        loadGameSystems();

        btnSelectCharacter.setOnClickListener(v -> showCharacterPickerDialog());
        btnSave.setOnClickListener(v -> saveCampaign());
    }

    private void loadGameSystems() {
        executor.execute(() -> {
            List<GameSystemEntity> systems = Open5eDatabase.getInstance(this)
                    .gameSystemDao().getAllGameSystems();

            List<String> keys = new ArrayList<>();
            List<String> names = new ArrayList<>();

            if (systems == null || systems.isEmpty()) {
                keys.add("5e-2014");
                names.add("D&D 5e (2014 Rules)");
            } else {
                for (GameSystemEntity gs : systems) {
                    keys.add(gs.key);
                    names.add(gs.name);
                }
            }

            runOnUiThread(() -> {
                gameSystemKeys.clear();
                gameSystemKeys.addAll(keys);
                gameSystemNames.clear();
                gameSystemNames.addAll(names);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, gameSystemNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGameSystem.setAdapter(adapter);

                if (editId != -1L) loadExistingCampaign();
            });
        });
    }

    private void loadExistingCampaign() {
        CampaignDatabase.getInstance(this).campaignDao().getById(editId)
                .observe(this, campaign -> {
                    if (campaign == null) return;
                    etName.setText(campaign.name);
                    etDescription.setText(campaign.description != null ? campaign.description : "");

                    int pos = gameSystemKeys.indexOf(campaign.gameSystem);
                    if (pos >= 0) spinnerGameSystem.setSelection(pos);

                    selectedCharacterId = campaign.characterId;
                    if (selectedCharacterId != -1L) {
                        loadCharacterName(selectedCharacterId);
                    } else {
                        tvSelectedCharacter.setText("No character selected");
                    }
                });
    }

    private void loadCharacterName(long characterId) {
        executor.execute(() -> {
            CharacterEntity character = PlayerCharacterDatabase.getInstance(this)
                    .characterDao().getCharacterSync(characterId);
            runOnUiThread(() -> {
                if (character != null) {
                    selectedCharacterName = character.name;
                    tvSelectedCharacter.setText("Selected: " + character.name);
                } else {
                    tvSelectedCharacter.setText("No character selected");
                    selectedCharacterId = -1L;
                }
            });
        });
    }

    private void showCharacterPickerDialog() {
        executor.execute(() -> {
            List<CharacterEntity> allCharacters = PlayerCharacterDatabase.getInstance(this)
                    .characterDao().getAllCharactersSync();
            runOnUiThread(() -> {
                if (allCharacters.isEmpty()) {
                    Toast.makeText(this, "No characters available — create one first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] names = allCharacters.stream()
                        .map(c -> c.name)
                        .toArray(String[]::new);

                new AlertDialog.Builder(this)
                        .setTitle("Select your character")
                        .setItems(names, (dialog, which) -> {
                            selectedCharacterId = allCharacters.get(which).id;
                            selectedCharacterName = allCharacters.get(which).name;
                            tvSelectedCharacter.setText("Selected: " + selectedCharacterName);
                        })
                        .setNeutralButton("Clear selection", (d, w) -> {
                            selectedCharacterId = -1L;
                            selectedCharacterName = "";
                            tvSelectedCharacter.setText("No character selected");
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
    }

    private void saveCampaign() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Name required");
            return;
        }

        String desc = etDescription.getText().toString().trim();
        int pos = spinnerGameSystem.getSelectedItemPosition();
        String gameSystem = (pos >= 0 && pos < gameSystemKeys.size())
                ? gameSystemKeys.get(pos) : "5e-2014";

        CampaignEntity campaign = new CampaignEntity();
        campaign.name = name;
        campaign.description = desc;
        campaign.gameSystem = gameSystem;
        campaign.characterId = selectedCharacterId;
        if (editId != -1L) campaign.id = editId;

        CampaignDatabase db = CampaignDatabase.getInstance(this);
        executor.execute(() -> {
            if (editId != -1L) {
                db.campaignDao().update(campaign);
            } else {
                db.campaignDao().insert(campaign);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Campaign saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
