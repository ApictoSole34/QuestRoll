package com.fizzycoyote.qusetroll.view_model.feature_language;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageDao;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageEntity;
import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;
import com.fizzycoyote.qusetroll.feature_language.model.CombinedLanguage;
import com.fizzycoyote.qusetroll.feature_language.view_model.LanguageListViewModel;
import com.fizzycoyote.qusetroll.util.feature_language.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

@RunWith(MockitoJUnitRunner.class)
public class LanguageListViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private LanguageRepository mockRepo;
    @Mock private Executor mockExecutor;
    @Mock private LanguageDao mockOpen5eDao;
    @Mock private CustomLanguageDao mockCustomDao;

    private LanguageListViewModel viewModel;
    private MutableLiveData<List<LanguageEntity>> open5eLiveData;
    private MutableLiveData<List<CustomLanguageEntity>> customLiveData;

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockExecutor).execute(any(Runnable.class));

        open5eLiveData = new MutableLiveData<>();
        customLiveData = new MutableLiveData<>();

        when(mockRepo.getAllOpen5eLanguages()).thenReturn(open5eLiveData);
        when(mockRepo.getAllCustomLanguages()).thenReturn(customLiveData);
        when(mockRepo.getExecutor()).thenReturn(mockExecutor);
        when(mockRepo.getOpen5eDao()).thenReturn(mockOpen5eDao);
        when(mockRepo.getCustomDao()).thenReturn(mockCustomDao);

        viewModel = new LanguageListViewModel(mockRepo);
    }

    @Test
    public void combineData_mergesOpen5eAndCustom() throws InterruptedException {
        LanguageEntity open5eEntity = new LanguageEntity();
        open5eEntity.name = "Elvish";
        open5eEntity.key = "elvish";
        open5eEntity.scriptLanguage = "script/elvish";
        open5eEntity.isExotic = false;
        open5eEntity.isSecret = false;

        LanguageEntity scriptEntity = new LanguageEntity();
        scriptEntity.name = "Elvish Script";
        when(mockOpen5eDao.getByKey("elvish")).thenReturn(scriptEntity);

        CustomLanguageEntity customEntity = new CustomLanguageEntity("Dwarvish", "Desc");
        customEntity.id = 1L;
        customEntity.scriptLanguageId = "2";
        customEntity.isExotic = false;
        customEntity.isSecret = false;

        CustomLanguageEntity customScriptEntity = new CustomLanguageEntity("Dwarvish Script", "Desc");
        customScriptEntity.id = 2L;
        when(mockCustomDao.findById(2L)).thenReturn(customScriptEntity);

        open5eLiveData.setValue(Collections.singletonList(open5eEntity));
        customLiveData.setValue(Collections.singletonList(customEntity));

        List<CombinedLanguage> result = LiveDataTestUtil.getOrAwaitValue(
                viewModel.getCombinedLanguages(),
                list -> list != null && list.size() == 2
        );

        assertEquals(2, result.size());
        assertEquals("Elvish", result.get(0).getName());
        assertEquals("Dwarvish", result.get(1).getName());
    }
}