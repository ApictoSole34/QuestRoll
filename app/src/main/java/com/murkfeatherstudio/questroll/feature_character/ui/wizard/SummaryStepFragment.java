package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterAttributesEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterCreationDTO;
import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterLanguageEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterMapper;
import com.murkfeatherstudio.questroll.core.models.character.CharacterSavingThrowEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterSkillProficiencyEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterSpellEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardSummaryBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.noties.markwon.Markwon;

/**
 * Fragment that displays a final summary of the character before saving it to the database.
 * Uses Markwon to render Markdown-formatted text for better readability and style.
 */
public class SummaryStepFragment extends Fragment {
    private WizardViewModel viewModel;
    private FragmentWizardSummaryBinding binding;
    private PlayerCharacterDatabase pcDb;
    private Open5eDatabase open5eDb;
    private Markwon markwon;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        pcDb = PlayerCharacterDatabase.getInstance(requireContext());
        open5eDb = Open5eDatabase.getInstance(requireContext());
        markwon = Markwon.create(requireContext());

        binding.summaryText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.summaryText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        displaySummary();
        displayImages();

        binding.saveButton.setOnClickListener(v -> saveCharacter());
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void displaySummary() {
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();

            // 1. Fetch pretty race name
            String raceName = viewModel.speciesKey;
            SpeciesEntity se = open5eDb.speciesDao().getByKeySync(viewModel.speciesKey);
            if (se != null) raceName = se.name;

            sb.append("### Basic Information\n");
            sb.append("**Name:** ").append(viewModel.characterName).append("\n");
            sb.append("**Race:** ").append(raceName).append("\n");
            sb.append("**Background:** ").append(viewModel.backgroundKey).append("\n");
            sb.append("**Alignment:** ").append(viewModel.alignmentKey).append("\n\n");

            sb.append("### Classes\n");
            for (WizardViewModel.ClassAssignment ca : viewModel.classAssignments) {
                sb.append("- ").append(ca.className).append(" (Level ").append(ca.level).append(")\n");
            }
            sb.append("\n");

            sb.append("### Attributes\n");
            String[] names = {"STR","DEX","CON","INT","WIS","CHA"};
            for (int i = 0; i < names.length; i++) {
                int val = viewModel.attributes.get(i);
                int mod = com.murkfeatherstudio.questroll.feature_character.engine.CharacterEngine.getAbilityModifier(val);
                sb.append("**").append(names[i]).append(":** ").append(val)
                  .append(" (").append(mod >= 0 ? "+" + mod : mod).append(")  ");
            }
            sb.append("\n\n");

            // Traits
            sb.append("### Traits\n");
            if (viewModel.characterTraits.isEmpty()) {
                sb.append("*None*\n");
            } else {
                for (CharacterTraitEntity trait : viewModel.characterTraits) {
                    sb.append("- **").append(trait.name).append("**\n");
                }
            }
            sb.append("\n");

            // Inventory
            sb.append("### Equipment\n");
            if (viewModel.backgroundCustomItems.isEmpty() && !viewModel.useClassEquipment && viewModel.classStartingGold <= 0) {
                sb.append("*None*\n");
            } else {
                for (CharacterCreationDTO.InventoryItemDTO item : viewModel.backgroundCustomItems) {
                    sb.append("- ").append(item.customName).append(" (x").append(item.quantity).append(")\n");
                }
                if (viewModel.useClassEquipment) {
                    for (CharacterCreationDTO.InventoryItemDTO item : viewModel.classEquipment) {
                        sb.append("- ").append(item.customName).append(" (x").append(item.quantity).append(")\n");
                    }
                } else if (viewModel.classStartingGold > 0) {
                    sb.append("- Starting Gold: ").append(viewModel.classStartingGold).append(" gp\n");
                }
            }

            final String finalMarkdown = sb.toString();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        markwon.setMarkdown(binding.summaryText, finalMarkdown);
                    }
                });
            }
        }).start();
    }

    private int calculateMaxHp(String classKey, int totalLevel, int conMod) {
        try {
            CharacterClassEntity classEntity = open5eDb.characterClassDao().getClassByKeySync(classKey);
            if (classEntity == null || classEntity.hitDice == null) return 10 + conMod * totalLevel;

            int diceSides = com.murkfeatherstudio.questroll.feature_character.engine.CharacterEngine.parseHitDie(classEntity.hitDice);
            int firstLevelHp = diceSides + conMod;
            int additionalLevels = totalLevel - 1;
            int averageRoll = (diceSides / 2) + 1;
            int hp = firstLevelHp + additionalLevels * (averageRoll + conMod);
            return Math.max(1, hp);
        } catch (Exception e) {
            return 10 + conMod * totalLevel;
        }
    }

    private void saveTraits(long characterId) {
        if (viewModel.characterTraits.isEmpty()) return;
        List<CharacterTraitEntity> entities = new ArrayList<>();
        for (CharacterTraitEntity t : viewModel.characterTraits) {
            CharacterTraitEntity copy = new CharacterTraitEntity();
            copy.characterId = characterId;
            copy.sourceType = t.sourceType;
            copy.sourceKey = t.sourceKey;
            copy.name = t.name;
            copy.description = t.description;
            copy.levelRequirement = t.levelRequirement;
            copy.displayOrder = t.displayOrder;
            entities.add(copy);
        }
        pcDb.traitDao().insertAll(entities);
    }

    private void saveSpells(long characterId) {
        List<CharacterSpellEntity> entities = new ArrayList<>();
        for (String spellKey : viewModel.chosenCantripKeys) {
            CharacterSpellEntity e = new CharacterSpellEntity();
            e.characterId = characterId;
            e.spellKey = spellKey;
            e.isPrepared = false;
            entities.add(e);
        }
        for (String spellKey : viewModel.chosenSpellKeys) {
            CharacterSpellEntity e = new CharacterSpellEntity();
            e.characterId = characterId;
            e.spellKey = spellKey;
            e.isPrepared = viewModel.isPreparedCaster;
            entities.add(e);
        }
        if (!entities.isEmpty()) {
            pcDb.spellDao().insertAll(entities);
        }
    }

    private void displayImages() {
        if (binding == null) return;
        if (viewModel.characterThumbnailPath != null && !viewModel.characterThumbnailPath.isEmpty()) {
            binding.thumbnailPreview.setImageURI(Uri.parse(viewModel.characterThumbnailPath));
        } else {
            binding.thumbnailPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        if (viewModel.characterImagePath != null && !viewModel.characterImagePath.isEmpty()) {
            binding.fullImagePreview.setImageURI(Uri.parse(viewModel.characterImagePath));
        } else {
            binding.fullImagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void saveSavingThrows(long characterId, Set<String> proficientAbilities) {
        if (proficientAbilities == null || proficientAbilities.isEmpty()) return;
        List<CharacterSavingThrowEntity> list = new ArrayList<>();
        for (String ability : proficientAbilities) {
            CharacterSavingThrowEntity st = new CharacterSavingThrowEntity();
            st.characterId = characterId;
            st.abilityKey = ability;
            st.isProficient = true;
            list.add(st);
        }
        pcDb.characterSavingThrowDao().insertAll(list);
    }

    private void saveCharacter() {
        CharacterCreationDTO dto = new CharacterCreationDTO();
        dto.name = viewModel.characterName;
        dto.gameSystem = viewModel.gameSystem;
        dto.alignmentKey = viewModel.alignmentKey;
        dto.backgroundKey = viewModel.backgroundKey;
        dto.speciesKey = viewModel.speciesKey;

        Map<String, Integer> attrMap = new HashMap<>();
        String[] keys = {"STR","DEX","CON","INT","WIS","CHA"};
        for (int i = 0; i < keys.length; i++) {
            attrMap.put(keys[i], viewModel.attributes.get(i));
        }
        dto.attributes = attrMap;

        List<CharacterCreationDTO.ClassAssignmentDTO> classDTOs = new ArrayList<>();
        for (WizardViewModel.ClassAssignment ca : viewModel.classAssignments) {
            CharacterCreationDTO.ClassAssignmentDTO cdto = new CharacterCreationDTO.ClassAssignmentDTO();
            cdto.classKey = ca.classKey;
            cdto.level = ca.level;
            classDTOs.add(cdto);
        }
        dto.classAssignments = classDTOs;

        List<CharacterCreationDTO.InventoryItemDTO> finalEquipment = new ArrayList<>();
        finalEquipment.addAll(viewModel.backgroundCustomItems);
        if (viewModel.useClassEquipment) {
            finalEquipment.addAll(viewModel.classEquipment);
        } else {
            CharacterCreationDTO.InventoryItemDTO classGoldItem = new CharacterCreationDTO.InventoryItemDTO();
            classGoldItem.customName = "Starting Gold";
            classGoldItem.quantity = (int) viewModel.classStartingGold;
            classGoldItem.customWeight = 0;
            finalEquipment.add(classGoldItem);
        }
        dto.startingItems = finalEquipment;
        dto.startingTraits = new ArrayList<>();
        dto.startingSpellKeys = new ArrayList<>();

        final int totalLevel = classDTOs.stream().mapToInt(c -> c.level).sum();
        String firstClassKey = classDTOs.isEmpty() ? null : classDTOs.get(0).classKey;
        int conMod = com.murkfeatherstudio.questroll.feature_character.engine.CharacterEngine.getAbilityModifier(dto.attributes.getOrDefault("CON", 10));
        
        final int maxHp = (firstClassKey != null) ? calculateMaxHp(firstClassKey, totalLevel, conMod) : 10 + conMod * totalLevel;

        final Set<String> savingThrows = viewModel.selectedSavingThrows != null
                ? new HashSet<>(viewModel.selectedSavingThrows) : new HashSet<>();

        viewModel.executor.execute(() -> {
            try {
                if (viewModel.isEditMode()) {
                    long charId = viewModel.getEditingCharacterId();
                    CharacterEntity existing = pcDb.characterDao().getCharacterSync(charId);
                    if (existing != null) {
                        existing.name = dto.name;
                        existing.gameSystem = dto.gameSystem;
                        existing.alignmentKey = dto.alignmentKey;
                        existing.backgroundKey = dto.backgroundKey;
                        existing.speciesKey = dto.speciesKey;
                        existing.totalLevel = totalLevel;
                        existing.imagePath = viewModel.characterImagePath;
                        existing.thumbnailPath = viewModel.characterThumbnailPath;
                        pcDb.characterDao().update(existing);

                        CharacterAttributesEntity attrs = CharacterMapper.toAttributesEntity(charId, dto);
                        pcDb.characterAttributesDao().update(attrs);

                        pcDb.classAssignmentDao().deleteForCharacter(charId);
                        pcDb.classAssignmentDao().insertAll(CharacterMapper.toClassAssignments(charId, dto));

                        pcDb.characterSavingThrowDao().deleteForCharacter(charId);
                        saveSavingThrows(charId, savingThrows);

                        saveInventory(charId, dto.startingItems);
                        pcDb.languageDao().deleteForCharacter(charId);
                        saveSkillProficiencies(charId);
                        saveLanguages(charId);
                        
                        pcDb.traitDao().deleteForCharacter(charId);
                        saveTraits(charId);
                        
                        pcDb.spellDao().deleteForCharacter(charId);
                        saveSpells(charId);
                    }
                } else {
                    CharacterEntity character = CharacterMapper.toEntity(dto);
                    character.imagePath = viewModel.characterImagePath;
                    character.thumbnailPath = viewModel.characterThumbnailPath;
                    character.maxHp = maxHp;
                    character.currentHp = maxHp;
                    long charId = pcDb.characterDao().insert(character);

                    pcDb.characterAttributesDao().insert(CharacterMapper.toAttributesEntity(charId, dto));
                    pcDb.classAssignmentDao().insertAll(CharacterMapper.toClassAssignments(charId, dto));

                    saveSavingThrows(charId, savingThrows);
                    saveInventory(charId, dto.startingItems);
                    saveLanguages(charId);
                    saveSkillProficiencies(charId);
                    saveTraits(charId);
                    saveSpells(charId);
                }

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Character saved!", Toast.LENGTH_SHORT).show();
                        requireActivity().finish();
                    });
                }
            } catch (Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Save error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void saveLanguages(long characterId) {
        List<CharacterLanguageEntity> entities = new ArrayList<>();
        for (String lang : viewModel.racialFixedLanguages) entities.add(createLanguageEntity(characterId, lang, false));
        for (String lang : viewModel.backgroundFixedLanguages) entities.add(createLanguageEntity(characterId, lang, false));
        for (String langKey : viewModel.chosenBonusLanguages) entities.add(createLanguageEntity(characterId, langKey, false));
        for (String langKey : viewModel.classSecretLanguages) entities.add(createLanguageEntity(characterId, langKey, true));
        
        Map<String, CharacterLanguageEntity> deduped = new LinkedHashMap<>();
        for (CharacterLanguageEntity e : entities) deduped.putIfAbsent(e.languageKey, e);
        if (!deduped.isEmpty()) pcDb.languageDao().insertAll(new ArrayList<>(deduped.values()));
    }

    private void saveSkillProficiencies(long characterId) {
        List<CharacterSkillProficiencyEntity> entities = new ArrayList<>();
        for (String skillName : viewModel.backgroundSkillProficiencies) entities.add(createSkillEntity(characterId, skillName, "BACKGROUND"));
        for (String skillKey : viewModel.chosenSkillProficiencies) entities.add(createSkillEntity(characterId, skillKey, "CLASS"));
        if (!entities.isEmpty()) pcDb.characterSkillProficiencyDao().insertAll(entities);
    }

    private CharacterSkillProficiencyEntity createSkillEntity(long characterId, String skillKey, String source) {
        CharacterSkillProficiencyEntity e = new CharacterSkillProficiencyEntity();
        e.characterId = characterId;
        e.skillKey = skillKey;
        e.source = source;
        return e;
    }

    private CharacterLanguageEntity createLanguageEntity(long characterId, String languageKey, boolean isSecret) {
        CharacterLanguageEntity entity = new CharacterLanguageEntity();
        entity.characterId = characterId;
        entity.languageKey = languageKey;
        entity.isSecret = isSecret;
        return entity;
    }

    private void saveInventory(long characterId, List<CharacterCreationDTO.InventoryItemDTO> items) {
        pcDb.inventoryItemDao().deleteForCharacter(characterId);
        for (CharacterCreationDTO.InventoryItemDTO dtoItem : items) {
            InventoryItemEntity entity = new InventoryItemEntity();
            entity.characterId = characterId;
            entity.itemKey = dtoItem.itemKey;
            entity.customName = dtoItem.customName;
            entity.customDescription = dtoItem.customDescription;
            entity.customWeight = dtoItem.customWeight;
            entity.customCost = dtoItem.customCost;
            entity.quantity = dtoItem.quantity;
            entity.isEquipped = dtoItem.equipped;
            entity.slot = dtoItem.slot;
            pcDb.inventoryItemDao().insert(entity);
        }
    }
}
