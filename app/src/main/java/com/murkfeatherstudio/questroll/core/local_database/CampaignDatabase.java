package com.murkfeatherstudio.questroll.core.local_database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.murkfeatherstudio.questroll.core.models.campaign.CampaignDao;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignEntity;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignNoteDao;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignNoteEntity;
import com.murkfeatherstudio.questroll.core.models.conventers.CustomConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room database for managing campaigns and campaign-related notes.
 * <p>
 * This database stores user-created campaigns, including metadata and associated
 * session or world-building notes.
 * </p>
 */
@Database(
        entities = {
                CampaignEntity.class,
                CampaignNoteEntity.class,
        },
        version = 5,
        exportSchema = false
)
@TypeConverters({CustomConverters.class})
public abstract class CampaignDatabase extends RoomDatabase {

    private static volatile CampaignDatabase INSTANCE;

    /**
     * Executor service for performing database writes on a background thread.
     */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newSingleThreadExecutor();

    public abstract CampaignDao campaignDao();
    public abstract CampaignNoteDao campaignNoteDao();

    /**
     * Gets the singleton instance of the CampaignDatabase.
     *
     * @param context The application context.
     * @return The singleton instance.
     */
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
