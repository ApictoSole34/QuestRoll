package com.murkfeatherstudio.questroll.feature_tools.pdf.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.pdf.CustomPdfDao;
import com.murkfeatherstudio.questroll.core.models.custom.pdf.CustomPdfEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfListViewModel extends AndroidViewModel {
    private final CustomPdfDao pdfDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final LiveData<List<CustomPdfEntity>> allPdfs;

    public PdfListViewModel(@NonNull Application application) {
        super(application);
        pdfDao = UserContentDatabase.getInstance(application).customPdfDao();
        allPdfs = pdfDao.getAllPdfs();
    }

    public LiveData<List<CustomPdfEntity>> getAllPdfs() {
        return allPdfs;
    }

    public void addPdf(String name, Uri uri) {
        executor.execute(() -> {
            CustomPdfEntity entity = new CustomPdfEntity();
            entity.name = name;
            entity.uri = uri.toString();
            entity.dateAdded = System.currentTimeMillis();
            pdfDao.insert(entity);
        });
    }

    public void deletePdf(CustomPdfEntity pdf) {
        executor.execute(() -> pdfDao.delete(pdf));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
