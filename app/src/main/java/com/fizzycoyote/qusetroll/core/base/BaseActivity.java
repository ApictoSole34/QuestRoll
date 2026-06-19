package com.fizzycoyote.qusetroll.core.base;

import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;

import java.util.Random;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int[] BACKGROUNDS = {
            R.drawable.threads_bg_1,
            R.drawable.threads_bg_2,
            R.drawable.threads_bg_3,
            R.drawable.threads_bg_4,
            R.drawable.threads_bg_5,
            R.drawable.threads_bg_6,
            R.drawable.threads_bg_7,
            R.drawable.threads_bg_8
    };

    @Override
    public void setContentView(int layoutResID) {

        super.setContentView(layoutResID);

        setupBackground();
    }

    private void setupBackground() {

        ImageView backgroundImage =
                findViewById(R.id.backgroundImage);

        if (backgroundImage == null) {
            return;
        }

        Random random = new Random();

        int randomIndex =
                random.nextInt(BACKGROUNDS.length);

        backgroundImage.setImageResource(
                BACKGROUNDS[randomIndex]
        );

        backgroundImage.setScaleX(1.35f);
        backgroundImage.setScaleY(1.35f);

        float offsetX =
                (random.nextFloat() - 0.5f) * 300f;

        float offsetY =
                (random.nextFloat() - 0.5f) * 600f;

        backgroundImage.setTranslationX(offsetX);
        backgroundImage.setTranslationY(offsetY);
    }
}