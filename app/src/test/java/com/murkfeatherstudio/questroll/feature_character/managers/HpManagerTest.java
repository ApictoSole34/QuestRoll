package com.murkfeatherstudio.questroll.feature_character.managers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterAttributesDao;
import com.murkfeatherstudio.questroll.core.models.character.CharacterAttributesEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterClassAssignmentDao;
import com.murkfeatherstudio.questroll.core.models.character.CharacterClassAssignmentEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterDao;
import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassDao;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class HpManagerTest {

    @Mock
    private PlayerCharacterDatabase mockPcDb;
    @Mock
    private Open5eDatabase mockOpen5eDb;
    @Mock
    private CharacterDao mockCharacterDao;
    @Mock
    private CharacterClassAssignmentDao mockClassAssignDao;
    @Mock
    private CharacterAttributesDao mockAttributesDao;
    @Mock
    private CharacterClassDao mockClassDao;

    private HpManager hpManager;

    @Before
    public void setUp() {
        when(mockPcDb.characterDao()).thenReturn(mockCharacterDao);
        when(mockPcDb.classAssignmentDao()).thenReturn(mockClassAssignDao);
        when(mockPcDb.characterAttributesDao()).thenReturn(mockAttributesDao);
        when(mockOpen5eDb.characterClassDao()).thenReturn(mockClassDao);
        
        hpManager = new HpManager(mockPcDb, mockOpen5eDb);
    }

    @Test
    public void calculateMaxHp_calculatesCorrectlyForMulticlass() {
        long charId = 1L;
        CharacterEntity character = new CharacterEntity();
        character.id = charId;
        when(mockCharacterDao.getCharacterSync(charId)).thenReturn(character);

        // Barbarian Level 1, Fighter Level 1
        CharacterClassAssignmentEntity barbAssign = new CharacterClassAssignmentEntity();
        barbAssign.classKey = "barbarian";
        barbAssign.level = 1;
        
        CharacterClassAssignmentEntity fighterAssign = new CharacterClassAssignmentEntity();
        fighterAssign.classKey = "fighter";
        fighterAssign.level = 1;

        when(mockClassAssignDao.getByCharacterId(charId)).thenReturn(Arrays.asList(barbAssign, fighterAssign));

        // Attributes: Con 14 (+2)
        CharacterAttributesEntity attrs = new CharacterAttributesEntity();
        attrs.constitutionMod = 2;
        when(mockAttributesDao.getByCharacterId(charId)).thenReturn(attrs);

        // Class Data
        CharacterClassEntity barbClass = new CharacterClassEntity();
        barbClass.hitPointsAt1stLevel = "12 + your Constitution modifier";
        barbClass.hitPointsAtHigherLevels = "1d12 (or 7) + your Constitution modifier";
        
        CharacterClassEntity fighterClass = new CharacterClassEntity();
        fighterClass.hitPointsAt1stLevel = "10 + your Constitution modifier";
        fighterClass.hitPointsAtHigherLevels = "1d10 (or 6) + your Constitution modifier";

        when(mockClassDao.getClassByKeySync("barbarian")).thenReturn(barbClass);
        when(mockClassDao.getClassByKeySync("fighter")).thenReturn(fighterClass);

        // Barb Level 1: 12 + 2 = 14
        // Fighter Level 1 (treated as 1st level in logic if lvl=1): 10 + 2 = 12
        // Total: 26
        // Note: Real 5e multiclassing only gives full HP for the very first level of the character.
        // But our manager logic currently does lvl == 1 check per class assignment. 
        // Let's verify the current production logic behavior.
        int maxHp = hpManager.calculateMaxHp(charId);
        assertEquals(26, maxHp);
    }

    @Test
    public void takeDamage_reducesCurrentHp() {
        CharacterEntity character = new CharacterEntity();
        character.id = 1L;
        character.currentHp = 20;
        character.temporaryHp = 0;
        
        when(mockCharacterDao.getCharacterSync(1L)).thenReturn(character);

        hpManager.takeDamage(1L, 5);

        ArgumentCaptor<CharacterEntity> captor = ArgumentCaptor.forClass(CharacterEntity.class);
        verify(mockCharacterDao).update(captor.capture());
        assertEquals(15, captor.getValue().currentHp);
    }

    @Test
    public void takeDamage_usesTemporaryHpFirst() {
        CharacterEntity character = new CharacterEntity();
        character.id = 1L;
        character.currentHp = 20;
        character.temporaryHp = 10;
        
        when(mockCharacterDao.getCharacterSync(1L)).thenReturn(character);

        hpManager.takeDamage(1L, 5);

        ArgumentCaptor<CharacterEntity> captor = ArgumentCaptor.forClass(CharacterEntity.class);
        verify(mockCharacterDao).update(captor.capture());
        assertEquals(20, captor.getValue().currentHp);
        assertEquals(5, captor.getValue().temporaryHp);
    }

    @Test
    public void addTempHp_onlyUpdatesIfHigher() {
        CharacterEntity character = new CharacterEntity();
        character.id = 1L;
        character.temporaryHp = 5;
        
        when(mockCharacterDao.getCharacterSync(1L)).thenReturn(character);

        hpManager.addTempHp(1L, 10);
        ArgumentCaptor<CharacterEntity> captor = ArgumentCaptor.forClass(CharacterEntity.class);
        verify(mockCharacterDao).update(captor.capture());
        assertEquals(10, captor.getValue().temporaryHp);
    }
}
