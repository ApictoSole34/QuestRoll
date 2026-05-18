package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.*;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;
import java.util.*;

import de.hdodenhof.circleimageview.CircleImageView;

public class SummaryStepFragment extends Fragment {
    private WizardViewModel viewModel;
    private TextView summaryText;
    private CircleImageView thumbnailPreview;
    private ImageView fullImagePreview;
    private PlayerCharacterDatabase pcDb;
    private Open5eDatabase open5eDb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_summary, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        pcDb = PlayerCharacterDatabase.getInstance(requireContext());
        open5eDb = Open5eDatabase.getInstance(requireContext());

        summaryText = view.findViewById(R.id.summary_text);
        thumbnailPreview = view.findViewById(R.id.thumbnail_preview);
        fullImagePreview = view.findViewById(R.id.full_image_preview);
        Button saveButton = view.findViewById(R.id.save_button);
        Button backButton = view.findViewById(R.id.back_button);

        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        displaySummary();
        displayImages();

        saveButton.setOnClickListener(v -> saveCharacter());
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void displaySummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("System: ").append(viewModel.gameSystem).append("\n");
        sb.append("Name: ").append(viewModel.characterName).append("\n");
        sb.append("Race: ").append(viewModel.speciesKey).append("\n");
        sb.append("Background: ").append(viewModel.backgroundKey).append("\n");
        sb.append("Alignment: ").append(viewModel.alignmentKey).append("\n");
        sb.append("Classes: ");
        for (WizardViewModel.ClassAssignment ca : viewModel.classAssignments) {
            sb.append(ca.className).append(" lvl ").append(ca.level).append(", ");
        }
        sb.append("\nAttributes: ");
        String[] names = {"STR","DEX","CON","INT","WIS","CHA"};
        for (int i = 0; i < names.length; i++) {
            sb.append(names[i]).append("=").append(viewModel.attributes.get(i)).append(" ");
        }

        // Languages
        sb.append("\n\nKnown languages (fixed): ");
        Set<String> allFixed = new LinkedHashSet<>();
        allFixed.addAll(viewModel.racialFixedLanguages);
        allFixed.addAll(viewModel.backgroundFixedLanguages);
        allFixed.addAll(viewModel.classSecretLanguages);
        sb.append(allFixed.isEmpty() ? "none" : String.join(", ", allFixed));
        if (!viewModel.chosenBonusLanguages.isEmpty()) {
            sb.append("\nAdditional chosen languages: ").append(String.join(", ", viewModel.chosenBonusLanguages));
        }

        // Starting equipment
        sb.append("\n\nStarting equipment:");
        List<String> equipmentStrings = new ArrayList<>();
        for (CharacterCreationDTO.InventoryItemDTO item : viewModel.backgroundCustomItems) {
            equipmentStrings.add(item.customName + " x" + item.quantity);
        }
        if (viewModel.backgroundGold > 0) {
            equipmentStrings.add("Background gold x" + viewModel.backgroundGold);
        }
        if (viewModel.useClassEquipment) {
            for (CharacterCreationDTO.InventoryItemDTO item : viewModel.classEquipment) {
                equipmentStrings.add(item.customName + " x" + item.quantity);
            }
        } else if (viewModel.classStartingGold > 0) {
            equipmentStrings.add("Class starting gold x" + viewModel.classStartingGold);
        }
        if (equipmentStrings.isEmpty()) {
            sb.append(" none");
        } else {
            for (String eq : equipmentStrings) {
                sb.append("\n- ").append(eq);
            }
        }

        // Traits
        sb.append("\n\nSpecial traits:");
        if (viewModel.characterTraits.isEmpty()) {
            sb.append(" none");
        } else {
            for (CharacterTraitEntity trait : viewModel.characterTraits) {
                sb.append("\n- ").append(trait.name);
            }
        }

