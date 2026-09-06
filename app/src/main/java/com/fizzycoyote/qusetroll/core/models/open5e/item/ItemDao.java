package com.fizzycoyote.qusetroll.core.models.open5e.item;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e items (equipment, magic items, weapons, etc.) stored in the compendium.
 * <p>
 * Provides filtering by name, category, source, rarity, and magic properties.
 * </p>
 */
@Dao
public interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemEntity> items);

    @Query("SELECT COUNT(*) FROM items")
    int getCount();

    @Query("DELETE FROM items")
    void deleteAll();

    /**
     * Retrieves all items associated with a specific game system.
     *
     * @param gameSystem The game system identifier.
     * @return A list of item entities sorted by name.
     */
    @Query("SELECT i.* FROM items i " +
            "INNER JOIN documents d ON i.document_key = d.key " +
            "WHERE d.gamesystem = :gameSystem " +
            "ORDER BY i.name ASC")
    List<ItemEntity> getAllByGameSystem(String gameSystem);

    /**
     * Synchronously retrieves a list of items by their unique keys and game system.
     */
    @Query("SELECT i.* FROM items i " +
            "INNER JOIN documents d ON i.document_key = d.key " +
            "WHERE i.key IN (:keys) AND d.gamesystem = :gameSystem")
    List<ItemEntity> getByKeysAndGameSystemSync(List<String> keys, String gameSystem);

    /**
     * Searches and filters items based on various criteria.
     *
     * @param query              Partial name to match.
     * @param categoryName       Exact category name to filter by.
     * @param source             Exact document name to filter by.
     * @param magicOnly          If 1, only returns magic items.
     * @param rarity             Exact rarity name to filter by.
     * @param attunement         If 1, only returns items requiring attunement.
     * @return LiveData containing the filtered list of items.
     */
    @Query("SELECT * FROM items WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:categoryName = '' OR category_name = :categoryName) AND " +
            "(:source = '' OR document_name = :source) AND " +
            "(:magicOnly = 0 OR is_magic_item = 1) AND " +
            "(:rarity = '' OR rarity_name = :rarity) AND " +
            "(:attunement = 0 OR requires_attunement = 1) " +
            "ORDER BY name ASC")
    LiveData<List<ItemEntity>> getFiltered(String query, String categoryName, String source,
                                           int magicOnly, String rarity, int attunement);

    /**
     * Retrieves a specific item by its unique key.
     */
    @Query("SELECT * FROM items WHERE key = :key")
    LiveData<ItemEntity> getByKey(String key);

    @Query("SELECT * FROM items WHERE key = :key")
    ItemEntity getByKeySync(String key);

    @Query("SELECT * FROM items ORDER BY name ASC")
    List<ItemEntity> getAllSync();

    /**
     * Retrieves a list of unique source document names present in the item database.
     */
    @Query("SELECT DISTINCT document_name FROM items WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();

    @Query("SELECT * FROM items WHERE key = :key LIMIT 1")
    ItemEntity getByKeySyncLimit(String key);

    /**
     * Retrieves a list of all unique item categories.
     */
    @Query("SELECT DISTINCT category_name FROM items ORDER BY category_name ASC")
    List<String> getDistinctCategories();
}
