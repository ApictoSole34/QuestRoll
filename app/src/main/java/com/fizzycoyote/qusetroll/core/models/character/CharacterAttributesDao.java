package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CharacterAttributesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CharacterAttributesEntity attributes);

    @Update
    void update(CharacterAttributesEntity attributes);

    @Query("SELECT * FROM character_attributes WHERE character_id = :characterId")
    CharacterAttributesEntity getByCharacterId(long characterId);
}