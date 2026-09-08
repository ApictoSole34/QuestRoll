package com.murkfeatherstudio.questroll.feature_character.view_model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.alignment.AlignmentDao;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundDao;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesDao;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassDao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CharacterCreatorViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    Application mockApplication;
    @Mock
    Open5eDatabase mockOpen5eDb;
    @Mock
    PlayerCharacterDatabase mockPcDb;
    @Mock
    AlignmentDao mockAlignmentDao;
    @Mock
    BackgroundDao mockBackgroundDao;
    @Mock
    SpeciesDao mockSpeciesDao;
    @Mock
    CharacterClassDao mockClassDao;

    private CharacterCreatorViewModel viewModel;

    @Before
    public void setUp() {
        // Mock the DAOs to avoid NPE when loadReferenceData is called in constructor
        when(mockOpen5eDb.alignmentDao()).thenReturn(mockAlignmentDao);
        when(mockOpen5eDb.backgroundDao()).thenReturn(mockBackgroundDao);
        when(mockOpen5eDb.speciesDao()).thenReturn(mockSpeciesDao);
        when(mockOpen5eDb.characterClassDao()).thenReturn(mockClassDao);

        viewModel = new CharacterCreatorViewModel(mockApplication, mockOpen5eDb, mockPcDb);
    }

    @Test
    public void setCharacterName_updatesLiveData() {
        viewModel.setCharacterName("Drizzt");
        assertEquals("Drizzt", viewModel.getCharacterName().getValue());
    }

    @Test
    public void addClass_updatesListCorrectly() {
        CharacterClassEntity fighter = new CharacterClassEntity();
        fighter.key = "fighter";
        fighter.name = "Fighter";

        viewModel.addClass(fighter, 1);
        
        List<CharacterCreatorViewModel.ClassAssignment> assignments = viewModel.getClassAssignments().getValue();
        assertNotNull(assignments);
        assertEquals(1, assignments.size());
        assertEquals("fighter", assignments.get(0).classKey);
    }

    @Test
    public void pointBuy_doesNotExceedLimit() {
        // Default is all 8s (cost 0)
        viewModel.generateAttributes("POINT_BUY");
        
        // Raising a stat to 15 costs 9 points.
        // 3 stats to 15 = 27 points. 4th stat to 15 should fail.
        viewModel.adjustAttributeForPointBuy(0, 7); // Stat 0 -> 15 (Cost 9)
        viewModel.adjustAttributeForPointBuy(1, 7); // Stat 1 -> 15 (Cost 9)
        viewModel.adjustAttributeForPointBuy(2, 7); // Stat 2 -> 15 (Cost 9, Total 27)
        
        viewModel.adjustAttributeForPointBuy(3, 1); // Stat 3 -> 9 (Total would be 28)
        
        assertEquals(Integer.valueOf(8), viewModel.getAttributes().getValue().get(3));
    }

    @Test
    public void removeClass_updatesList() {
        CharacterClassEntity c = new CharacterClassEntity();
        c.key = "rogue";
        viewModel.addClass(c, 1);
        
        viewModel.removeClass(0);
        
        assertTrue(viewModel.getClassAssignments().getValue().isEmpty());
    }
}
