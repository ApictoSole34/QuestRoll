package com.murkfeatherstudio.questroll.core.models.open5e.environment;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface EnvironmentDao extends BaseDao<EnvironmentEntity> {
    @Query("SELECT COUNT(*) FROM environments")
    int getCount();

    @Query("DELETE FROM environments")
    void deleteAll();

    @Query("SELECT * FROM environments ORDER BY name ASC")
    LiveData<List<EnvironmentEntity>> getAll();

    @Query("SELECT * FROM environments WHERE key = :key")
    LiveData<EnvironmentEntity> getByKey(String key);
}
