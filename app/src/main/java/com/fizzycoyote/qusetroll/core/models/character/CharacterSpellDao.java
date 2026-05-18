package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CharacterSpellDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CharacterSpellEntity spell);

    @Insert
    void insertAll(List<CharacterSpellEntity> spells);

    @Query("SELECT * FROM character_spells WHERE character_id = :characterId")
    List<CharacterSpellEntity> getByCharacterId(long characterId);

    @Query("DELETE FROM character_spells WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);
}