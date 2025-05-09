package com.fizzycoyote.qusetroll.feature_character.ui.edit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.ui.base.BaseCharacterActivity;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.CharacterImagePickerDialog;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.adapter.CharacterImagePickerDialogAdapter;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.helper.CharacterImageHelper;

import java.io.File;
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
                    characterMainImagePath = imagePath;
                    if (imagePath.startsWith("assets://")) {
                        try {
                            InputStream is = getAssets().open(imagePath.replace("assets://", ""));
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            imageViewCharacter.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            imageViewCharacter.setImageResource(R.drawable.default_character_image);
                        }
                    } else {
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        imageViewCharacter.setImageBitmap(bitmap != null ? bitmap : BitmapFactory.decodeResource(getResources(), R.drawable.default_character_image));
                    }

                    characterMiniaturePath = CharacterImageHelper.getMiniaturePathForImage(imagePath);
                    if (characterMiniaturePath != null) {
                        if (characterMiniaturePath.startsWith("assets://")) {
                            try {
                                String assetPAth = characterMiniaturePath.replace("assets://", "");
                                InputStream is = getAssets().open(assetPAth);
                                imageViewMiniature.setImageBitmap(BitmapFactory.decodeStream(is));
                            } catch (IOException e) {
                                setDefaultMiniature();
                            }
                        } else {
                            Log.d("MINI_DEBUG", "Trying to load LOCAL miniature");
                            File miniatureFile = new File(characterMiniaturePath);
                            if (miniatureFile.exists()) {
                                Bitmap bitmapMini = BitmapFactory.decodeFile(characterMiniaturePath);
                                imageViewMiniature.setImageBitmap(bitmapMini);
                            } else {
                                Log.e("MINI_DEBUG", "Local miniature file not found");
                                setDefaultMiniature();
                            }
                        }
                    } else {
                        Log.e("MINI_DEBUG", "Miniature path is empty");
                        setDefaultMiniature();
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
                characterMainImagePath = "assets://characters_images/hood-8779438_1920.png";
            }
        }

        characterRPG.setId(characterId);
        characterRPG.setCharacterMainImagePath(characterMainImagePath);
        characterRPG.setCharacterMiniaturePath(characterMiniaturePath);
        dbHelper.updateCharacter(characterRPG);
        Toast.makeText(this, "Character updated", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}