        summaryText.setText(sb.toString());
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
        if (viewModel.characterThumbnailPath != null && !viewModel.characterThumbnailPath.isEmpty()) {
            thumbnailPreview.setImageURI(Uri.parse(viewModel.characterThumbnailPath));
        } else {
            thumbnailPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        if (viewModel.characterImagePath != null && !viewModel.characterImagePath.isEmpty()) {
            fullImagePreview.setImageURI(Uri.parse(viewModel.characterImagePath));
        } else {
            fullImagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }
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
        if (viewModel.backgroundGold > 0) {
            CharacterCreationDTO.InventoryItemDTO goldItem = new CharacterCreationDTO.InventoryItemDTO();
            goldItem.customName = "Background gold";
            goldItem.quantity = viewModel.backgroundGold;
            goldItem.customWeight = 0;
            finalEquipment.add(goldItem);
        }
        if (viewModel.useClassEquipment) {
            finalEquipment.addAll(viewModel.classEquipment);
        } else {
            CharacterCreationDTO.InventoryItemDTO classGoldItem = new CharacterCreationDTO.InventoryItemDTO();
            classGoldItem.customName = "Class starting gold";
            classGoldItem.quantity = viewModel.classStartingGold;
            classGoldItem.customWeight = 0;
            finalEquipment.add(classGoldItem);
        }
        dto.startingItems = finalEquipment;
        dto.startingTraits = new ArrayList<>();
        dto.startingSpellKeys = new ArrayList<>();

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
                        existing.totalLevel = dto.classAssignments.stream()
                                .mapToInt(c -> c.level).sum();
                        existing.imagePath = viewModel.characterImagePath;
                        existing.thumbnailPath = viewModel.characterThumbnailPath;
                        pcDb.characterDao().update(existing);

                        CharacterAttributesEntity attrs =
                                CharacterMapper.toAttributesEntity(charId, dto);
                        pcDb.attributesDao().update(attrs);

                        pcDb.classAssignmentDao().deleteForCharacter(charId);
                        List<CharacterClassAssignmentEntity> classAssigns =
                                CharacterMapper.toClassAssignments(charId, dto);
                        pcDb.classAssignmentDao().insertAll(classAssigns);

                        saveInventory(charId, dto.startingItems);
                        pcDb.languageDao().deleteForCharacter(charId);
                        saveSkillProficiencies(charId);
                        saveLanguages(charId);
                        saveTraits(charId);
                        saveSpells(charId);
                    }
                } else {
                    CharacterEntity character = CharacterMapper.toEntity(dto);
                    character.imagePath = viewModel.characterImagePath;
                    character.thumbnailPath = viewModel.characterThumbnailPath;
                    long charId = pcDb.characterDao().insert(character);

                    CharacterAttributesEntity attrs =
                            CharacterMapper.toAttributesEntity(charId, dto);
                    pcDb.attributesDao().insert(attrs);

                    List<CharacterClassAssignmentEntity> classAssigns =
                            CharacterMapper.toClassAssignments(charId, dto);
                    pcDb.classAssignmentDao().insertAll(classAssigns);

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
                e.printStackTrace();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "Save error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void saveLanguages(long characterId) {
        List<CharacterLanguageEntity> entities = new ArrayList<>();

        for (String lang : viewModel.racialFixedLanguages) {
            entities.add(createLanguageEntity(characterId, lang, false));
        }
        for (String lang : viewModel.backgroundFixedLanguages) {
            entities.add(createLanguageEntity(characterId, lang, false));
        }
        for (String langKey : viewModel.chosenBonusLanguages) {
            entities.add(createLanguageEntity(characterId, langKey, false));
        }
        for (String langKey : viewModel.classSecretLanguages) {
            entities.add(createLanguageEntity(characterId, langKey, true));
        }

        Map<String, CharacterLanguageEntity> deduped = new LinkedHashMap<>();
        for (CharacterLanguageEntity e : entities) {
            deduped.putIfAbsent(e.languageKey, e);
        }

        if (!deduped.isEmpty()) {
            pcDb.languageDao().insertAll(new ArrayList<>(deduped.values()));
        }
    }

    private void saveSkillProficiencies(long characterId) {
        List<CharacterSkillProficiencyEntity> entities = new ArrayList<>();
        for (String skillName : viewModel.backgroundSkillProficiencies) {
            entities.add(createSkillEntity(characterId, skillName, "BACKGROUND"));
        }
        for (String skillKey : viewModel.chosenSkillProficiencies) {
            entities.add(createSkillEntity(characterId, skillKey, "CLASS"));
        }
        if (!entities.isEmpty()) {
            pcDb.characterSkillProficiencyDao().insertAll(entities);
        }
    }

    private CharacterSkillProficiencyEntity createSkillEntity(long characterId, String skillKey, String source) {
        CharacterSkillProficiencyEntity e = new CharacterSkillProficiencyEntity();
        e.characterId = characterId;
        e.skillKey = skillKey;
        e.source = source;
        return e;
    }

    private CharacterLanguageEntity createLanguageEntity(long characterId, String languageKey,
                                                         boolean isSecret) {
        CharacterLanguageEntity entity = new CharacterLanguageEntity();
        entity.characterId = characterId;
        entity.languageKey = languageKey;
        entity.isSecret = isSecret;
        return entity;
    }

    private void saveInventory(long characterId,
                               List<CharacterCreationDTO.InventoryItemDTO> items) {
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