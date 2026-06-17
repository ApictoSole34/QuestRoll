package com.fizzycoyote.qusetroll.core.models.open5e.character_class;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface CharacterClassDao {

    @Transaction
    @Query("SELECT * FROM classes WHERE class_key = :classKey")
    CharacterClassWithDetails getClassWithDetails(String classKey);

    @Query("SELECT * FROM classes WHERE subclass_of_key IS NULL")
    LiveData<List<CharacterClassEntity>> getBaseClasses();

    @Query("SELECT * FROM classes WHERE class_key = :key")
    CharacterClassEntity getClassByKeySync(String key);

    @Query("SELECT * FROM classes WHERE subclass_of_key IS NULL")
    List<CharacterClassEntity> getBaseClassesSync();

    @Query("SELECT * FROM classes WHERE subclass_of_key = :parentKey")
    LiveData<List<CharacterClassEntity>> getSubclasses(String parentKey);

    @Query("SELECT c.* FROM classes c " +
            "INNER JOIN documents d ON c.document = d.key " +
            "WHERE d.gamesystem = :gameSystem " +
            "AND c.subclass_of_key IS NULL " +
            "ORDER BY c.name ASC")
    List<CharacterClassEntity> getBaseClassesByGameSystem(String gameSystem);

    @Query("SELECT * FROM classes ORDER BY name ASC")
    LiveData<List<CharacterClassEntity>> getAllClasses();

    @Query("SELECT COUNT(*) FROM classes")
    int getCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertClass(CharacterClassEntity entity);

    @Query("DELETE FROM classes WHERE class_key = :classKey")
    void deleteClass(String classKey);

    @Query("SELECT * FROM classes WHERE class_key = :key")
    LiveData<CharacterClassEntity> getClassByKey(String key);

    @Query("SELECT * FROM classes WHERE subclass_of_key = :parentKey")
    List<CharacterClassEntity> getSubclassesByParentKeySync(String parentKey);
}