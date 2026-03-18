package com.fizzycoyote.qusetroll.core.models.open5e.spell;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SpellDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpellEntity> spells);

    @Query("SELECT COUNT(*) FROM spells")
    int getCount();

    @Query("DELETE FROM spells")
    void deleteAll();

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

    @Query("SELECT DISTINCT document_name FROM spells WHERE document_name IS NOT NULL ORDER BY document_name ASC")
    List<String> getDistinctSources();

    @Query("SELECT * FROM spells WHERE key = :key")
    LiveData<SpellEntity> getSpellByKey(String key);
}
