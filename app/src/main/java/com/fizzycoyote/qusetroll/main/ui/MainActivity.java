package com.fizzycoyote.qusetroll.main.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.ui.list.CharacterListActivity;
import com.fizzycoyote.qusetroll.feature_class.ui.ClassListActivity;
import com.fizzycoyote.qusetroll.feature_dice.ui.RollDiceActivity;
import com.fizzycoyote.qusetroll.feature_language.ui.language_list.LanguageListActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
    }

    public void openRollDiceActivity(View view) {
        Intent intent = new Intent(this, RollDiceActivity.class);
        startActivity(intent);
    }

    public void openCharacterListActivity(View view) {
        Intent intent = new Intent(this, CharacterListActivity.class);
        startActivity(intent);
    }

    public void openLanguageListActivity(View view) {
        Intent intent = new Intent(this, LanguageListActivity.class);
        startActivity(intent);
    }

    public void openClassListActivity(View view) {
        Intent intent = new Intent(this, ClassListActivity.class);
        startActivity(intent);
    }
}
