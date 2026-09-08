package com.murkfeatherstudio.questroll.core.database.base;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.List;

public interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<T> items);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(T item);

    @Update
    void update(T item);

    @Delete
    void delete(T item);
}
