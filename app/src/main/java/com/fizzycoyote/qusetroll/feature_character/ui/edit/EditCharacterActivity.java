package com.fizzycoyote.qusetroll.feature_character.ui.edit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.data.CharacterDatabaseHelper;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;

public class EditCharacterActivity extends AppCompatActivity {
    private EditText editTextName, editTextRace, editTextClass, editTextLevel,editTextStrength,editTextDexterity,editTextConstitution,editTextIntelligence,editTextWisdom,editTextCharisma;
    private Button buttonSave;
    private CharacterDatabaseHelper dbHelper;
    private int characterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_character);

        editTextName = findViewById(R.id.editTextName);
        editTextRace = findViewById(R.id.editTextRace);
        editTextClass = findViewById(R.id.editTextClass);
        editTextLevel = findViewById(R.id.editTextLevel);
        editTextStrength = findViewById(R.id.editTextStrength);
        editTextDexterity = findViewById(R.id.editTextDexterity);
        editTextConstitution = findViewById(R.id.editTextConstitution);
        editTextIntelligence = findViewById(R.id.editTextIntelligence);
        editTextWisdom = findViewById(R.id.editTextWisdom);
        editTextCharisma = findViewById(R.id.editTextCharisma);

        buttonSave = findViewById(R.id.buttonSave);

        dbHelper = new CharacterDatabaseHelper(this);

        characterId = getIntent().getIntExtra("characterId", -1);

        if (characterId != -1) {
            loadCharacterData(characterId);
        } else {
            Toast.makeText(this, "Character not found", Toast.LENGTH_SHORT).show();
            finish();

        }
        buttonSave.setOnClickListener(v -> saveCharacterData());
    }

    private void loadCharacterData(int characterId) {
        CharacterRPG characterRPG = dbHelper.getCharacterById(characterId);
        if (characterRPG != null) {
            editTextName.setText(characterRPG.getName());
            editTextRace.setText(characterRPG.getRace());
            editTextClass.setText(characterRPG.getCharacterClass());
            editTextLevel.setText(String.valueOf(characterRPG.getLevel()));
            editTextStrength.setText(String.valueOf(characterRPG.getStrength()));
            editTextDexterity.setText(String.valueOf(characterRPG.getDexterity()));
            editTextConstitution.setText(String.valueOf(characterRPG.getConstitution()));
            editTextIntelligence.setText(String.valueOf(characterRPG.getIntelligence()));
            editTextWisdom.setText(String.valueOf(characterRPG.getWisdom()));
            editTextCharisma.setText(String.valueOf(characterRPG.getCharisma()));

        } else {
            Toast.makeText(this, "Failed to load character data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveCharacterData() {
        String name = editTextName.getText().toString();
        String race = editTextRace.getText().toString();
        String characterClass = editTextClass.getText().toString();
        int strength = Integer.parseInt(editTextStrength.getText().toString());
        int dexterity = Integer.parseInt(editTextDexterity.getText().toString());
        int constitution = Integer.parseInt(editTextConstitution.getText().toString());
        int intelligence = Integer.parseInt(editTextIntelligence.getText().toString());
        int wisdom = Integer.parseInt(editTextWisdom.getText().toString());
        int charisma = Integer.parseInt(editTextCharisma.getText().toString());
        int level = Integer.parseInt(editTextLevel.getText().toString());

        CharacterRPG characterRPG = new CharacterRPG(name, race, characterClass, level, strength, dexterity, constitution, intelligence, wisdom, charisma, "D&D 5e");
        characterRPG.setId(characterId);

        dbHelper.updateCharacter(characterRPG);
        Toast.makeText(this, "Character updated", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }


}
