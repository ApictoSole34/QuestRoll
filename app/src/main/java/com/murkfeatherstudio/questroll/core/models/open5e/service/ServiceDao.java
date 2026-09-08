package com.murkfeatherstudio.questroll.core.models.open5e.service;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface ServiceDao extends BaseDao<ServiceEntity> {

    @Query("SELECT COUNT(*) FROM services")
    int getCount();

    @Query("DELETE FROM services")
    void deleteAll();

    @Query("SELECT * FROM services ORDER BY name ASC")
    LiveData<List<ServiceEntity>> getAll();

    @Query("SELECT * FROM services WHERE key = :key")
    LiveData<ServiceEntity> getByKey(String key);
}
