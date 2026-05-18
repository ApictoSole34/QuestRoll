package com.fizzycoyote.qusetroll.core.models.open5e.alignment;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

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

    @Query("SELECT * FROM alignments")
    List<AlignmentEntity> getAllSync();

    @Query("SELECT a.* FROM alignments a " +
            "INNER JOIN documents d ON a.document_key = d.key " +
            "WHERE d.gamesystem = :gameSystem " +
            "ORDER BY a.short_name ASC")
    List<AlignmentEntity> getByGameSystem(String gameSystem);

    @Query("SELECT * FROM alignments ORDER BY key ASC")
    LiveData<List<AlignmentEntity>> getAll();

    @Query("SELECT * FROM alignments WHERE key = :key")
    LiveData<AlignmentEntity> getByKey(String key);
}