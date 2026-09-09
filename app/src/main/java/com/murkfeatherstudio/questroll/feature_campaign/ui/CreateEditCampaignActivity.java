package com.murkfeatherstudio.questroll.feature_campaign.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.CampaignDatabase;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.game_system.GameSystemEntity;
import com.murkfeatherstudio.questroll.databinding.ActivityCreateEditCampaignBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for creating or editing a campaign.
 */
public class CreateEditCampaignActivity extends BaseActivity {

    public static final String EXTRA_CAMPAIGN_ID = "campaign_id";

    private ActivityCreateEditCampaignBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<String> gameSystemKeys = new ArrayList<>();
    private final List<String> gameSystemNames = new ArrayList<>();

    private long editId = -1L;
    private long selectedCharacterId = -1L;
    private String selectedCharacterName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateEditCampaignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getLongExtra(EXTRA_CAMPAIGN_ID, -1L);
        setTitle(editId != -1L ? "Edit Campaign" : "New Campaign");

        loadGameSystems();

        binding.btnSelectCharacter.setOnClickListener(v -> showCharacterPickerDialog());
        binding.btnSave.setOnClickListener(v -> saveCampaign());
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
                binding.spinnerGameSystem.setAdapter(adapter);

                if (editId != -1L) loadExistingCampaign();
            });
        });
    }

    private void loadExistingCampaign() {
        CampaignDatabase.getInstance(this).campaignDao().getById(editId)
                .observe(this, campaign -> {
                    if (campaign == null) return;
                    binding.etName.setText(campaign.name);
                    binding.etDescription.setText(campaign.description != null ? campaign.description : "");

                    int pos = gameSystemKeys.indexOf(campaign.gameSystem);
                    if (pos >= 0) binding.spinnerGameSystem.setSelection(pos);

                    selectedCharacterId = campaign.characterId;
                    if (selectedCharacterId != -1L) {
                        loadCharacterName(selectedCharacterId);
                    } else {
                        binding.tvSelectedCharacter.setText("No character selected");
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
                    binding.tvSelectedCharacter.setText("Selected: " + character.name);
                } else {
                    binding.tvSelectedCharacter.setText("No character selected");
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
                            binding.tvSelectedCharacter.setText("Selected: " + selectedCharacterName);
                        })
                        .setNeutralButton("Clear selection", (d, w) -> {
                            selectedCharacterId = -1L;
                            selectedCharacterName = "";
                            binding.tvSelectedCharacter.setText("No character selected");
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
    }

    private void saveCampaign() {
        String name = binding.etName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.etName.setError("Name required");
            return;
        }

        String desc = binding.etDescription.getText().toString().trim();
        int pos = binding.spinnerGameSystem.getSelectedItemPosition();
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
