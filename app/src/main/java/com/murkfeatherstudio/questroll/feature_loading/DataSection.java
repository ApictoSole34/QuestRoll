package com.murkfeatherstudio.questroll.feature_loading;

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
    CREATURE_TYPES("Creature Types"),
    SPECIES("Species"),
    BACKGROUNDS("Backgrounds"),
    ITEMS("Items"),
    ITEM_SETS("Item Sets"),
    ITEM_CATEGORIES("Item Categories"),
    DAMAGE_TYPES("Damage Types"),
    ALIGNMENTS("Alignments"),
    ITEM_RARITIES("Item Rarities"),
    WEAPON_PROPERTIES("Weapon Properties"),
    SERVICES("Services"),
    ENVIRONMENTS("Environments"),
    RULES("Rules"),
    RULESETS("Rule Sets"),
    CONDITIONS("Conditions");

    public final String displayName;

    DataSection(String displayName) {
        this.displayName = displayName;
    }
}