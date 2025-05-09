package com.fizzycoyote.qusetroll.feature_character.ui.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.data.CharacterDatabaseHelper;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.helper.CharacterImageHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class BaseCharacterActivity extends AppCompatActivity {
    protected CharacterDatabaseHelper dbHelper;
    protected static final int PICK_IMAGE_REQUEST = 1;
    protected static final int UCROP_MINIATURE_REQUEST = 3;

    protected EditText editTextName, editTextRace, editTextClass, editTextLevel, editTextStrength, editTextDexterity,
            editTextConstitution, editTextIntelligence, editTextWisdom, editTextCharisma;
    protected ImageView imageViewCharacter, imageViewMiniature;
    protected String characterMainImagePath, characterMiniaturePath;

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
        imageViewMiniature = findViewById(R.id.imageViewMiniature);


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

            String mainImagePath = characterRPG.getCharacterMainImagePath();
            this.characterMainImagePath = mainImagePath;
            if (mainImagePath != null && !mainImagePath.isEmpty()) {
                if (mainImagePath.startsWith("assets://")) {
                    try {
                        String assetPath = mainImagePath.replace("assets://", "");
                        InputStream is = getAssets().open(assetPath);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageViewCharacter.setImageBitmap(bitmap);
                        is.close();
                    } catch (IOException e) {
                        imageViewCharacter.setImageResource(R.drawable.default_character_image);
                    }
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(mainImagePath);
                    if (bitmap != null) {
                        imageViewCharacter.setImageBitmap(bitmap);
                    } else {
                        imageViewCharacter.setImageResource(R.drawable.default_character_image);
                    }
                }
            } else {
                imageViewCharacter.setImageResource(R.drawable.default_character_image);
            }

            String miniaturePath = characterRPG.getCharacterMiniaturePath();
            this.characterMiniaturePath = miniaturePath;
            if (miniaturePath != null && !miniaturePath.isEmpty()) {
                if (miniaturePath.startsWith("assets://")) {
                    String assetMiniPath = miniaturePath.replace("assets://", "");
                    try {
                        InputStream isMini = getAssets().open(assetMiniPath);
                        Bitmap bitmapMini = BitmapFactory.decodeStream(isMini);
                        imageViewMiniature.setImageBitmap(bitmapMini);
                    } catch (IOException e) {
                        setDefaultMiniature();
                    }
                } else {
                    File miniatureFile = new File(miniaturePath);

                    if (miniatureFile.exists()) {
                        Bitmap bitmapMini = BitmapFactory.decodeFile(miniaturePath);
                        if (bitmapMini != null) {
                            imageViewMiniature.setImageBitmap(bitmapMini);
                        } else {
                            setDefaultMiniature();
                        }
                    } else {
                        setDefaultMiniature();
                    }
                }
            } else {
                setDefaultMiniature();
            }
        }
    }
    protected void setDefaultMiniature() {
        try {
            InputStream isMini = getAssets().open("characters_miniatures/hood-8779438_1920_mini.png");
            Bitmap bitmapMini = BitmapFactory.decodeStream(isMini);
            imageViewMiniature.setImageBitmap(bitmapMini);
            characterMiniaturePath = "assets://characters_miniatures/hood-8779438_1920_mini.png";
        } catch (IOException e) {
            e.printStackTrace();
            imageViewMiniature.setImageResource(R.drawable.default_character_image);
            Toast.makeText(this, "Failed to load default miniature", Toast.LENGTH_SHORT).show();
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

        String characterMainImagePath = this.characterMainImagePath != null ?
                this.characterMainImagePath : "assets://characters_images/hood-8779438_1920.png";

        String characterMiniaturePath;
        if (characterMainImagePath.startsWith("assets://")) {
            characterMiniaturePath = "assets://characters_miniatures/hood-8779438_1920_mini.png";
        } else {
            characterMiniaturePath = CharacterImageHelper.getMiniaturePathForImage(characterMainImagePath);
            if (characterMiniaturePath == null || characterMiniaturePath.isEmpty()) {
                characterMiniaturePath = "assets://characters_miniatures/hood-8779438_1920_mini.png";
            }
        }

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
                characterMainImagePath,
                characterMiniaturePath
        );

        saveCharacter(characterRPG);
    }

    protected abstract void saveCharacter(CharacterRPG characterRPG);

    protected void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCROP_MINIATURE_REQUEST) {
            if (resultCode == RESULT_OK) {
                handleSuccessfulMiniatureUpdate();
            } else if (resultCode == UCrop.RESULT_ERROR) {
                handleCropError(data);
            }
        }
    }

    private void handleSuccessfulMiniatureUpdate() {
        Bitmap bitmap = BitmapFactory.decodeFile(characterMiniaturePath);
        ImageView miniatureView = findViewById(R.id.imageViewMiniature);
        miniatureView.setImageBitmap(bitmap);
    }

    private void handleCropError(Intent data) {
        Throwable error = UCrop.getError(data);
        Toast.makeText(this, "Failed to crop image: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
