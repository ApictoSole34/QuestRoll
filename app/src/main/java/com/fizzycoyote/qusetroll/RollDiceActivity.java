package com.fizzycoyote.qusetroll;


import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll_dice);

        diceImageView = findViewById(R.id.diceImageView);
        // Load the initial GIF
        Glide.with(this)
                .asGif()
                .load(R.drawable.d20s20)
                .into(new CustomTarget<GifDrawable>() {
                    @Override
                    public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                        resource.setLoopCount(0);
                        diceImageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // This method is called when the Glide request is cleared
                    }
                });

    }


    // Roll the dice and display the result
    // gif 24.98 1000
    public void rollDice(View view) {
        int rollResult = (int) (Math.random() * 20) + 1;
        int diceType = 20;
        Toast.makeText(this, "Roll result: " + rollResult, Toast.LENGTH_SHORT).show();

        String gifName = "d" + diceType + "s" + rollResult;
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
                            resource.start();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // This method is called when the Glide request is cleared
                        }
                    });
        } else {
            Toast.makeText(this, "GIF not found for roll result: " + rollResult, Toast.LENGTH_SHORT).show();
        }

    }
}
