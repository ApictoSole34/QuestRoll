package com.fizzycoyote.qusetroll.core.local_database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.fizzycoyote.qusetroll.core.models.campaign.CampaignDao;
import com.fizzycoyote.qusetroll.core.models.campaign.CampaignEntity;
import com.fizzycoyote.qusetroll.core.models.campaign.CampaignNoteDao;
import com.fizzycoyote.qusetroll.core.models.campaign.CampaignNoteEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Database(
        entities = {
                CampaignEntity.class,
                CampaignNoteEntity.class,
        },
        version = 4,
        exportSchema = false
)
public abstract class CampaignDatabase extends RoomDatabase {

    private static volatile CampaignDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newSingleThreadExecutor();

    public abstract CampaignDao campaignDao();
    public abstract CampaignNoteDao campaignNoteDao();

    public static CampaignDatabase getInstance(Context context) {

        if (INSTANCE == null) {

            synchronized (CampaignDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    CampaignDatabase.class,
                                    "campaign_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}