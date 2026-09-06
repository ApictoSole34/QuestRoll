package com.fizzycoyote.qusetroll.core.models.character;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for {@link CharacterClassAssignmentEntity}.
 * <p>
 * Manages the association between characters and their classes, supporting
 * multiclassing by allowing multiple class entries per character.
 * </p>
 */
@Dao
public interface CharacterClassAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CharacterClassAssignmentEntity assignment);

    @Insert
    void insertAll(List<CharacterClassAssignmentEntity> assignments);

    @Update
    void update(CharacterClassAssignmentEntity assignment);

    /**
     * Retrieves all class assignments for a specific character.
     *
     * @param characterId The ID of the character.
     * @return A list of class assignments (class key and level).
     */
    @Query("SELECT * FROM character_classes WHERE character_id = :characterId")
    List<CharacterClassAssignmentEntity> getByCharacterId(long characterId);

    /**
     * Removes all class associations for a specific character.
     *
     * @param characterId The ID of the character.
     */
    @Query("DELETE FROM character_classes WHERE character_id = :characterId")
    void deleteForCharacter(long characterId);
}
