package com.fizzycoyote.qusetroll.main.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.ui.create.CreateCharacterActivity;
import com.fizzycoyote.qusetroll.feature_character.ui.list.CharacterListActivity;
import com.fizzycoyote.qusetroll.feature_dice.ui.RollDiceActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
