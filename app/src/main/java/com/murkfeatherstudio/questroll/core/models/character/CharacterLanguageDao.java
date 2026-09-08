package com.murkfeatherstudio.questroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CharacterLanguageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CharacterLanguageEntity language);

    @Insert
    void insertAll(List<CharacterLanguageEntity> languages);

    @Query("DELETE FROM character_languages WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);

    @Query("SELECT * FROM character_languages WHERE character_id = :characterId")
    List<CharacterLanguageEntity> getByCharacterId(long characterId);
}