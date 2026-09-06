package com.fizzycoyote.qusetroll.core.models.custom.custom_spell;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for user-created custom spells.
 * <p>
 * This DAO provides CRUD operations for homebrew spells, including retrieval
 * by ID and school name.
 * </p>
 */
@Dao
public interface CustomSpellDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomSpellEntity spell);

    @Update
    void update(CustomSpellEntity spell);

    /**
     * Deletes a custom spell by its ID.
     */
    @Query("DELETE FROM custom_spells WHERE id = :id")
    void delete(long id);

    /**
     * Retrieves all custom spells sorted by level and name.
     */
    @Query("SELECT * FROM custom_spells ORDER BY level ASC, name ASC")
    LiveData<List<CustomSpellEntity>> getAll();

    @Query("SELECT * FROM custom_spells")
    List<CustomSpellEntity> getAllSync();

    /**
     * Retrieves a specific custom spell by its ID.
     */
    @Query("SELECT * FROM custom_spells WHERE id = :id")
    LiveData<CustomSpellEntity> getById(long id);

    @Query("SELECT * FROM custom_spells WHERE id = :id")
    CustomSpellEntity getByIdSync(long id);

    /**
     * Retrieves all custom spells belonging to a specific school.
     */
    @Query("SELECT * FROM custom_spells WHERE school_name = :schoolName")
    List<CustomSpellEntity> getBySchoolNameSync(String schoolName);

    /**
     * Checks if a spell with the given name already exists.
     */
    @Query("SELECT COUNT(*) FROM custom_spells WHERE name = :name")
    int countByName(String name);
}
