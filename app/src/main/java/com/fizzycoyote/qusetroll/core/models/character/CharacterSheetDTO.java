package com.fizzycoyote.qusetroll.core.models.character;

import com.fizzycoyote.qusetroll.core.models.character.*;
import java.util.List;

public class CharacterSheetDTO {
    public CharacterEntity character;
    public CharacterAttributesEntity attributes;
    public List<CharacterClassAssignmentEntity> classes;
    public List<InventoryItemEntity> inventory;
    public List<CharacterTraitEntity> traits;
    public List<CharacterSpellEntity> spells;

    public int proficiencyBonus;
    public int maxHp;
    public int armorClass;
}