package com.fizzycoyote.qusetroll.core.models.custom.custom_character_class;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;

import java.util.List;

/**
 * Data Access Object for user-created custom character classes.
 * <p>
 * Provides methods for CRUD operations on custom classes and their associated features.
 * It also supports querying classes by game system and resolving subclass relationships.
 * </p>
 */
@Dao
public interface CustomCharacterClassDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertClass(CustomCharacterClassEntity clazz);

    @Update
    void updateClass(CustomCharacterClassEntity clazz);

    /**
     * Deletes a custom class by its ID.
     *
     * @param classId The unique ID of the custom class.
     */
    @Query("DELETE FROM custom_character_classes WHERE id = :classId")
    void deleteClass(long classId);

    /**
     * Retrieves all custom classes along with their features, ordered by name.
     *
     * @return LiveData containing a list of {@link CustomCharacterClassWithFeatures}.
     */
    @Transaction
    @Query("SELECT * FROM custom_character_classes ORDER BY name ASC")
    LiveData<List<CustomCharacterClassWithFeatures>> getAllClasses();

    /**
     * Synchronously retrieves a single custom class with its features.
     *
     * @param classId The unique ID of the custom class.
     * @return The class and its features, or null if not found.
     */
    @Transaction
    @Query("SELECT * FROM custom_character_classes WHERE id = :classId")
    CustomCharacterClassWithFeatures getClassWithFeaturesSync(long classId);

    /**
     * Retrieves all custom classes belonging to a specific game system.
     *
     * @param gameSystem The game system identifier (e.g., "5e-2014").
     * @return LiveData list of custom class entities.
     */
    @Query("SELECT * FROM custom_character_classes WHERE game_system = :gameSystem ORDER BY name ASC")
    LiveData<List<CustomCharacterClassEntity>> getClassesByGameSystem(String gameSystem);

    /**
     * Synchronously retrieves all base custom classes (not subclasses) for a game system.
     *
     * @param gameSystem The game system identifier.
     * @return A list of base custom class entities.
     */
    @Query("SELECT * FROM custom_character_classes WHERE game_system = :gameSystem AND (subclass_of IS NULL OR subclass_of = '') ORDER BY name ASC")
    List<CustomCharacterClassEntity> getBaseClassesSync(String gameSystem);

    @Query("SELECT * FROM custom_character_classes WHERE id = :id")
    LiveData<CustomCharacterClassEntity> getClassById(long id);

    @Query("SELECT * FROM custom_character_classes WHERE id = :id")
    CustomCharacterClassEntity getClassByIdSync(long id);

    /**
     * Synchronously retrieves all custom subclasses of a parent class within a game system.
     *
     * @param parentKey  The key or ID of the parent class.
     * @param gameSystem The game system identifier.
     * @return A list of custom subclass entities.
     */
    @Query("SELECT * FROM custom_character_classes WHERE subclass_of = :parentKey AND game_system = :gameSystem ORDER BY name ASC")
    List<CustomCharacterClassEntity> getSubclassesByParentKeySync(String parentKey, String gameSystem);

    /**
     * Counts how many custom classes have the specified name (used for unique name validation).
     *
     * @param name The name to check.
     * @return The count of classes with that name.
     */
    @Query("SELECT COUNT(*) FROM custom_character_classes WHERE name = :name")
    int countByName(String name);

    /**
     * Retrieves a custom class with its features as LiveData.
     *
     * @param classId The unique ID of the custom class.
     * @return LiveData containing the class and its features.
     */
    @Transaction
    @Query("SELECT * FROM custom_character_classes WHERE id = :classId")
    LiveData<CustomCharacterClassWithFeatures> getClassWithFeatures(long classId);

    @Insert
    void insertFeatures(List<CustomFeatureEntity> features);

    /**
     * Deletes all custom features associated with a specific custom class.
     *
     * @param classId The ID of the custom class.
     */
    @Query("DELETE FROM custom_features WHERE class_id = :classId")
    void deleteFeaturesForClass(long classId);
}
