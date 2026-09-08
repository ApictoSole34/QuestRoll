package com.murkfeatherstudio.questroll.core.models.open5e.spell;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e spells stored in the compendium.
 */
@Dao
public interface SpellDao extends BaseDao<SpellEntity> {

    @Query("SELECT COUNT(*) FROM spells")
    int getCount();

    @Query("DELETE FROM spells")
    void deleteAll();

    /**
     * Retrieves all spells associated with a specific game system.
     */
    @Query("SELECT s.* FROM spells s " +
            "INNER JOIN documents d ON s.document_key = d.key " +
            "WHERE d.gamesystem = :gameSystem " +
            "ORDER BY s.level ASC, s.name ASC")
    List<SpellEntity> getAllByGameSystem(String gameSystem);

    /**
     * Searches and filters spells based on multiple criteria.
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
     */
    @Query("SELECT DISTINCT document_name FROM spells WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();

    @Query("SELECT * FROM spells WHERE key = :key")
    LiveData<SpellEntity> getSpellByKey(String key);
}
