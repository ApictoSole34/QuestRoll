package com.fizzycoyote.qusetroll.feature_character.ui.list;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.fizzycoyote.qusetroll.R;

public class CharacterListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_character);
        NavController navController = navHost.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = ((NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_character)).getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}