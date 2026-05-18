package com.fizzycoyote.qusetroll.core.local_database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.fizzycoyote.qusetroll.core.models.character.*;

@Database(
        entities = {
                CharacterEntity.class,
                CharacterAttributesEntity.class,
                CharacterClassAssignmentEntity.class,
                InventoryItemEntity.class,
                CharacterTraitEntity.class,
                CharacterSpellEntity.class,
                CharacterLanguageEntity.class,
                CharacterSkillProficiencyEntity.class
        },
        version = 5,
        exportSchema = false
)
public abstract class PlayerCharacterDatabase extends RoomDatabase {
    public abstract CharacterDao characterDao();
    public abstract CharacterAttributesDao attributesDao();
    public abstract CharacterClassAssignmentDao classAssignmentDao();
    public abstract InventoryItemDao inventoryItemDao();
    public abstract CharacterTraitDao traitDao();
    public abstract CharacterSpellDao spellDao();
    public abstract CharacterLanguageDao languageDao();
    public abstract CharacterSkillProficiencyDao characterSkillProficiencyDao();

    private static volatile PlayerCharacterDatabase INSTANCE;

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