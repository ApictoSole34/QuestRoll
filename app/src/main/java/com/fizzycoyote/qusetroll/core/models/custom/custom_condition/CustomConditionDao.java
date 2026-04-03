package com.fizzycoyote.qusetroll.core.models.custom.custom_condition;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CustomConditionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomConditionEntity condition);

    @Update
    void update(CustomConditionEntity condition);

    @Query("DELETE FROM custom_conditions WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM custom_conditions ORDER BY name ASC")
    LiveData<List<CustomConditionEntity>> getAll();

    @Query("SELECT * FROM custom_conditions WHERE id = :id")
    LiveData<CustomConditionEntity> getById(long id);

    @Query("SELECT * FROM custom_conditions WHERE id = :id")
    CustomConditionEntity getByIdSync(long id);

    @Query("SELECT COUNT(*) FROM custom_conditions WHERE name = :name")
    int countByName(String name);
}
