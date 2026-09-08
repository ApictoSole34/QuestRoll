package com.murkfeatherstudio.questroll.repository.feature_language;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageDao;
import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentDao;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageDao;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageEntity;
import com.murkfeatherstudio.questroll.feature_language.data.repository.LanguageRepository;
import com.murkfeatherstudio.questroll.feature_language.model.CombinedLanguage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Executor;

@RunWith(MockitoJUnitRunner.class)
public class LanguageRepositoryTest {
    @Mock private DocumentDao mockDocumentDao;
    @Mock private LanguageDao mockOpen5eDao;
    @Mock private CustomLanguageDao mockCustomDao;
    @Mock private Executor mockExecutor;

    private LanguageRepository repository;

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockExecutor).execute(any(Runnable.class));

        repository = new LanguageRepository(mockDocumentDao, mockOpen5eDao, mockCustomDao, mockExecutor);
    }

    @Test
    public void getCombinedLanguage_open5eKey_callsOpen5eDao() {
        // Given
        LanguageEntity mockEntity = new LanguageEntity();
        when(mockOpen5eDao.getByKey("test_key")).thenReturn(mockEntity);
        CombinedLanguage[] result = new CombinedLanguage[1];

        // When
        repository.getCombinedLanguage("test_key", null,
                lang -> result[0] = lang,
                e -> fail());

        // Then
        assertNotNull(result[0]);
        verify(mockOpen5eDao).getByKey("test_key");
    }

    @Test
    public void deleteLanguage_callsCustomDaoDelete() {
        // When
        repository.deleteLanguage(123L, () -> {}, e -> {});

        // Then
        verify(mockCustomDao).deleteById(123L);
    }

    @Test
    public void getScriptLanguage_numericKey_callsCustomDao() {
        // Given
        CustomLanguageEntity mockEntity = new CustomLanguageEntity("Dwarvish", "Test Description");
        when(mockCustomDao.findById(456L)).thenReturn(mockEntity);

        // When
        repository.getScriptLanguage("456",
                Assert::assertNotNull,
                e -> fail());

        // Then
        verify(mockCustomDao).findById(456L);
    }
}