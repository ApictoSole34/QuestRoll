package com.fizzycoyote.qusetroll.core.models.open5e.publisher;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PublisherDao {
    @Query("SELECT * FROM publishers")
    List<PublisherEntity> getAllPublishers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PublisherEntity> publishers);
}
