package com.murkfeatherstudio.questroll.feature_class.view_model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.murkfeatherstudio.questroll.feature_class.model.CombinedClass;
import com.murkfeatherstudio.questroll.feature_class.repository.ClassRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ClassListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ClassRepository repository;
    
    @Mock
    private Observer<List<CombinedClass>> observer;

    private ClassListViewModel viewModel;
    private MutableLiveData<List<CombinedClass>> classLiveData;

    @Before
    public void setUp() {
        classLiveData = new MutableLiveData<>();
        List<CombinedClass> mockList = Arrays.asList(
                new CombinedClass("fighter", "Fighter", false, null, null, "5e-2014"),
                new CombinedClass("wizard", "Wizard", false, null, null, "5e-2014"),
                new CombinedClass("homebrew", "Homebrew Class", true, null, null, "Custom")
        );
        classLiveData.setValue(mockList);
        when(repository.getCombinedClasses()).thenReturn(classLiveData);

        viewModel = new ClassListViewModel(repository);
        // LiveData needs an observer to trigger updates in MediatorLiveData/Transformations
        viewModel.getFilteredClasses().observeForever(observer);
    }

    @Test
    public void filtering_byQuery_returnsMatchingClasses() {
        viewModel.setQuery("wiz");
        List<CombinedClass> result = viewModel.getFilteredClasses().getValue();
        assertNotNull("Result should not be null", result);
        assertEquals(1, result.size());
        assertEquals("Wizard", result.get(0).name);
    }

    @Test
    public void filtering_byType_returnsOnlyCustom() {
        viewModel.setTypeFilter("Custom");
        List<CombinedClass> result = viewModel.getFilteredClasses().getValue();
        assertNotNull("Result should not be null", result);
        assertEquals(1, result.size());
        assertEquals("Homebrew Class", result.get(0).name);
    }

    @Test
    public void filtering_bySystem_returnsMatchingSystem() {
        viewModel.setSystemFilter("5e-2014");
        List<CombinedClass> result = viewModel.getFilteredClasses().getValue();
        assertNotNull("Result should not be null", result);
        assertEquals(2, result.size());
    }
}
