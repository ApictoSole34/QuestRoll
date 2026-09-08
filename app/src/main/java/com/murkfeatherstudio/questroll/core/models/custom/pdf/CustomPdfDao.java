package com.murkfeatherstudio.questroll.core.models.custom.pdf;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface CustomPdfDao {
    @Insert
    long insert(CustomPdfEntity pdf);

    @Query("SELECT * FROM custom_pdfs ORDER BY dateAdded DESC")
    LiveData<List<CustomPdfEntity>> getAllPdfs();

    @Delete
    void delete(CustomPdfEntity pdf);

    @Query("SELECT * FROM custom_pdfs WHERE id = :id")
    CustomPdfEntity getById(long id);
}
