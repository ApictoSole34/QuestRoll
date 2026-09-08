package com.murkfeatherstudio.questroll.core.local_database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.murkfeatherstudio.questroll.core.local_database.dao.character.CharacterResourceDao;
import com.murkfeatherstudio.questroll.core.models.character.*;

/**
 * Room database for storing player character data.
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
                CharacterSubclassAssignmentEntity.class,
                CharacterResourceEntity.class
        },
        version = 12,
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
    public abstract CharacterResourceDao characterResourceDao();

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
