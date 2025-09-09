package com.fizzycoyote.qusetroll.core.models.open5e.spell_list;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SpellListDao {
    @Query("SELECT * FROM spell_list")
    List<SpellListEntity> getAllSpellLists();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpellListEntity> spellList);
}
