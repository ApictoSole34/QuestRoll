package com.fizzycoyote.qusetroll.feature_character.ui.sheet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterLanguageEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterSpellEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.character.InventoryItemEntity;
import com.fizzycoyote.qusetroll.feature_character.ui.wizard.CharacterWizardActivity;
import com.fizzycoyote.qusetroll.feature_character.view_model.CharacterSheetViewModel;

import java.io.File;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CharacterSheetFragment extends Fragment {

    private CharacterSheetViewModel viewModel;
    private TextView nameView, speciesView, classesView, alignmentView, backgroundView;
    private TextView hpView, acView, initiativeView, proficiencyView;
    private LinearLayout attributesContainer, skillsContainer;
    private Button btnEdit, btnDelete;
    private ImageView fullImageView;
    private LinearLayout languagesContainer, traitsContainer, inventoryContainer, spellsContainer;
    private long characterId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        characterId = getArguments().getLong("character_id", -1);
        if (characterId == -1) {
            Toast.makeText(getContext(), "Missing character ID", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        viewModel = new ViewModelProvider(this).get(CharacterSheetViewModel.class);
        viewModel.init(characterId, requireContext());

        nameView = view.findViewById(R.id.character_name);
        speciesView = view.findViewById(R.id.character_species);
        classesView = view.findViewById(R.id.character_classes);
        alignmentView = view.findViewById(R.id.character_alignment);
        backgroundView = view.findViewById(R.id.character_background);
        hpView = view.findViewById(R.id.hp_text);
        acView = view.findViewById(R.id.ac_text);
        initiativeView = view.findViewById(R.id.initiative_text);
        proficiencyView = view.findViewById(R.id.proficiency_text);
        attributesContainer = view.findViewById(R.id.attributes_container);
        skillsContainer = view.findViewById(R.id.skills_container);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnDelete = view.findViewById(R.id.btn_delete);
        fullImageView = view.findViewById(R.id.character_full_image);
        languagesContainer = view.findViewById(R.id.languages_container);
        traitsContainer = view.findViewById(R.id.traits_container);
        inventoryContainer = view.findViewById(R.id.inventory_container);
        spellsContainer = view.findViewById(R.id.spells_container);

        // Observers
        viewModel.getCharacter().observe(getViewLifecycleOwner(), character -> {
            if (character != null) nameView.setText(character.name);
        });
        viewModel.getSpeciesName().observe(getViewLifecycleOwner(), name -> speciesView.setText("Race: " + name));
        viewModel.getAlignmentName().observe(getViewLifecycleOwner(), name -> alignmentView.setText("Alignment: " + name));
        viewModel.getBackgroundName().observe(getViewLifecycleOwner(), name -> backgroundView.setText("Background: " + name));
        viewModel.getClassNames().observe(getViewLifecycleOwner(), names -> classesView.setText("Classes: " + String.join(", ", names)));

        viewModel.getCurrentHp().observe(getViewLifecycleOwner(), hp -> updateHpDisplay(hp, viewModel.getMaxHp().getValue()));
        viewModel.getMaxHp().observe(getViewLifecycleOwner(), max -> updateHpDisplay(viewModel.getCurrentHp().getValue(), max));
        viewModel.getArmorClass().observe(getViewLifecycleOwner(), ac -> acView.setText("AC: " + ac));
        viewModel.getInitiative().observe(getViewLifecycleOwner(), init -> initiativeView.setText("Initiative: " + init));
        viewModel.getProficiencyBonus().observe(getViewLifecycleOwner(), prof -> proficiencyView.setText("Prof. bonus: +" + prof));

        viewModel.getAttributes().observe(getViewLifecycleOwner(), this::displayAttributes);
        viewModel.getSkillBonuses().observe(getViewLifecycleOwner(), this::displaySkills);
        viewModel.getFullImagePath().observe(getViewLifecycleOwner(), this::displayFullImage);
        viewModel.getLanguages().observe(getViewLifecycleOwner(), this::displayLanguages);
        viewModel.getTraits().observe(getViewLifecycleOwner(), this::displayTraits);
        viewModel.getInventory().observe(getViewLifecycleOwner(), this::displayInventory);
        viewModel.getSpells().observe(getViewLifecycleOwner(), this::displaySpells);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CharacterWizardActivity.class);
            intent.putExtra("character_id", characterId);
            intent.putExtra("edit_mode", true);
            startActivity(intent);
            requireActivity().finish();
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete character")
                    .setMessage("Are you sure you want to delete this character?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        new Thread(() -> {
                            PlayerCharacterDatabase.getInstance(requireContext())
                                    .characterDao().deleteCharacter(characterId);
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Character deleted", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(view).navigateUp();
                            });
                        }).start();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void updateHpDisplay(Integer current, Integer max) {
        if (current != null && max != null) hpView.setText("HP: " + current + " / " + max);
    }

    private void displayAttributes(Map<String, Integer> attrs) {
        attributesContainer.removeAllViews();
        String[] order = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (String key : order) {
            Integer val = attrs.get(key);
            if (val == null) val = 10;
            int mod = (val - 10) / 2;
            TextView tv = new TextView(getContext());
            tv.setText(key + ": " + val + " (" + (mod >= 0 ? "+" + mod : String.valueOf(mod)) + ")");
            attributesContainer.addView(tv);
        }
    }

    private void displaySkills(List<CharacterSheetViewModel.SkillDisplay> skills) {
        skillsContainer.removeAllViews();
        for (CharacterSheetViewModel.SkillDisplay sd : skills) {
            TextView tv = new TextView(getContext());
            tv.setText(sd.name + ": " + (sd.bonus >= 0 ? "+" + sd.bonus : String.valueOf(sd.bonus)));
            skillsContainer.addView(tv);
        }
    }

    private void displayFullImage(String path) {
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists()) fullImageView.setImageURI(Uri.fromFile(file));
            else fullImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        } else {
            fullImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void displayLanguages(List<CharacterLanguageEntity> languages) {
        languagesContainer.removeAllViews();
        if (languages == null || languages.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("None");
            languagesContainer.addView(empty);
            return;
        }
        for (CharacterLanguageEntity lang : languages) {
            TextView tv = new TextView(getContext());
            tv.setText("• " + lang.languageKey);
            tv.setPadding(32, 4, 0, 4);
            languagesContainer.addView(tv);
        }
    }

    private void displayTraits(List<CharacterTraitEntity> traits) {
        traitsContainer.removeAllViews();
        if (traits == null || traits.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("None");
            traitsContainer.addView(empty);
            return;
        }
        for (CharacterTraitEntity t : traits) {
            TextView tv = new TextView(getContext());
            tv.setText("• " + t.name);
            tv.setPadding(32, 4, 0, 4);
            traitsContainer.addView(tv);
        }
    }

    private void displayInventory(List<InventoryItemEntity> inventory) {
        inventoryContainer.removeAllViews();
        if (inventory == null || inventory.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("None");
            inventoryContainer.addView(empty);
            return;
        }
        for (InventoryItemEntity item : inventory) {
            TextView tv = new TextView(getContext());
            String name = (item.customName != null) ? item.customName : item.itemKey;
            tv.setText("• " + name + " (x" + item.quantity + ")");
            tv.setPadding(32, 4, 0, 4);
            inventoryContainer.addView(tv);
        }
    }

    private void displaySpells(List<CharacterSpellEntity> spells) {
        spellsContainer.removeAllViews();
        if (spells == null || spells.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("None");
            spellsContainer.addView(empty);
            return;
        }
        for (CharacterSpellEntity spell : spells) {
            TextView tv = new TextView(getContext());
            tv.setText("• " + spell.spellKey);
            tv.setPadding(32, 4, 0, 4);
            spellsContainer.addView(tv);
        }
    }
}