package com.fizzycoyote.qusetroll.feature_item.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_item.ui.weapon.WeaponListActivity;

public class ItemMenuActivity  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_item_menu);
    }


    public void openWeaponListActivity(View view) {
        startActivity(new Intent(this, WeaponListActivity.class));
    }

}
