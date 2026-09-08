package com.murkfeatherstudio.questroll.core.models.open5e.document;

import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e source documents (e.g., SRD, various Open5e modules).
 */
@Dao
public interface DocumentDao extends BaseDao<DocumentEntity> {

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
}
