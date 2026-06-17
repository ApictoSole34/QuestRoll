package com.fizzycoyote.qusetroll.core.models.campaign;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface CampaignDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CampaignEntity campaign);

    @Update
    void update(CampaignEntity campaign);

    @Delete
    void delete(CampaignEntity campaign);

    @Query("SELECT * FROM campaigns")
    LiveData<List<CampaignEntity>> getAll();

    @Query("SELECT * FROM campaigns WHERE id = :id")
    LiveData<CampaignEntity> getById(long id);

    @Query("SELECT * FROM campaigns WHERE id = :id")
    CampaignEntity getByIdSync(long id);

    /** Update only the characterId field — called when assigning a character. */
    @Query("UPDATE campaigns SET character_id = :characterId WHERE id = :campaignId")
    void updateCharacterId(long campaignId, long characterId);
}
