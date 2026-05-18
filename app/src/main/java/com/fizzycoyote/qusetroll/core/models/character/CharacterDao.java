package com.fizzycoyote.qusetroll.core.models.character;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CharacterEntity character);

    @Update
    void update(CharacterEntity character);

    @Query("SELECT * FROM characters WHERE id = :id")
    LiveData<CharacterEntity> getCharacter(long id);

    @Query("SELECT * FROM characters WHERE id = :id")
    CharacterEntity getCharacterSync(long id);  // DODAJ TĘ METODĘ

    @Query("SELECT * FROM characters ORDER BY name ASC")
    LiveData<List<CharacterEntity>> getAllCharacters();

    @Transaction
    @Query("SELECT * FROM characters WHERE id = :id")
    LiveData<CharacterWithRelations> getCharacterWithRelations(long id);

    @Query("DELETE FROM characters WHERE id = :id")
    void deleteCharacter(long id);
}