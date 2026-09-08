package com.murkfeatherstudio.questroll.feature_background.view_model;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.models.custom.custom_background.CustomBackgroundDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_background.CustomBackgroundEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundDao;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundEntity;
import com.murkfeatherstudio.questroll.feature_background.model.CombinedBackground;

import java.util.ArrayList;
import java.util.List;

public class BackgroundListViewModel extends ViewModel {

    private final BackgroundDao backgroundDao;
    private final CustomBackgroundDao customBackgroundDao;

    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> source = new MutableLiveData<>("");
    private final MutableLiveData<List<String>> sources = new MutableLiveData<>();
    private final LiveData<List<CombinedBackground>> combinedBackgrounds;

    public BackgroundListViewModel(BackgroundDao backgroundDao,
                                   CustomBackgroundDao customBackgroundDao) {
        this.backgroundDao = backgroundDao;
        this.customBackgroundDao = customBackgroundDao;

        MediatorLiveData<Pair<String, String>> filterTrigger = new MediatorLiveData<>();
        filterTrigger.addSource(query, q -> filterTrigger.setValue(
                new Pair<>(q != null ? q : "", source.getValue() != null ? source.getValue() : "")));
        filterTrigger.addSource(source, s -> filterTrigger.setValue(
                new Pair<>(query.getValue() != null ? query.getValue() : "", s != null ? s : "")));

        LiveData<List<BackgroundEntity>> open5eBackgrounds = Transformations.switchMap(
                filterTrigger, pair -> backgroundDao.getFiltered(pair.first, pair.second));

        LiveData<List<CustomBackgroundEntity>> customBackgrounds = customBackgroundDao.getAll();

        MediatorLiveData<List<CombinedBackground>> mediator = new MediatorLiveData<>();
        mediator.addSource(open5eBackgrounds, o5e ->
                combine(o5e, customBackgrounds.getValue(), mediator));
        mediator.addSource(customBackgrounds, custom ->
                combine(open5eBackgrounds.getValue(), custom, mediator));
        combinedBackgrounds = mediator;
    }

    private void combine(List<BackgroundEntity> open5e,
                         List<CustomBackgroundEntity> custom,
                         MediatorLiveData<List<CombinedBackground>> result) {
        List<CombinedBackground> combined = new ArrayList<>();
        String currentSource = source.getValue() != null ? source.getValue() : "";
        String currentQuery = query.getValue() != null ? query.getValue() : "";
        boolean sourceIsCustom = currentSource.equals("custom");

        if (custom != null) {
            for (CustomBackgroundEntity b : custom) {
                if (!currentSource.isEmpty() && !sourceIsCustom) continue;
                if (!currentQuery.isEmpty()
                        && !b.name.toLowerCase().contains(currentQuery.toLowerCase())) continue;
                combined.add(new CombinedBackground(b));
            }
        }

        if (open5e != null && !sourceIsCustom) {
            for (BackgroundEntity b : open5e) combined.add(new CombinedBackground(b));
        }

        combined.sort((a, b) -> a.name.compareTo(b.name));
        result.setValue(combined);
    }

    public void setQuery(String q) { query.setValue(q != null ? q : ""); }
    public void setSource(String s) { source.setValue(s != null ? s : ""); }

    public void loadSources() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<String> dbSources = new ArrayList<>(backgroundDao.getDistinctSources());
            dbSources.add(0, "custom");
            sources.postValue(dbSources);
        });
    }

    public LiveData<List<CombinedBackground>> getBackgrounds() { return combinedBackgrounds; }
    public LiveData<List<String>> getSources() { return sources; }
    public String getCurrentSource() { return source.getValue() != null ? source.getValue() : ""; }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final BackgroundDao backgroundDao;
        private final CustomBackgroundDao customBackgroundDao;

        public Factory(BackgroundDao backgroundDao, CustomBackgroundDao customBackgroundDao) {
            this.backgroundDao = backgroundDao;
            this.customBackgroundDao = customBackgroundDao;
        }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new BackgroundListViewModel(backgroundDao, customBackgroundDao);
        }
    }
}
