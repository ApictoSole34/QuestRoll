package com.murkfeatherstudio.questroll.core.models.open5e.armor;

import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface ArmorDao extends BaseDao<ArmorEntity> {
    @Query("SELECT * FROM armor")
    List<ArmorEntity> getAllArmor();
}
