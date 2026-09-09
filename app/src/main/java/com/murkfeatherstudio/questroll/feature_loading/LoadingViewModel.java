package com.murkfeatherstudio.questroll.feature_loading;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.murkfeatherstudio.questroll.core.api.Open5eApiClient;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.repository.open5e.Open5eRepository;
import com.murkfeatherstudio.questroll.core.repository.open5e.Resource;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * ViewModel for {@link LoadingActivity}.
 * <p>
 * Owns the {@link Open5eRepository} and exposes the fetch progress as LiveData so that
 * the download survives configuration changes (e.g. screen rotation) instead of being
 * restarted or losing its observer, which is what happens when this logic lives in the
 * Activity itself.
 * </p>
 */
public class LoadingViewModel extends AndroidViewModel {

    private final Open5eRepository repository;

    // Result of the initial "does the DB already have data" check.
    private final MutableLiveData<Boolean> hasExistingData = new MutableLiveData<>();

    // Currently active fetch operation (null until the user/auto-check starts one).
    private LiveData<Resource<Boolean>> fetchSource;
    private final MediatorLiveData<Resource<Boolean>> fetchState = new MediatorLiveData<>();

    // Prevents re-triggering the fetch after a config change if one is already running/finished.
    private boolean fetchStarted = false;

    public LoadingViewModel(@NonNull Application application) {
        super(application);
        repository = buildRepository(application);
    }

    private Open5eRepository buildRepository(Application application) {
        Open5eDatabase db = Open5eDatabase.getInstance(application);
        Executor executor = Executors.newSingleThreadExecutor();
        return new Open5eRepository(
                Open5eApiClient.getApiService(),
                db.publisherDao(), db.gameSystemDao(), db.licenseDao(),
                db.documentDao(), db.languageDao(), db.abilityDao(),
                db.skillDao(), db.characterClassDao(), db.featureDao(),
                db.hitPointsDao(), db.savingThrowDao(), db.spellDao(),
                db.spellSchoolDao(), db.creatureDao(), db.speciesDao(),
                db.backgroundDao(), db.itemDao(), db.damageTypeDao(),
                db.alignmentDao(), db.itemRarityDao(), db.weaponPropertyDao(),
                db.serviceDao(), db.environmentDao(), db.ruleDao(), db.rulesetDao(),
                db.conditionDao(), db.creatureTypeDao(), db.itemCategoryDao(),
                db.itemSetDao(),
                executor
        );
    }

    /** True once the initial DB check has finished; value tells whether data already exists. */
    public LiveData<Boolean> getHasExistingData() {
        return hasExistingData;
    }

    /** Progress/result stream for whichever fetch is currently running. */
    public LiveData<Resource<Boolean>> getFetchState() {
        return fetchState;
    }

    public boolean isFetchStarted() {
        return fetchStarted;
    }

    /**
     * Checks if the local database already contains essential game data
     * (mirrors the old Activity#checkDataAndProceed logic).
     */
    public void checkExistingData() {
        Open5eDatabase db = Open5eDatabase.getInstance(getApplication());
        db.getQueryExecutor().execute(() -> {
            int classCount = db.characterClassDao().getCount();
            int spellCount = db.spellDao().getCount();
            hasExistingData.postValue(classCount > 0 && spellCount > 0);
        });
    }

    public void startFetchingAll() {
        if (fetchStarted) return;
        fetchStarted = true;
        attachFetchSource(repository.refreshAllData());
    }

    public void startFetchingSelected(Set<DataSection> sections) {
        if (fetchStarted) return;
        fetchStarted = true;
        attachFetchSource(repository.refreshSelectedData(sections));
    }

    /** Lets the UI retry after an ERROR without creating a brand-new ViewModel. */
    public void retryFetchAll() {
        fetchStarted = false;
        startFetchingAll();
    }

    private void attachFetchSource(LiveData<Resource<Boolean>> source) {
        if (fetchSource != null) {
            fetchState.removeSource(fetchSource);
        }
        fetchSource = source;
        fetchState.addSource(fetchSource, fetchState::setValue);
    }
}