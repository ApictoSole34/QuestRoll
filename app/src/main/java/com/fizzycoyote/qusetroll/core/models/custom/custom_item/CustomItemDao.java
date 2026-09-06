package com.fizzycoyote.qusetroll.core.models.custom.custom_item;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.fizzycoyote.qusetroll.core.models.open5e.item.ItemEntity;

import java.util.List;

/**
 * Data Access Object for user-created custom items.
 * <p>
 * Provides CRUD operations for homebrew equipment and magic items stored in the
 * {@link com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase}.
 * </p>
 */
@Dao
public interface CustomItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomItemEntity item);

    @Update
    void update(CustomItemEntity item);

    /**
     * Deletes a custom item by its ID.
     */
    @Query("DELETE FROM custom_items WHERE id = :id")
    void delete(long id);

    /**
     * Retrieves all custom items ordered by name.
     *
     * @return LiveData list of custom item entities.
     */
    @Query("SELECT * FROM custom_items ORDER BY name ASC")
    LiveData<List<CustomItemEntity>> getAll();

    /**
     * Synchronously retrieves all custom items.
     */
    @Query("SELECT * FROM custom_items ORDER BY name ASC")
    List<CustomItemEntity> getAllSync();

    /**
     * Retrieves a specific custom item by its unique ID.
     */
    @Query("SELECT * FROM custom_items WHERE id = :id")
    LiveData<CustomItemEntity> getById(long id);

    @Query("SELECT * FROM custom_items WHERE id = :id")
    CustomItemEntity getByIdSync(long id);

    /**
     * Checks if an item with the given name already exists.
     */
    @Query("SELECT COUNT(*) FROM custom_items WHERE name = :name")
    int countByName(String name);
}
