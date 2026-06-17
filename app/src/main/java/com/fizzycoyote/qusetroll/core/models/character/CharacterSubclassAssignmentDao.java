package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CharacterSubclassAssignmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CharacterSubclassAssignmentEntity assignment);

    @Update
    void update(CharacterSubclassAssignmentEntity assignment);

    @Delete
    void delete(CharacterSubclassAssignmentEntity assignment);

    @Query("DELETE FROM character_subclasses WHERE characterId = :characterId AND classKey = :classKey")
    void deleteForCharacterAndClass(long characterId, String classKey);

    @Query("SELECT * FROM character_subclasses WHERE characterId = :characterId")
    List<CharacterSubclassAssignmentEntity> getForCharacter(long characterId);

    @Query("SELECT * FROM character_subclasses WHERE characterId = :characterId AND classKey = :classKey")
    CharacterSubclassAssignmentEntity getForCharacterAndClass(long characterId, String classKey);
}