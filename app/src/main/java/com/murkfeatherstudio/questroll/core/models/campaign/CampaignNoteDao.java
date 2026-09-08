package com.murkfeatherstudio.questroll.core.models.campaign;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

/**
 * Data Access Object for {@link CampaignNoteEntity}.
 * <p>
 * Handles operations for managing campaign-specific notes, allowing them to be
 * grouped and retrieved by campaign ID.
 * </p>
 */
@Dao
public interface CampaignNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CampaignNoteEntity note);

    @Update
    void update(CampaignNoteEntity note);

    /**
     * Deletes a specific note by its ID.
     *
     * @param noteId The unique ID of the note.
     */
    @Query("DELETE FROM campaign_notes WHERE id = :noteId")
    void deleteById(long noteId);

    /**
     * Deletes all notes associated with a specific campaign.
     *
     * @param campaignId The ID of the campaign.
     */
    @Query("DELETE FROM campaign_notes WHERE campaign_id = :campaignId")
    void deleteForCampaign(long campaignId);

    /**
     * Retrieves all notes for a specific campaign, ordered by their creation date.
     *
     * @param campaignId The ID of the campaign.
     * @return LiveData containing a list of notes.
     */
    @Query("SELECT * FROM campaign_notes WHERE campaign_id = :campaignId ORDER BY createdAt DESC")
    LiveData<List<CampaignNoteEntity>> getForCampaign(long campaignId);
}
