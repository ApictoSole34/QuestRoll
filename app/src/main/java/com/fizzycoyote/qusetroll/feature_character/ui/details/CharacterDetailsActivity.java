package com.fizzycoyote.qusetroll.feature_character.ui.details;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.data.CharacterDatabaseHelper;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;
import com.fizzycoyote.qusetroll.feature_character.ui.edit.EditCharacterActivity;

import java.io.IOException;
import java.io.InputStream;

public class CharacterDetailsActivity extends AppCompatActivity {
    private TextView textViewName, textViewRace, textViewClass, textViewLevel, textViewStrength, textViewDexterity, textViewConstitution
            , textViewIntelligence, textViewWisdom, textViewCharisma, textViewGameVersion;
    private Button buttonManage;
    private CharacterDatabaseHelper dbHelper;
    private int characterId;
    private ImageView imageViewCharacter;
    private CharacterRPG characterRPG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_details);

        textViewName = findViewById(R.id.textViewName);
        textViewRace = findViewById(R.id.textViewRace);
        textViewClass = findViewById(R.id.textViewClass);
        textViewLevel = findViewById(R.id.textViewLevel);
        textViewStrength = findViewById(R.id.textViewStrength);
        textViewDexterity = findViewById(R.id.textViewDexterity);
        textViewConstitution = findViewById(R.id.textViewConstitution);
        textViewIntelligence = findViewById(R.id.textViewIntelligence);
        textViewWisdom = findViewById(R.id.textViewWisdom);
        textViewCharisma = findViewById(R.id.textViewCharisma);
        textViewGameVersion = findViewById(R.id.textViewGameVersion);
        buttonManage = findViewById(R.id.buttonManage);
        imageViewCharacter = findViewById(R.id.imageViewCharacter);

        dbHelper = new CharacterDatabaseHelper(this);

        characterId = getIntent().getIntExtra("characterId", -1);

        if (characterId != -1) {
            loadCharacterData(characterId);
        } else {
            Toast.makeText(this, "Character not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonManage.setOnClickListener(v -> showManagePopup());
    }

    private void loadCharacterData(int characterId) {
         this.characterRPG = dbHelper.getCharacterById(characterId);
        if (this.characterRPG != null) {
            textViewName.setText("Name: " + this.characterRPG.getName());
            textViewRace.setText("Race " +this.characterRPG.getRace());
            textViewClass.setText("Class: " + this.characterRPG.getCharacterClass());
            textViewLevel.setText("Level: " + String.valueOf(this.characterRPG.getLevel()));
            textViewStrength.setText("Strength: " + String.valueOf(this.characterRPG.getStrength()));
            textViewDexterity.setText("Dexterity: " + String.valueOf(this.characterRPG.getDexterity()));
            textViewConstitution.setText("Constitution: " + String.valueOf(this.characterRPG.getConstitution()));
            textViewIntelligence.setText("Intelligence: " + String.valueOf(this.characterRPG.getIntelligence()));
            textViewWisdom.setText("Wisdom: " + String.valueOf(this.characterRPG.getWisdom()));
            textViewCharisma.setText("Charisma: " + String.valueOf(this.characterRPG.getCharisma()));
            textViewGameVersion.setText(this.characterRPG.getGameVersion());

            String imagePath = this.characterRPG.getCharacterMainImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("assets://")) {
                    try {
                        InputStream is = getAssets().open(imagePath.replace("assets://", ""));
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageViewCharacter.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        imageViewCharacter.setImageResource(R.drawable.default_character_image);
                        Toast.makeText(this, "Character image not found in assets", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        imageViewCharacter.setImageBitmap(bitmap);
                    } else {
                        imageViewCharacter.setImageResource(R.drawable.default_character_image);
                        Toast.makeText(this, "Character image not found in internal storage", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                imageViewCharacter.setImageResource(R.drawable.default_character_image);
                Toast.makeText(this, "No image path provided", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to load character data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showManagePopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_manage_character_details, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        Button buttonEdit = popupView.findViewById(R.id.buttonEdit);
        Button buttonDelete = popupView.findViewById(R.id.buttonDelete);
        Button buttonCancel = popupView.findViewById(R.id.buttonCancel);

        buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(CharacterDetailsActivity.this, EditCharacterActivity.class);
            intent.putExtra("characterId", characterId);

            String imagePath = (this.characterRPG != null) ? this.characterRPG.getCharacterMainImagePath() : null;
            intent.putExtra("characterMainImagePath", imagePath);

            startActivityForResult(intent, 1);
            popupWindow.dismiss();
        });

        buttonDelete.setOnClickListener(v -> {
            popupWindow.dismiss();
            showConfirmDeletePopup();
        });

        buttonCancel.setOnClickListener(v -> popupWindow.dismiss());

        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
    }

    private void showConfirmDeletePopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_confirm_delete,null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        PopupWindow popupWindow = new PopupWindow(popupView,width,height,focusable);

        Button buttonConfirm = popupView.findViewById(R.id.buttonConfirm);
        Button buttonCancel = popupView.findViewById(R.id.buttonCancel);
        popupWindow.showAtLocation(popupView, Gravity.CENTER,0,0);

        buttonConfirm.setOnClickListener(v -> {
            dbHelper.deleteCharacter(characterId);
            Toast.makeText(this, "Character deleted", Toast.LENGTH_SHORT).show();
            finish();
            popupWindow.dismiss();
        });

        buttonCancel.setOnClickListener(view -> popupWindow.dismiss());

        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadCharacterData(characterId);
        }
    }
}
