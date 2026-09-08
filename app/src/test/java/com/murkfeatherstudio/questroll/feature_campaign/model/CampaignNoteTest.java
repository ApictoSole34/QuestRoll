package com.murkfeatherstudio.questroll.feature_campaign.model;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Date;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignNoteEntity;

public class CampaignNoteTest {

    @Test
    public void campaignNote_isCreatedWithCorrectData() {
        CampaignNoteEntity note = new CampaignNoteEntity();
        note.id = 1L;
        note.campaignId = 10L;
        note.title = "Important Discovery";
        note.content = "Found a hidden map in the tavern.";
        note.createdAt = new Date();

        assertEquals("Important Discovery", note.title);
        assertEquals(10L, note.campaignId);
        assertNotNull(note.createdAt);
    }

    @Test
    public void noteContent_canHandleLongText() {
        String longText = "This is a very long campaign note describing the entire history of the Forgotten Realms...";
        CampaignNoteEntity note = new CampaignNoteEntity();
        note.content = longText;
        
        assertEquals(longText, note.content);
    }
}
