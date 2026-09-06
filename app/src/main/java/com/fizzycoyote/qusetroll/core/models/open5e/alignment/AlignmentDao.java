package com.fizzycoyote.qusetroll.core.models.open5e.alignment;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e alignments (e.g., Lawful Good, Chaotic Evil)
 * stored in the compendium.
 */
@Dao
public interface AlignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AlignmentEntity> alignments);

    @Query("SELECT COUNT(*) FROM alignments")
    int getCount();

    @Query("SELECT * FROM alignments WHERE key = :key")
    AlignmentEntity getByKeySync(String key);

    @Query("DELETE FROM alignments")
    void deleteAll();

    /**
     * Synchronously retrieves all alignment entities.
     */
    @Query("SELECT * FROM alignments")
    List<AlignmentEntity> getAllSync();

    /**
     * Retrieves all alignments associated with a specific game system.
     *
     * @param gameSystem The game system identifier.
     * @return A list of alignment entities.
     */
    @Query("SELECT a.* FROM alignments a " +
            "INNER JOIN documents d ON a.document_key = d.key " +
            "WHERE d.gamesystem = :gameSystem " +
            "ORDER BY a.short_name ASC")
    List<AlignmentEntity> getByGameSystem(String gameSystem);

    /**
     * Retrieves all alignments as LiveData, ordered by key.
     */
    @Query("SELECT * FROM alignments ORDER BY key ASC")
    LiveData<List<AlignmentEntity>> getAll();

    /**
     * Retrieves a specific alignment by its key.
     */
    @Query("SELECT * FROM alignments WHERE key = :key")
    LiveData<AlignmentEntity> getByKey(String key);
}
