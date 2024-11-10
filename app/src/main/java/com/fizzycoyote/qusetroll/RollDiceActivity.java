package com.fizzycoyote.qusetroll;


import android.graphics.drawable.Drawable;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;



public class RollDiceActivity  extends AppCompatActivity {

    private ImageView diceImageView;
    private Dice currentDice;
    private Spinner diceTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll_dice);

        diceImageView = findViewById(R.id.diceImageView);
        diceTypeSpinner = findViewById(R.id.diceTypeSpinner);

        currentDice = new Dice(20, this);
        setDiceGif("d20s1");

        diceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            // Handle item selection
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedDiceType = adapterView.getItemAtPosition(i).toString();

                if (selectedDiceType.startsWith("d")) {
                    selectedDiceType = selectedDiceType.substring(1);
                }

                int diceType = Integer.parseInt(selectedDiceType);
                currentDice = new Dice(diceType, RollDiceActivity.this);

                setDiceGif("d" + diceType + "s" + diceType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    // Roll the dice and display the result
    // gif 24.98 1000


    // Roll the dice and display the result
    public void rollDice(View view) {
        int rollResult = currentDice.roll();
        int gifResId = currentDice.getGifResourceId(rollResult);

        if (gifResId != 0) {
            Glide.with(this)
                    .asGif()
                    .load(gifResId)
                    .into(new CustomTarget<GifDrawable>() {
                        @Override
                        public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                            resource.setLoopCount(1);
                            diceImageView.setImageDrawable(resource);
                            resource.start();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        } else {
            Toast.makeText(this, "No GIF found for roll result: " + rollResult, Toast.LENGTH_SHORT).show();
        }
    }

    // Set the dice GIF based on the roll result
    private void setDiceGif(String gifName) {
        int gifResId = getResources().getIdentifier(gifName, "drawable", getPackageName());

        if (gifResId != 0) {
            Glide.with(this)
                    .asGif()
                    .load(gifResId)
                    .into(new CustomTarget<GifDrawable>() {
                        @Override
                        public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                            resource.setLoopCount(1);
                            diceImageView.setImageDrawable(resource);
                            resource.stop();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        } else {
            Toast.makeText(this, "GIF not found for name: " + gifName, Toast.LENGTH_SHORT).show();
        }
    }


}

