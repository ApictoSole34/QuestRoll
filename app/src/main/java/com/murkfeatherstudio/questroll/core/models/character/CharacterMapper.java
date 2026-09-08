package com.murkfeatherstudio.questroll.core.models.character;

import java.util.ArrayList;

/**
 * Utility class for mapping between Data Transfer Objects (DTOs) and Room Entities.
 * <p>
 * This mapper is primarily used during the character creation process to convert
 * {@link CharacterCreationDTO} into the various persisted entities required by
 * {@link com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase}.
 * </p>
 */
public class CharacterMapper {

    /**
     * Converts a character creation DTO to a base character entity.
     *
     * @param dto The data transfer object from the creation flow.
     * @return A new {@link CharacterEntity}.
     */
    public static CharacterEntity toEntity(CharacterCreationDTO dto) {
        CharacterEntity entity = new CharacterEntity();
        entity.name = dto.name;
        entity.gameSystem = dto.gameSystem;
        entity.alignmentKey = dto.alignmentKey;
        entity.backgroundKey = dto.backgroundKey;
        entity.speciesKey = dto.speciesKey;
        entity.totalLevel = dto.classAssignments.stream().mapToInt(c -> c.level).sum();
        entity.experience = 0;
        entity.currentHp = 0;
        entity.temporaryHp = 0;
        return entity;
    }

    /**
     * Extracts and calculates ability modifiers to create a {@link CharacterAttributesEntity}.
     *
     * @param characterId The ID of the already inserted character.
     * @param dto         The creation DTO containing raw attribute scores.
     * @return A new {@link CharacterAttributesEntity} with calculated modifiers.
     */
    public static CharacterAttributesEntity toAttributesEntity(long characterId, CharacterCreationDTO dto) {
        CharacterAttributesEntity attrs = new CharacterAttributesEntity();
        attrs.characterId = characterId;
        attrs.strength = dto.attributes.getOrDefault("STR", 10);
        attrs.dexterity = dto.attributes.getOrDefault("DEX", 10);
        attrs.constitution = dto.attributes.getOrDefault("CON", 10);
        attrs.intelligence = dto.attributes.getOrDefault("INT", 10);
        attrs.wisdom = dto.attributes.getOrDefault("WIS", 10);
        attrs.charisma = dto.attributes.getOrDefault("CHA", 10);

        attrs.strengthMod = calcMod(attrs.strength);
        attrs.dexterityMod = calcMod(attrs.dexterity);
        attrs.constitutionMod = calcMod(attrs.constitution);
        attrs.intelligenceMod = calcMod(attrs.intelligence);
        attrs.wisdomMod = calcMod(attrs.wisdom);
        attrs.charismaMod = calcMod(attrs.charisma);
        return attrs;
    }

    private static int calcMod(int score) {
        // D&D 5e modifier calculation: floor((score - 10) / 2)
        // Using Math.floorDiv handles negative results correctly for modifiers (e.g., 7 -> -2)
        return Math.floorDiv(score - 10, 2);
    }

    /**
     * Converts DTO class assignments into a list of entities.
     *
     * @param characterId The ID of the associated character.
     * @param dto         The creation DTO.
     * @return A list of {@link CharacterClassAssignmentEntity}.
     */
    public static ArrayList<CharacterClassAssignmentEntity> toClassAssignments(long characterId, CharacterCreationDTO dto) {
        ArrayList<CharacterClassAssignmentEntity> list = new ArrayList<>();
        for (CharacterCreationDTO.ClassAssignmentDTO ca : dto.classAssignments) {
            CharacterClassAssignmentEntity e = new CharacterClassAssignmentEntity();
            e.characterId = characterId;
            e.classKey = ca.classKey;
            e.level = ca.level;
            list.add(e);
        }
        return list;
    }

    /**
     * Converts DTO inventory items into a list of entities.
     *
     * @param characterId The ID of the associated character.
     * @param dto         The creation DTO.
     * @return A list of {@link InventoryItemEntity}.
     */
    public static ArrayList<InventoryItemEntity> toInventoryItems(long characterId, CharacterCreationDTO dto) {
        ArrayList<InventoryItemEntity> list = new ArrayList<>();
        if (dto.startingItems == null) return list;
        for (CharacterCreationDTO.InventoryItemDTO i : dto.startingItems) {
            InventoryItemEntity e = new InventoryItemEntity();
            e.characterId = characterId;
            e.itemKey = i.itemKey;
            e.customName = i.customName;
            e.customDescription = i.customDescription;
            e.customWeight = i.customWeight;
            e.customCost = i.customCost;
            e.quantity = i.quantity;
            e.isEquipped = i.equipped;
            e.slot = i.slot;
            list.add(e);
        }
        return list;
    }

    /**
     * Converts DTO traits into a list of entities.
     *
     * @param characterId The ID of the associated character.
     * @param dto         The creation DTO.
     * @return A list of {@link CharacterTraitEntity}.
     */
    public static ArrayList<CharacterTraitEntity> toTraits(long characterId, CharacterCreationDTO dto) {
        ArrayList<CharacterTraitEntity> list = new ArrayList<>();
        if (dto.startingTraits == null) return list;
        int order = 0;
        for (CharacterCreationDTO.TraitDTO t : dto.startingTraits) {
            CharacterTraitEntity e = new CharacterTraitEntity();
            e.characterId = characterId;
            e.sourceType = t.sourceType;
            e.sourceKey = t.sourceKey;
            e.name = t.name;
            e.description = t.description;
            e.levelRequirement = t.levelRequirement;
            e.displayOrder = order++;
            list.add(e);
        }
        return list;
    }

    /**
     * Converts a list of spell keys into preparation records.
     *
     * @param characterId The ID of the associated character.
     * @param dto         The creation DTO.
     * @return A list of {@link CharacterSpellEntity}.
     */
    public static ArrayList<CharacterSpellEntity> toSpells(long characterId, CharacterCreationDTO dto) {
        ArrayList<CharacterSpellEntity> list = new ArrayList<>();
        if (dto.startingSpellKeys == null) return list;
        for (String spellKey : dto.startingSpellKeys) {
            CharacterSpellEntity e = new CharacterSpellEntity();
            e.characterId = characterId;
            e.spellKey = spellKey;
            e.isPrepared = false;
            list.add(e);
        }
        return list;
    }
}
