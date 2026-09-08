package com.murkfeatherstudio.questroll.core.models.open5e.publisher;

import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for publishers of D&D 5e content (e.g., Wizards of the Coast, Kobold Press).
 */
@Dao
public interface PublisherDao extends BaseDao<PublisherEntity> {
    /**
     * Retrieves all publishers from the database.
     *
     * @return A list of all publisher entities.
     */
    @Query("SELECT * FROM publishers")
    List<PublisherEntity> getAllPublishers();
}
