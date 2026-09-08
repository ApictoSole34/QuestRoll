package com.murkfeatherstudio.questroll.core.models.open5e.character_class.hit_points;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface HitPointsDao {
    @Query("SELECT * FROM hitpoints WHERE class_key_ref = :classKey")
    LiveData<HitPointsEntity> getHitPointsForClass(String classKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHitPoints(HitPointsEntity hp);

    @Query("DELETE FROM hitpoints WHERE class_key_ref = :classKey")
    void deleteHitPointsForClass(String classKey);
}
