package com.murkfeatherstudio.questroll.core.models.campaign;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

/**
 * Data Access Object for the {@link CampaignEntity}.
 * <p>
 * Manages storage operations for user-created campaigns, including linking
 * a campaign to a specific player character.
 * </p>
 */
@Dao
public interface CampaignDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CampaignEntity campaign);

    @Update
    void update(CampaignEntity campaign);

    @Delete
    void delete(CampaignEntity campaign);

    /**
     * Retrieves all campaigns stored in the database.
     *
     * @return LiveData containing a list of all campaign entities.
     */
    @Query("SELECT * FROM campaigns")
    LiveData<List<CampaignEntity>> getAll();

    /**
     * Retrieves a campaign by its ID.
     *
     * @param id The unique ID of the campaign.
     * @return LiveData containing the campaign entity.
     */
    @Query("SELECT * FROM campaigns WHERE id = :id")
    LiveData<CampaignEntity> getById(long id);

    /**
     * Synchronously retrieves a campaign by its ID.
     *
     * @param id The unique ID of the campaign.
     * @return The campaign entity, or null if not found.
     */
    @Query("SELECT * FROM campaigns WHERE id = :id")
    CampaignEntity getByIdSync(long id);

    /**
     * Updates the character associated with a specific campaign.
     *
     * @param campaignId  The ID of the campaign to update.
     * @param characterId The ID of the character to assign (use -1 to unassign).
     */
    @Query("UPDATE campaigns SET character_id = :characterId WHERE id = :campaignId")
    void updateCharacterId(long campaignId, long characterId);
}
