package com.fizzycoyote.qusetroll.core.models.open5e.species;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e species (races) stored in the compendium.
 * <p>
 * This DAO provides methods to access racial data, including distinguishing
 * between base species and subspecies, and filtering by game system.
 * </p>
 */
@Dao
public interface SpeciesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpeciesEntity> species);

    @Query("SELECT COUNT(*) FROM species")
    int getCount();

    @Query("DELETE FROM species")
    void deleteAll();

    @Query("SELECT * FROM species WHERE key = :key")
    SpeciesEntity getByKeySync(String key);

    @Query("SELECT * FROM species")
    List<SpeciesEntity> getAllSync();

    /**
     * Retrieves all species associated with a specific game system.
     *
     * @param gameSystem The game system identifier.
     * @return A list of species entities sorted by name.
     */
    @Query("SELECT s.* FROM species s " +
            "INNER JOIN documents d ON s.document_key = d.`key` " +
            "WHERE d.gamesystem = :gameSystem " +
            "ORDER BY s.name ASC")
    List<SpeciesEntity> getByGameSystem(String gameSystem);

    /**
     * Retrieves only the base species (not subspecies) for a game system.
     *
     * @param gameSystem The game system identifier.
     * @return A list of base species.
     */
    @Query("SELECT s.* FROM species s " +
            "INNER JOIN documents d ON s.document_key = d.`key` " +
            "WHERE d.gamesystem = :gameSystem " +
            "AND s.is_subspecies = 0 " +
            "ORDER BY s.name ASC")
    List<SpeciesEntity> getBaseSpeciesByGameSystem(String gameSystem);

    /**
     * Retrieves all subspecies belonging to a parent species within a game system.
     *
     * @param parentKey  The key of the parent species.
     * @param gameSystem The game system identifier.
     * @return A list of subspecies.
     */
    @Query("SELECT s.* FROM species s " +
            "INNER JOIN documents d ON s.document_key = d.`key` " +
            "WHERE d.gamesystem = :gameSystem " +
            "AND s.subspecies_of = :parentKey " +
            "ORDER BY s.name ASC")
    List<SpeciesEntity> getSubspeciesByParent(String parentKey, String gameSystem);

    /**
     * Searches and filters species based on name, subspecies status, and source.
     *
     * @param query          Partial name to match.
     * @param subspeciesOnly If 1, only returns subspecies.
     * @param mainOnly       If 1, only returns base species.
     * @param source         Partial source document name to match.
     * @return LiveData containing the matching species entities.
     */
    @Query("SELECT * FROM species WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:subspeciesOnly = 0 OR is_subspecies != 0) AND " +
            "(:mainOnly = 0 OR is_subspecies = 0) AND " +
            "(:source = '' OR document_name LIKE '%' || :source || '%') " +
            "ORDER BY name ASC")
    LiveData<List<SpeciesEntity>> getFilteredSpecies(
            String query,
            int subspeciesOnly,
            int mainOnly,
            String source
    );

    @Query("SELECT * FROM species WHERE key = :key")
    LiveData<SpeciesEntity> getByKey(String key);

    @Query("SELECT * FROM species WHERE subspecies_of LIKE '%' || :parentKey || '%' ORDER BY name ASC")
    LiveData<List<SpeciesEntity>> getSubspeciesOf(String parentKey);

    /**
     * Retrieves a list of unique source document names present in the species database.
     */
    @Query("SELECT DISTINCT document_name FROM species WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();
}
