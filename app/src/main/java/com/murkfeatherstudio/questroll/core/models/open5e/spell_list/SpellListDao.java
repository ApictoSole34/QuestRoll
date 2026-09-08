package com.murkfeatherstudio.questroll.core.models.open5e.spell_list;

import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface SpellListDao extends BaseDao<SpellListEntity> {
    @Query("SELECT * FROM spell_list")
    List<SpellListEntity> getAllSpellLists();
}
