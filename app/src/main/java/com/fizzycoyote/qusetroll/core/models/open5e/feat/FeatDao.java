package com.fizzycoyote.qusetroll.core.models.open5e.feat;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FeatDao {
    @Query("SELECT * FROM feats ORDER BY name ASC")
    List<FeatEntity> getAllFeats();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FeatEntity> feats);
}
