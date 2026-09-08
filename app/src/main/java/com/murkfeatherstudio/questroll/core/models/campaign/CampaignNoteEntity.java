package com.murkfeatherstudio.questroll.core.models.campaign;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "campaign_notes",
        foreignKeys = @ForeignKey(
                entity = CampaignEntity.class,
                parentColumns = "id",
                childColumns = "campaign_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("campaign_id"))
public class CampaignNoteEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "campaign_id")
    public long campaignId;

    public String title;
    public String content;

    public Date createdAt;
}