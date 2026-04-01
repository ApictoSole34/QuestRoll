package com.fizzycoyote.qusetroll.feature_loading;

public enum DataSection {
    PUBLISHERS("Publishers"),
    LICENSES("Licenses"),
    DOCUMENTS("Documents"),
    GAME_SYSTEMS("Game Systems"),
    LANGUAGES("Languages"),
    ABILITIES("Abilities & Skills"),
    CLASSES("Classes"),
    SPELLS("Spells"),
    SPELL_SCHOOLS("Spell Schools"),
    CREATURES("Creatures"),
    SPECIES("Species"),
    BACKGROUNDS("Backgrounds"),
    ITEMS("Items"),
    DAMAGE_TYPES("Damage Types"),
    ALIGNMENTS("Alignments");

    public final String displayName;

    DataSection(String displayName) {
        this.displayName = displayName;
    }
}