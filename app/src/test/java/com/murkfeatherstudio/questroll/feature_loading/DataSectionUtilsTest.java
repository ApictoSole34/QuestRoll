package com.murkfeatherstudio.questroll.feature_loading;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;

public class DataSectionUtilsTest {

    @Test
    public void getDependencies_forClasses_includesDocumentsAndSystems() {
        List<DataSection> deps = DataSectionUtils.getDependencies(DataSection.CLASSES);
        assertTrue(deps.contains(DataSection.DOCUMENTS));
        assertTrue(deps.contains(DataSection.GAME_SYSTEMS));
    }

    @Test
    public void getDependencies_forSpells_includesSpellSchools() {
        List<DataSection> deps = DataSectionUtils.getDependencies(DataSection.SPELLS);
        assertTrue(deps.contains(DataSection.SPELL_SCHOOLS));
    }

    @Test
    public void getDependencies_forDocuments_includesInfrastructure() {
        List<DataSection> deps = DataSectionUtils.getDependencies(DataSection.DOCUMENTS);
        assertTrue(deps.contains(DataSection.PUBLISHERS));
        assertTrue(deps.contains(DataSection.LICENSES));
        assertTrue(deps.contains(DataSection.GAME_SYSTEMS));
    }

    @Test
    public void getDependencies_forBaseInfrastructure_hasNoExtraDeps() {
        // Publishers is a root, but our logic adds DOCUMENTS to everything not in the base list.
        // Let's check a base one.
        List<DataSection> deps = DataSectionUtils.getDependencies(DataSection.PUBLISHERS);
        assertFalse(deps.contains(DataSection.DOCUMENTS));
    }
}
