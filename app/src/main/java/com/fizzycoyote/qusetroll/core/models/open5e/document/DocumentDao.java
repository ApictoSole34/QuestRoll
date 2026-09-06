package com.fizzycoyote.qusetroll.core.models.open5e.document;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for D&D 5e source documents (e.g., SRD, various Open5e modules).
 * <p>
 * This DAO provides methods to lookup documents by their unique key or official URL,
 * primarily used for resolving license and source information for other game content.
 * </p>
 */
@Dao
public interface DocumentDao {

    /**
     * Retrieves a document by its unique key.
     *
     * @param key The document key (e.g., "srd").
     * @return The document entity, or null if not found.
     */
    @Query("SELECT * FROM documents WHERE `key` = :key LIMIT 1")
    DocumentEntity getByKey(String key);

    /**
     * Retrieves a document by its official URL.
     *
     * @param url The document's source URL.
     * @return The document entity, or null if not found.
     */
    @Query("SELECT * FROM documents WHERE url = :url LIMIT 1")
    DocumentEntity getByUrl(String url);


    @Query("SELECT * FROM documents")
    List<DocumentEntity> getAllDocuments();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DocumentEntity> documents);
}
