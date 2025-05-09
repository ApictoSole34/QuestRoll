package com.fizzycoyote.qusetroll.view_model.feature_language;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageDao;
import com.fizzycoyote.qusetroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageDao;
import com.fizzycoyote.qusetroll.feature_language.data.repository.LanguageRepository;
import com.fizzycoyote.qusetroll.feature_language.view_model.CustomLanguageCreateViewModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Executor;

@RunWith(MockitoJUnitRunner.class)
public class CustomLanguageCreateViewModelTest {
    @Mock private LanguageRepository mockRepo;
    @Mock private CustomLanguageDao mockCustomDao;
    @Mock private LanguageDao mockOpen5eDao;
    @Mock private Executor mockExecutor;

    private CustomLanguageCreateViewModel viewModel;

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockExecutor).execute(any(Runnable.class));

        when(mockRepo.getCustomDao()).thenReturn(mockCustomDao);
        when(mockRepo.getOpen5eDao()).thenReturn(mockOpen5eDao);
        when(mockRepo.getExecutor()).thenReturn(mockExecutor);

        viewModel = new CustomLanguageCreateViewModel(mockRepo);
    }

    @Test
    public void saveLanguage_newName_checksDuplicate() {
        // Given
        when(mockCustomDao.countByName("NewLang")).thenReturn(0);

        // When
        viewModel.saveLanguage("NewLang", "Desc", false, false, null, -1);

        // Then
        verify(mockCustomDao).insert(any(CustomLanguageEntity.class));
        assertTrue(viewModel.getSaveResult().getValue());
    }

    @Test
    public void saveLanguage_duplicateName_returnsFalse() {
        // Given
        when(mockCustomDao.countByName("Existing")).thenReturn(1);

        // When
        viewModel.saveLanguage("Existing", "Desc", false, false, null, -1);

        // Then
        verify(mockCustomDao, never()).insert(any());
        assertFalse(viewModel.getSaveResult().getValue());
    }
}