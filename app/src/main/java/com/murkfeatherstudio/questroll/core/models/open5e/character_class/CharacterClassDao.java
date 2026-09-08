package com.murkfeatherstudio.questroll.core.models.open5e.character_class;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/**
 * Data Access Object for official D&D 5e character classes stored in the compendium.
 * <p>
 * Provides methods for querying classes, including distinguishing between base classes
 * and subclasses, and filtering by game system.
 * </p>
 */
@Dao
public interface CharacterClassDao {

    /**
     * Retrieves a class along with its related features, hit points, and saving throws.
     *
     * @param classKey The unique key of the class.
     * @return A {@link CharacterClassWithDetails} object.
     */
    @Transaction
    @Query("SELECT * FROM classes WHERE class_key = :classKey")
    CharacterClassWithDetails getClassWithDetails(String classKey);

    /**
     * Retrieves all base classes (classes that are not subclasses of another).
     *
     * @return LiveData list of base class entities.
     */
    @Query("SELECT * FROM classes WHERE subclass_of_key IS NULL")
    LiveData<List<CharacterClassEntity>> getBaseClasses();

    @Query("SELECT * FROM classes WHERE class_key = :key")
    CharacterClassEntity getClassByKeySync(String key);

    @Query("SELECT * FROM classes WHERE subclass_of_key IS NULL")
    List<CharacterClassEntity> getBaseClassesSync();

    /**
     * Retrieves all subclasses associated with a parent class.
     *
     * @param parentKey The key of the parent class.
     * @return LiveData list of subclasses.
     */
    @Query("SELECT * FROM classes WHERE subclass_of_key = :parentKey")
    LiveData<List<CharacterClassEntity>> getSubclasses(String parentKey);

    /**
     * Retrieves base classes filtered by a specific game system (e.g., "5e-2014").
     *
     * @param gameSystem The game system identifier.
     * @return A list of matching base class entities.
     */
    @Query("SELECT c.* FROM classes c " +
            "INNER JOIN documents d ON c.document = d.key " +
            "WHERE d.gamesystem = :gameSystem " +
            "AND c.subclass_of_key IS NULL " +
            "ORDER BY c.name ASC")
    List<CharacterClassEntity> getBaseClassesByGameSystem(String gameSystem);

    @Query("SELECT * FROM classes ORDER BY name ASC")
    LiveData<List<CharacterClassEntity>> getAllClasses();

    @Query("SELECT COUNT(*) FROM classes")
    int getCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertClass(CharacterClassEntity entity);

    @Query("DELETE FROM classes WHERE class_key = :classKey")
    void deleteClass(String classKey);

    @Query("SELECT * FROM classes WHERE class_key = :key")
    LiveData<CharacterClassEntity> getClassByKey(String key);

    @Query("SELECT * FROM classes WHERE subclass_of_key = :parentKey")
    List<CharacterClassEntity> getSubclassesByParentKeySync(String parentKey);
}
