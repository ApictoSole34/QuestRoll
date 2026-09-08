package com.murkfeatherstudio.questroll.core.models.character;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * A POJO used by Room to fetch a {@link CharacterEntity} along with all its associated
 * data in a single database transaction.
 * <p>
 * This class represents the full state of a player character, including their attributes,
 * class assignments (multiclassing), inventory, traits, known spells, languages,
 * and proficiencies.
 * </p>
 */
public class CharacterWithRelations {
    /**
     * The base character record.
     */
    @Embedded
    public CharacterEntity character;

    /**
     * The character's ability scores and modifiers.
     */
    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterAttributesEntity.class)
    public CharacterAttributesEntity attributes;

    /**
     * List of class assignments, defining the character's levels in various classes.
     */
    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterClassAssignmentEntity.class)
    public List<CharacterClassAssignmentEntity> classAssignments;

    /**
     * The character's inventory items.
     */
    @Relation(parentColumn = "id", entityColumn = "character_id", entity = InventoryItemEntity.class)
    public List<InventoryItemEntity> inventory;

    /**
     * Racial, class, or background traits.
     */
    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterTraitEntity.class)
    public List<CharacterTraitEntity> traits;

    /**
     * Spells known or prepared by the character.
     */
    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterSpellEntity.class)
    public List<CharacterSpellEntity> spells;

    /**
     * Languages known by the character.
     */
    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterLanguageEntity.class)
    public List<CharacterLanguageEntity> languages;

    /**
     * Skill proficiencies.
     */
    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterSkillProficiencyEntity.class)
    public List<CharacterSkillProficiencyEntity> skillProficiencies;

    /**
     * Saving throw proficiencies.
     */
    @Relation(parentColumn = "id", entityColumn = "character_id", entity = CharacterSavingThrowEntity.class)
    public List<CharacterSavingThrowEntity> savingThrows;

    /**
     * Subclass assignments for each of the character's classes.
     */
    @Relation(parentColumn = "id", entityColumn = "characterId", entity = CharacterSubclassAssignmentEntity.class)
    public List<CharacterSubclassAssignmentEntity> subclassAssignments;
}
