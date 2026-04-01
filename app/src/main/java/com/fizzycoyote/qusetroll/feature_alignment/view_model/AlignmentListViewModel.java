package com.fizzycoyote.qusetroll.feature_alignment.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.core.models.custom.custom_alignment.CustomAlignmentDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_alignment.CustomAlignmentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.alignment.AlignmentDao;
import com.fizzycoyote.qusetroll.core.models.open5e.alignment.AlignmentEntity;
import com.fizzycoyote.qusetroll.feature_alignment.model.CombinedAlignment;

import java.util.ArrayList;
import java.util.List;

public class AlignmentListViewModel extends ViewModel {
    private final AlignmentDao alignmentDao;
    private final CustomAlignmentDao customAlignmentDao;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<CombinedAlignment>> combinedAlignments;

    public AlignmentListViewModel(AlignmentDao alignmentDao, CustomAlignmentDao customAlignmentDao) {
        this.alignmentDao = alignmentDao;
        this.customAlignmentDao = customAlignmentDao;

        LiveData<List<AlignmentEntity>> apiAlignments = Transformations.switchMap(query, q ->
                alignmentDao.getAll()
        );

        LiveData<List<CustomAlignmentEntity>> customAlignments = customAlignmentDao.getAll();

        MediatorLiveData<List<CombinedAlignment>> mediator = new MediatorLiveData<>();
        mediator.addSource(apiAlignments, api -> combine(api, customAlignments.getValue(), mediator));
        mediator.addSource(customAlignments, custom -> combine(apiAlignments.getValue(), custom, mediator));
        combinedAlignments = mediator;
    }

    private void combine(List<AlignmentEntity> api, List<CustomAlignmentEntity> custom,
                         MediatorLiveData<List<CombinedAlignment>> mediator) {
        List<CombinedAlignment> result = new ArrayList<>();
        String currentQuery = query.getValue();
        if (currentQuery == null) currentQuery = "";

        if (api != null) {
            for (AlignmentEntity a : api) {
                if (currentQuery.isEmpty() || a.key.contains(currentQuery) ||
                        (a.shortName != null && a.shortName.contains(currentQuery))) {
                    result.add(new CombinedAlignment(a));
                }
            }
        }
        if (custom != null) {
            for (CustomAlignmentEntity c : custom) {
                if (currentQuery.isEmpty() || c.name.contains(currentQuery) ||
                        (c.shortName != null && c.shortName.contains(currentQuery))) {
                    result.add(new CombinedAlignment(c));
                }
            }
        }
        result.sort((a, b) -> a.name.compareTo(b.name));
        mediator.setValue(result);
    }

    public void setQuery(String q) {
        query.setValue(q);
    }

    public LiveData<List<CombinedAlignment>> getAlignments() {
        return combinedAlignments;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final AlignmentDao alignmentDao;
        private final CustomAlignmentDao customAlignmentDao;

        public Factory(AlignmentDao alignmentDao, CustomAlignmentDao customAlignmentDao) {
            this.alignmentDao = alignmentDao;
            this.customAlignmentDao = customAlignmentDao;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new AlignmentListViewModel(alignmentDao, customAlignmentDao);
        }
    }
}