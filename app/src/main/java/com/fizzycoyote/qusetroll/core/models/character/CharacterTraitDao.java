package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CharacterTraitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CharacterTraitEntity trait);

    @Insert
    void insertAll(List<CharacterTraitEntity> traits);

    @Update
    void update(CharacterTraitEntity trait);

    @Query("SELECT * FROM character_traits WHERE character_id = :characterId ORDER BY display_order ASC")
    List<CharacterTraitEntity> getByCharacterId(long characterId);

    @Query("DELETE FROM character_traits WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);
}