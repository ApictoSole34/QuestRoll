package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CharacterSavingThrowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CharacterSavingThrowEntity savingThrow);

    @Insert
    void insertAll(List<CharacterSavingThrowEntity> savingThrows);

    @Query("DELETE FROM character_saving_throws WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);

    @Query("SELECT * FROM character_saving_throws WHERE character_id = :characterId")
    List<CharacterSavingThrowEntity> getByCharacterId(long characterId);
}
