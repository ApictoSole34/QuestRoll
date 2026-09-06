package com.fizzycoyote.qusetroll.core.local_database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.fizzycoyote.qusetroll.core.models.character.*;

/**
 * Room database for storing player character data.
 * <p>
 * This database manages all user-created content including characters, their attributes,
 * equipment, traits, spells, and proficiencies. It is separate from the game data
 * cache in {@link Open5eDatabase}.
 * </p>
 */
@Database(
        entities = {
                CharacterEntity.class,
                CharacterAttributesEntity.class,
                CharacterClassAssignmentEntity.class,
                InventoryItemEntity.class,
                CharacterTraitEntity.class,
                CharacterSpellEntity.class,
                CharacterLanguageEntity.class,
                CharacterSkillProficiencyEntity.class,
                CharacterSavingThrowEntity.class,
                CharacterSubclassAssignmentEntity.class
        },
        version = 11,
        exportSchema = false
)
public abstract class PlayerCharacterDatabase extends RoomDatabase {
    public abstract CharacterDao characterDao();
    public abstract CharacterAttributesDao characterAttributesDao();
    public abstract CharacterClassAssignmentDao classAssignmentDao();
    public abstract InventoryItemDao inventoryItemDao();
    public abstract CharacterTraitDao traitDao();
    public abstract CharacterSpellDao spellDao();
    public abstract CharacterLanguageDao languageDao();
    public abstract CharacterSkillProficiencyDao characterSkillProficiencyDao();
    public abstract CharacterSavingThrowDao characterSavingThrowDao();
    public abstract CharacterSubclassAssignmentDao characterSubclassAssignmentDao();

    private static volatile PlayerCharacterDatabase INSTANCE;

    /**
     * Gets the singleton instance of the PlayerCharacterDatabase.
     *
     * @param context The application context.
     * @return The singleton instance.
     */
    public static PlayerCharacterDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PlayerCharacterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            PlayerCharacterDatabase.class,
                            "player_characters.db"
                    ).fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
