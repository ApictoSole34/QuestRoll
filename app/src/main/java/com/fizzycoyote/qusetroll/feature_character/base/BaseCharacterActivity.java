package com.fizzycoyote.qusetroll.feature_character.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.data.CharacterDatabaseHelper;
import com.fizzycoyote.qusetroll.feature_character.helper.CharacterImageHelper;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;

import java.io.IOException;
import java.io.InputStream;

public abstract class BaseCharacterActivity extends AppCompatActivity {
    protected CharacterDatabaseHelper dbHelper;
    protected static final int PICK_IMAGE_REQUEST = 1;

    protected EditText editTextName, editTextRace, editTextClass, editTextLevel, editTextStrength, editTextDexterity, editTextConstitution, editTextIntelligence, editTextWisdom, editTextCharisma;
    protected ImageView imageViewCharacter;
    protected String characterMainImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        dbHelper = new CharacterDatabaseHelper(this);

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
        imageViewCharacter = findViewById(R.id.imageViewCharacter);
    }

    protected abstract int getLayoutResourceId();

    protected void loadCharacterData(int characterId) {
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

            String imagePath = characterRPG.getCharacterMainImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("assets://")) {
                    try {
                        InputStream is = getAssets().open(imagePath.replace("assets://", ""));
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageViewCharacter.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        imageViewCharacter.setImageResource(R.drawable.default_character_image);
                        Toast.makeText(this, "Failed to load character image from assets", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        imageViewCharacter.setImageBitmap(bitmap);
                    } else {
                        imageViewCharacter.setImageResource(R.drawable.default_character_image);
                        Toast.makeText(this, "Failed to load image from internal storage", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                imageViewCharacter.setImageResource(R.drawable.default_character_image);
                characterMainImagePath = "assets://characters_images/hood-8779438_1920.png";
                Toast.makeText(this, "No image path provided", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to load character data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    protected void saveCharacterData() {
        String name = editTextName.getText().toString();
        String race = editTextRace.getText().toString();
        String characterClass = editTextClass.getText().toString();
        int level = Integer.parseInt(editTextLevel.getText().toString());
        int strength = Integer.parseInt(editTextStrength.getText().toString());
        int dexterity = Integer.parseInt(editTextDexterity.getText().toString());
        int constitution = Integer.parseInt(editTextConstitution.getText().toString());
        int intelligence = Integer.parseInt(editTextIntelligence.getText().toString());
        int wisdom = Integer.parseInt(editTextWisdom.getText().toString());
        int charisma = Integer.parseInt(editTextCharisma.getText().toString());
        String gameVersion = "D&D 5e";

        String characterMainImagePath = this.characterMainImagePath != null ? this.characterMainImagePath : "assets://characters_images/hood-8779438_1920.png";
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
                gameVersion,
                characterMainImagePath
        );

        Log.d("BaseCharacterActivity", "Before saving, characterMainImagePath = " + characterMainImagePath);

        saveCharacter(characterRPG);
    }

    protected abstract void saveCharacter(CharacterRPG characterRPG);

    protected void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    protected String copyImageToInternalStorage(Uri uri) {
        return CharacterImageHelper.copyImageToInternalStorage(this, uri);
    }
}