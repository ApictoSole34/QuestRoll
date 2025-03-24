package com.fizzycoyote.qusetroll.feature_character.ui.create;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.base.BaseCharacterActivity;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.CharacterImagePickerDialog;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.adapter.CharacterImagePickerDialogAdapter;
import com.fizzycoyote.qusetroll.feature_character.ui.list.CharacterListActivity;

import java.io.IOException;
import java.io.InputStream;

public class CreateCharacterActivity extends BaseCharacterActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_create_character;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> saveCharacterData());

        imageViewCharacter.setOnClickListener(v -> {
            CharacterImagePickerDialog dialog = new CharacterImagePickerDialog(new CharacterImagePickerDialogAdapter.OnImageClickListener() {
                @Override
                public void onImageClick(String imagePath) {
                    if (imagePath.startsWith("assets://")) {
                        try {
                            String assetPath = imagePath.replace("assets://", "");
                            InputStream is = getAssets().open(assetPath);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            imageViewCharacter.setImageBitmap(bitmap);
                            characterMainImagePath = imagePath;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        if (bitmap != null) {
                            imageViewCharacter.setImageBitmap(bitmap);
                            characterMainImagePath = imagePath;
                        } else {
                            Toast.makeText(CreateCharacterActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onAddImageClick() {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            });
            dialog.show(getSupportFragmentManager(), "CharacterImagePickerDialog");
        });
    }

    @Override
    protected void saveCharacter(CharacterRPG characterRPG) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            characterMainImagePath = getRealPathFromUri(uri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageViewCharacter.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
}
