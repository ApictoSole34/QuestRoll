package com.murkfeatherstudio.questroll.core.models.open5e.feat;

import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface FeatDao extends BaseDao<FeatEntity> {
    @Query("SELECT * FROM feats ORDER BY name ASC")
    List<FeatEntity> getAllFeats();
}
