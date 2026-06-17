package com.fizzycoyote.qusetroll.core.models.character;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class CharacterWithRelations {
    @Embedded
    public CharacterEntity character;

    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterAttributesEntity.class)
    public CharacterAttributesEntity attributes;

    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterClassAssignmentEntity.class)
    public List<CharacterClassAssignmentEntity> classAssignments;

    @Relation(parentColumn = "id", entityColumn = "character_id", entity = InventoryItemEntity.class)
    public List<InventoryItemEntity> inventory;

    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterTraitEntity.class)
    public List<CharacterTraitEntity> traits;

    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterSpellEntity.class)
    public List<CharacterSpellEntity> spells;

    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterLanguageEntity.class)
    public List<CharacterLanguageEntity> languages;

    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterSkillProficiencyEntity.class)
    public List<CharacterSkillProficiencyEntity> skillProficiencies;

    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterSavingThrowEntity.class)
    public List<CharacterSavingThrowEntity> savingThrows;

    @Relation(parentColumn = "id", entityColumn = "characterId", entity = CharacterSubclassAssignmentEntity.class)
    public List<CharacterSubclassAssignmentEntity> subclassAssignments;
}