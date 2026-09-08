package com.murkfeatherstudio.questroll.view_model.feature_language;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.murkfeatherstudio.questroll.feature_language.data.repository.LanguageRepository;
import com.murkfeatherstudio.questroll.feature_language.view_model.LanguageDetailViewModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LanguageDetailViewModelTest {
    @Mock
    private LanguageRepository mockRepo;
    private LanguageDetailViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new LanguageDetailViewModel(mockRepo);
    }

    @Test
    public void deleteLanguage_callsRepoDelete() {
        // When
        viewModel.deleteLanguage(123L);

        // Then
        verify(mockRepo).deleteLanguage(eq(123L), any(), any());
    }
}