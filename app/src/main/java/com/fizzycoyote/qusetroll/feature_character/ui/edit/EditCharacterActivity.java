package com.fizzycoyote.qusetroll.feature_character.ui.edit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.base.BaseCharacterActivity;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.CharacterImagePickerDialog;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.adapter.CharacterImagePickerDialogAdapter;

import java.io.IOException;
import java.io.InputStream;

public class EditCharacterActivity extends BaseCharacterActivity {
    private int characterId;
    private String existingImagePath;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_edit_character;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        existingImagePath = getIntent().getStringExtra("characterMainImagePath");
        characterMainImagePath = existingImagePath;

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> saveCharacterData());

        characterId = getIntent().getIntExtra("characterId", -1);

        if (characterId != -1) {
            loadCharacterData(characterId);
        } else {
            Toast.makeText(this, "Character not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        imageViewCharacter.setOnClickListener(v -> {
            CharacterImagePickerDialog dialog = new CharacterImagePickerDialog(new CharacterImagePickerDialogAdapter.OnImageClickListener() {
                @Override
                public void onImageClick(String imagePath) {
                    if (imagePath.startsWith("assets://")) {
                        try {
                            InputStream is = getAssets().open(imagePath.replace("assets://", ""));
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            imageViewCharacter.setImageBitmap(bitmap);
                            characterMainImagePath = imagePath;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        imageViewCharacter.setImageBitmap(bitmap);
                        characterMainImagePath = imagePath;
                    }
                }

                @Override
                public void onAddImageClick() {
                    openImageChooser();
                }
            });
            dialog.show(getSupportFragmentManager(), "CharacterImagePickerDialog");
        });
    }

    @Override
    protected void saveCharacter(CharacterRPG characterRPG) {
        if (characterMainImagePath == null || characterMainImagePath.isEmpty()) {
            if (existingImagePath != null && !existingImagePath.isEmpty()) {
                characterMainImagePath = existingImagePath;
            } else {
                characterMainImagePath = "assets://characters_images/hood-8779438_1920.png"; // Domyślny obraz
            }
        }

        characterRPG.setId(characterId);
        characterRPG.setCharacterMainImagePath(characterMainImagePath); // Upewnij się, że ścieżka obrazu jest ustawiona
        dbHelper.updateCharacter(characterRPG);
        Toast.makeText(this, "Character updated", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}