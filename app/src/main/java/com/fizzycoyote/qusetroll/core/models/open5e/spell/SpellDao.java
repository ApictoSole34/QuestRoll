package com.fizzycoyote.qusetroll.core.models.open5e.spell;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e spells stored in the compendium.
 * <p>
 * Provides comprehensive filtering capabilities for searching spells by name, level,
 * school, components (ritual/concentration), and source document.
 * </p>
 */
@Dao
public interface SpellDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpellEntity> spells);

    @Query("SELECT COUNT(*) FROM spells")
    int getCount();

    @Query("DELETE FROM spells")
    void deleteAll();

    /**
     * Retrieves all spells associated with a specific game system.
     *
     * @param gameSystem The game system identifier (e.g., "5e-2014").
     * @return A list of spell entities sorted by level and name.
     */
    @Query("SELECT s.* FROM spells s " +
            "INNER JOIN documents d ON s.document_key = d.key " +
            "WHERE d.gamesystem = :gameSystem " +
            "ORDER BY s.level ASC, s.name ASC")
    List<SpellEntity> getAllByGameSystem(String gameSystem);

    /**
     * Searches and filters spells based on multiple criteria.
     *
     * @param query         Partial name to match.
     * @param level         The spell level (-1 for any).
     * @param schoolKey     The unique key of the spell school.
     * @param ritual        If true, filters for ritual spells.
     * @param concentration If true, filters for concentration spells.
     * @param source        Partial name of the source document.
     * @return LiveData containing the list of matching spells.
     */
    @Query("SELECT * FROM spells WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:level = -1 OR level = :level) AND " +
            "(:schoolKey = '' OR school_key = :schoolKey) AND " +
            "(:ritual = 0 OR ritual = 1) AND " +
            "(:concentration = 0 OR concentration = 1) AND " +
            "(:source = '' OR document_name LIKE '%' || :source || '%') " +
            "ORDER BY level ASC, name ASC")
    LiveData<List<SpellEntity>> getFilteredSpells(
            String query,
            int level,
            String schoolKey,
            boolean ritual,
            boolean concentration,
            String source
    );

    @Query("SELECT * FROM spells")
    List<SpellEntity> getAllSync();

    /**
     * Retrieves a list of unique source document names present in the spell database.
     *
     * @return A list of source names.
     */
    @Query("SELECT DISTINCT document_name FROM spells WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();

    @Query("SELECT * FROM spells WHERE key = :key")
    LiveData<SpellEntity> getSpellByKey(String key);
}
