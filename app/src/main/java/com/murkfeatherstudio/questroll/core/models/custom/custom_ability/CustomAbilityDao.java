package com.murkfeatherstudio.questroll.core.models.custom.custom_ability;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomAbilityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomAbilityEntity entity);

    @Update
    void update(CustomAbilityEntity entity);

    @Delete
    void delete(CustomAbilityEntity entity);

    @Query("SELECT * FROM custom_abilities ORDER BY name ASC")
    LiveData<List<CustomAbilityEntity>> getAll();

    @Query("SELECT * FROM custom_abilities WHERE id = :id")
    LiveData<CustomAbilityEntity> getById(long id);

    @Query("SELECT * FROM custom_abilities WHERE id = :id")
    CustomAbilityEntity getByIdSync(long id);
}