package com.fizzycoyote.qusetroll.core.models.character;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CharacterClassAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CharacterClassAssignmentEntity assignment);

    @Insert
    void insertAll(List<CharacterClassAssignmentEntity> assignments);

    @Update
    void update(CharacterClassAssignmentEntity assignment);

    @Query("SELECT * FROM character_classes WHERE character_id = :characterId")
    List<CharacterClassAssignmentEntity> getByCharacterId(long characterId);

    @Query("DELETE FROM character_classes WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);
}