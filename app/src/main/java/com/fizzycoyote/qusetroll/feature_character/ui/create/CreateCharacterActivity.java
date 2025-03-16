package com.fizzycoyote.qusetroll.feature_character.ui.create;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.data.CharacterDatabaseHelper;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;
import com.fizzycoyote.qusetroll.feature_character.ui.list.CharacterListActivity;

public class CreateCharacterActivity extends AppCompatActivity {
    private EditText editTextName, editTextRace, editTextCharacterClass, editTextLevel, editTextStrength, editTextDexterity, editTextConstitution, editTextIntelligence, editTextWisdom, editTextCharisma;
    private CharacterDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_character);

        dbHelper = new CharacterDatabaseHelper(this);

        editTextName = findViewById(R.id.editTextName);
        editTextRace = findViewById(R.id.editTextRace);
        editTextCharacterClass = findViewById(R.id.editTextCharacterClass);
        editTextLevel = findViewById(R.id.editTextLevel);
        editTextStrength = findViewById(R.id.editTextStrength);
        editTextDexterity = findViewById(R.id.editTextDexterity);
        editTextConstitution = findViewById(R.id.editTextConstitution);
        editTextIntelligence = findViewById(R.id.editTextIntelligence);
        editTextWisdom = findViewById(R.id.editTextWisdom);
        editTextCharisma = findViewById(R.id.editTextCharisma);
        Button buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(v -> saveCharacter());
    }

    private void saveCharacter() {
        String name = editTextName.getText().toString();
        String race = editTextRace.getText().toString();
        String characterClass = editTextCharacterClass.getText().toString();
        int level = Integer.parseInt(editTextLevel.getText().toString());
        int strength = Integer.parseInt(editTextStrength.getText().toString());
        int dexterity = Integer.parseInt(editTextDexterity.getText().toString());
        int constitution = Integer.parseInt(editTextConstitution.getText().toString());
        int intelligence = Integer.parseInt(editTextIntelligence.getText().toString());
        int wisdom = Integer.parseInt(editTextWisdom.getText().toString());
        int charisma = Integer.parseInt(editTextCharisma.getText().toString());
        String gameVersion = "D&D 5e"; //todo after add other games and d&d versions i need to change that

        CharacterRPG characterRPG = new CharacterRPG(
                name,
                race,
                characterClass,
                level,
                strength,
                dexterity,
                constitution,
                intelligence,
                wisdom,
                charisma,
                gameVersion
        );

        long id = dbHelper.addCharacter(characterRPG);
        if (id != -1) {
            Toast.makeText(this, "Character saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, CharacterListActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error saving character", Toast.LENGTH_SHORT).show();
        }
    }
}
