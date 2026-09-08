package com.murkfeatherstudio.questroll.feature_tools.calculator;

import static org.junit.Assert.*;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DndCalculatorViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DndCalculatorViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new DndCalculatorViewModel();
    }

    @Test
    public void initialDisplay_isZero() {
        assertEquals("0", viewModel.getDisplay().getValue());
    }

    @Test
    public void buttonClicks_updateDisplay() {
        viewModel.onButtonClick("1");
        viewModel.onButtonClick("2");
        assertEquals("12", viewModel.getDisplay().getValue());
    }

    @Test
    public void simpleAddition_worksAndAddsToHistory() {
        viewModel.onButtonClick("5");
        viewModel.onButtonClick("+");
        viewModel.onButtonClick("3");
        viewModel.onButtonClick("=");
        
        assertEquals("8", viewModel.getDisplay().getValue());
        assertFalse(viewModel.getHistory().getValue().isEmpty());
        assertEquals("5 + 3 = 8", viewModel.getHistory().getValue().get(0));
    }

    @Test
    public void clearButton_resetsState() {
        viewModel.onButtonClick("9");
        viewModel.onButtonClick("C");
        assertEquals("0", viewModel.getDisplay().getValue());
    }

    @Test
    public void toggleHistory_changesState() {
        assertFalse(viewModel.getIsHistoryOpen().getValue());
        viewModel.onButtonClick("H");
        assertTrue(viewModel.getIsHistoryOpen().getValue());
    }
}
