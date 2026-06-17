package com.fizzycoyote.qusetroll.core.models.campaign;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface CampaignNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CampaignNoteEntity note);

    @Update
    void update(CampaignNoteEntity note);

    @Query("DELETE FROM campaign_notes WHERE id = :noteId")
    void deleteById(long noteId);

    @Query("DELETE FROM campaign_notes WHERE campaign_id = :campaignId")
    void deleteForCampaign(long campaignId);

    @Query("SELECT * FROM campaign_notes WHERE campaign_id = :campaignId")
    LiveData<List<CampaignNoteEntity>> getForCampaign(long campaignId);
}
