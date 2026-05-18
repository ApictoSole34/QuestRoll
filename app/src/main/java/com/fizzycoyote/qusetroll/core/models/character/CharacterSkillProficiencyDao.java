package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CharacterSkillProficiencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CharacterSkillProficiencyEntity> proficiencies);

    @Query("DELETE FROM character_skill_proficiencies WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);

    @Query("SELECT skillKey FROM character_skill_proficiencies WHERE character_id = :characterId")
    List<String> getSkillKeysForCharacter(long characterId);
}