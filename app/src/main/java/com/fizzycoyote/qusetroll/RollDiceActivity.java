package com.fizzycoyote.qusetroll;


import android.widget.ImageView;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import androidx.appcompat.app.AppCompatActivity;



public class RollDiceActivity  extends AppCompatActivity {

    private ImageView diceImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll_dice);

        diceImageView = findViewById(R.id.diceImageView);
    }

    // Roll the dice and display the result
    public void rollDice(View view) {
        int rollResult = (int) (Math.random() * 20) + 1;
        int diceType = 20;
        Toast.makeText(this, "Roll result: " + rollResult, Toast.LENGTH_SHORT).show();
        String gifName = "d" + diceType + "s" + rollResult;
        int gifResId = getResources().getIdentifier(gifName, "drawable", getPackageName());

        if (gifResId != 0) {
            ImageView imageView = findViewById(R.id.diceImageView);

            Glide.with(this)
                    .load(gifResId)
                    .into(diceImageView);
        } else {
            Toast.makeText(this, "GIF not found for roll result: " + rollResult, Toast.LENGTH_SHORT).show();
        }

    }
}
