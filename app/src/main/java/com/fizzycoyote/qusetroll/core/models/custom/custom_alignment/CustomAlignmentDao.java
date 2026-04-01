package com.fizzycoyote.qusetroll.core.models.custom.custom_alignment;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomAlignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomAlignmentEntity alignment);

    @Update
    void update(CustomAlignmentEntity alignment);

    @Query("DELETE FROM custom_alignments WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_alignments ORDER BY name ASC")
    LiveData<List<CustomAlignmentEntity>> getAll();

    @Query("SELECT * FROM custom_alignments WHERE id = :id")
    LiveData<CustomAlignmentEntity> getById(long id);

    @Query("SELECT * FROM custom_alignments WHERE id = :id")
    CustomAlignmentEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_alignments WHERE name = :name")
    int countByName(String name);
}