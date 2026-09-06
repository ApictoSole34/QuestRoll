package com.fizzycoyote.qusetroll.core.models.open5e.publisher;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for publishers of D&D 5e content (e.g., Wizards of the Coast, Kobold Press).
 * <p>
 * This DAO provides access to the list of publishers available in the Open5e compendium.
 * </p>
 */
@Dao
public interface PublisherDao {
    /**
     * Retrieves all publishers from the database.
     *
     * @return A list of all publisher entities.
     */
    @Query("SELECT * FROM publishers")
    List<PublisherEntity> getAllPublishers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PublisherEntity> publishers);
}
