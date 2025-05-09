package com.fizzycoyote.qusetroll.core.local_database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.fizzycoyote.qusetroll.core.models.custom.CustomConverters;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;


@Database(
        entities = {CustomLanguageEntity.class},
        version = 4,
        exportSchema = false
)
@TypeConverters({CustomConverters.class})
public abstract class UserContentDatabase extends RoomDatabase {
    private static volatile UserContentDatabase INSTANCE;

    public abstract CustomLanguageDao customLanguageDao();

    public static UserContentDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserContentDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            UserContentDatabase.class,
                            "user_content_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
