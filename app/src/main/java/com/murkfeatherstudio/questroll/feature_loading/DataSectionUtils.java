package com.murkfeatherstudio.questroll.feature_loading;

import java.util.ArrayList;
import java.util.List;

public class DataSectionUtils {
    public static List<DataSection> getDependencies(DataSection section) {
        List<DataSection> deps = new ArrayList<>();
        // Most content depends on basic infrastructure
        if (section != DataSection.PUBLISHERS && section != DataSection.LICENSES &&
                section != DataSection.GAME_SYSTEMS && section != DataSection.DOCUMENTS) {
            deps.add(DataSection.DOCUMENTS);
        }

        switch (section) {
            case DOCUMENTS:
                deps.add(DataSection.PUBLISHERS);
                deps.add(DataSection.LICENSES);
                deps.add(DataSection.GAME_SYSTEMS);
                break;
            case CLASSES:
                deps.add(DataSection.GAME_SYSTEMS);
                break;
            case SPELLS:
                deps.add(DataSection.SPELL_SCHOOLS);
                break;
            case CREATURES:
                deps.add(DataSection.CREATURE_TYPES);
                deps.add(DataSection.ALIGNMENTS);
                break;
            case ITEMS:
                deps.add(DataSection.ITEM_CATEGORIES);
                deps.add(DataSection.ITEM_RARITIES);
                deps.add(DataSection.GAME_SYSTEMS);
                break;
            case RULES:
                deps.add(DataSection.RULESETS);
                break;
            case ITEM_SETS:
                deps.add(DataSection.ITEMS);
                break;
        }
        return deps;
    }
}
