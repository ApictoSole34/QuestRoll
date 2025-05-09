package com.fizzycoyote.qusetroll.feature_character.ui.list;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.data.CharacterDatabaseHelper;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;
import com.fizzycoyote.qusetroll.feature_character.ui.create.CreateCharacterActivity;
import com.fizzycoyote.qusetroll.feature_character.ui.details.CharacterDetailsActivity;
import com.fizzycoyote.qusetroll.feature_character.ui.list.adapter.CharacterAdapter;

import java.util.List;

public class CharacterListActivity extends AppCompatActivity implements CharacterAdapter.OnCharacterClickListener {
    private RecyclerView recyclerView;
    private CharacterAdapter adapter;
    private CharacterDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_list);

        recyclerView = findViewById(R.id.recyclerViewCharacters);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new CharacterDatabaseHelper(this);
        List<CharacterRPG> characterRPGList = dbHelper.getAllCharacters();

        adapter = new CharacterAdapter(characterRPGList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAddCharacterClick() {
        Intent intent = new Intent(this, CreateCharacterActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCharacterClick(CharacterRPG characterRPG) {
        Intent intent = new Intent(this, CharacterDetailsActivity.class);
        intent.putExtra("characterId", characterRPG.getId());
        startActivityForResult(intent, 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        List<CharacterRPG> updatedList = dbHelper.getAllCharacters();
        adapter.setCharacterList(updatedList);
        adapter.notifyDataSetChanged();
    }
}
