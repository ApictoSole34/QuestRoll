package com.fizzycoyote.qusetroll.feature_character.ui.details;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.data.CharacterDatabaseHelper;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;
import com.fizzycoyote.qusetroll.feature_character.ui.edit.EditCharacterActivity;

public class CharacterDetailsActivity extends AppCompatActivity {
    private TextView textViewName, textViewRace, textViewClass, textViewLevel, textViewStrength, textViewDexterity, textViewConstitution
            , textViewIntelligence, textViewWisdom, textViewCharisma, textViewGameVersion;

    private Button buttonManage;
    private CharacterDatabaseHelper dbHelper;
    private int characterId;

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
        CharacterRPG characterRPG = dbHelper.getCharacterById(characterId);
        if (characterRPG != null) {
            textViewName.setText("Name: " + characterRPG.getName());
            textViewRace.setText("Race " +characterRPG.getRace());
            textViewClass.setText("Class: " + characterRPG.getCharacterClass());
            textViewLevel.setText("Level: " + String.valueOf(characterRPG.getLevel()));
            textViewStrength.setText("Strength: " + String.valueOf(characterRPG.getStrength()));
            textViewDexterity.setText("Dexterity: " + String.valueOf(characterRPG.getDexterity()));
            textViewConstitution.setText("Constitution: " + String.valueOf(characterRPG.getConstitution()));
            textViewIntelligence.setText("Intelligence: " + String.valueOf(characterRPG.getIntelligence()));
            textViewWisdom.setText("Wisdom: " + String.valueOf(characterRPG.getWisdom()));
            textViewCharisma.setText("Charisma: " + String.valueOf(characterRPG.getCharisma()));
            textViewGameVersion.setText(characterRPG.getGameVersion());
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
            startActivity(intent);
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
}

