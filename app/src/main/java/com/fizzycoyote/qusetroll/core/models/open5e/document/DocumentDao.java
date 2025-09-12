package com.fizzycoyote.qusetroll.core.models.open5e.document;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DocumentDao {

    @Query("SELECT * FROM documents WHERE `key` = :key LIMIT 1")
    DocumentEntity getByKey(String key);

    @Query("SELECT * FROM documents WHERE url = :url LIMIT 1")
    DocumentEntity getByUrl(String url);


    @Query("SELECT * FROM documents")
    List<DocumentEntity> getAllDocuments();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DocumentEntity> documents);
}
