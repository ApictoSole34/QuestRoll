package com.murkfeatherstudio.questroll.core.models.custom.custom_creature;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomCreatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomCreatureEntity creature);

    @Update
    void update(CustomCreatureEntity creature);

    @Query("DELETE FROM custom_creatures WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_creatures ORDER BY cr_decimal ASC, name ASC")
    LiveData<List<CustomCreatureEntity>> getAll();

    @Query("SELECT * FROM custom_creatures WHERE id = :id")
    LiveData<CustomCreatureEntity> getById(long id);

    @Query("SELECT * FROM custom_creatures WHERE id = :id")
    CustomCreatureEntity getByIdSync(long id);

    @Query("SELECT * FROM custom_creatures WHERE type_name = :typeName")
    List<CustomCreatureEntity> getByTypeNameSync(String typeName);

    @Query("SELECT COUNT(*) FROM custom_creatures WHERE name = :name")
    int countByName(String name);
}