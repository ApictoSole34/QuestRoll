package com.murkfeatherstudio.questroll.feature_character.ui.sheet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentCharacterSheetBinding;
import com.murkfeatherstudio.questroll.feature_character.engine.CharacterEngine;
import com.murkfeatherstudio.questroll.feature_character.view_model.CharacterSheetViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.noties.markwon.Markwon;

/**
 * Fragment that displays a standalone character sheet with all core D&D 5e mechanics.
 * Optimized for viewing, printing, and managing character traits and inventory.
 */
public class CharacterSheetFragment extends Fragment {

    private CharacterSheetViewModel viewModel;
    private FragmentCharacterSheetBinding binding;
    private Markwon markwon;
    private long characterId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCharacterSheetBinding.inflate(inflater, container, false);
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
        characterId = getArguments() != null ? getArguments().getLong("character_id", -1) : -1;

        markwon = Markwon.create(requireContext());
        viewModel = new ViewModelProvider(this).get(CharacterSheetViewModel.class);
        viewModel.init(characterId, requireContext());

        setupObservers();

        binding.btnExportPdf.setOnClickListener(v -> exportToPdf());
        binding.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Character")
                    .setMessage("Are you sure? This cannot be undone.")
                    .setPositiveButton("Delete", (d, w) -> {
                        viewModel.deleteCharacter();
                        requireActivity().onBackPressed();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupObservers() {
        viewModel.getCharacter().observe(getViewLifecycleOwner(), c -> { 
            if (c != null && binding != null) binding.characterName.setText(c.name); 
        });
        viewModel.getSpeciesName().observe(getViewLifecycleOwner(), name -> {
            if (binding != null) binding.characterSpecies.setText("Race: " + name);
        });
        viewModel.getClassNames().observe(getViewLifecycleOwner(), names -> {
            if (binding != null) binding.characterClasses.setText("Classes: " + String.join(", ", names));
        });
        
        viewModel.getCurrentHp().observe(getViewLifecycleOwner(), hp -> updateHpDisplay());
        viewModel.getMaxHp().observe(getViewLifecycleOwner(), max -> updateHpDisplay());
        
        viewModel.getArmorClass().observe(getViewLifecycleOwner(), ac -> {
            if (binding != null) binding.acText.setText(String.valueOf(ac));
        });
        viewModel.getInitiative().observe(getViewLifecycleOwner(), in -> {
            if (binding != null) binding.initiativeText.setText((in >= 0 ? "+" : "") + in);
        });
        
        // Complex data observers
        viewModel.getAttributes().observe(getViewLifecycleOwner(), attrs -> {
            displayAttributes(attrs);
            // Refresh dependent sections when attributes are loaded to avoid null pointer issues
            displaySavingThrows(viewModel.getProficientSavingThrows().getValue());
            displaySkills(viewModel.getSkillBonuses().getValue());
        });
        
        viewModel.getProficientSavingThrows().observe(getViewLifecycleOwner(), this::displaySavingThrows);
        viewModel.getSkillBonuses().observe(getViewLifecycleOwner(), this::displaySkills);
        viewModel.getTraits().observe(getViewLifecycleOwner(), this::displayTraits);
        viewModel.getInventory().observe(getViewLifecycleOwner(), this::displayInventory);

        viewModel.getFullImagePath().observe(getViewLifecycleOwner(), path -> {
            if (binding == null) return;
            if (path != null && !path.isEmpty() && new File(path).exists()) {
                Glide.with(this).load(new File(path)).into(binding.characterFullImage);
            } else {
                Glide.with(this).load(Uri.parse("file:///android_asset/characters_images/ai-generated-9221232_1920.png")).into(binding.characterFullImage);
            }
        });
    }

    private void updateHpDisplay() {
        if (binding == null) return;
        Integer cur = viewModel.getCurrentHp().getValue();
        Integer max = viewModel.getMaxHp().getValue();
        if (cur != null && max != null) {
            binding.hpText.setText(cur + " / " + max);
        }
    }

    /**
     * JAVADOC: attributesContainer is a dynamic layout. We use removeAllViews() and addView()
     * because these TextViews are generated programmatically based on the character's 
     * attribute data at runtime. View Binding cannot be used for views that do not 
     * exist in the XML layout.
     */
    private void displayAttributes(Map<String, Integer> attrs) {
        if (attrs == null || binding == null) return;
        binding.attributesContainer.removeAllViews();
        String[] order = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (String key : order) {
            Integer val = attrs.getOrDefault(key, 10);
            int mod = CharacterEngine.getAbilityModifier(val);
            TextView tv = new TextView(getContext());
            tv.setTextAppearance(R.style.QuestRollBody);
            tv.setText(key + ": " + val + " (" + (mod >= 0 ? "+" + mod : mod) + ")");
            tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_semibold));
            tv.setPadding(0, 8, 0, 8);
            binding.attributesContainer.addView(tv);
        }
    }

    /**
     * JAVADOC: savingThrowsContainer is a dynamic layout. Views are created programmatically
     * and added to the container at runtime. Since these views aren't defined in the 
     * XML layout, they cannot be accessed through View Binding.
     */
    private void displaySavingThrows(List<String> profs) {
        if (binding == null || binding.savingThrowsContainer == null || profs == null) return;
        binding.savingThrowsContainer.removeAllViews();
        Map<String, Integer> attrs = viewModel.getAttributes().getValue();
        if (attrs == null) return;

        String[] order = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        int totalLevel = viewModel.getCharacter().getValue() != null ? viewModel.getCharacter().getValue().totalLevel : 1;
        int profBonus = CharacterEngine.getProficiencyBonus(totalLevel);
        
        for (String key : order) {
            int mod = CharacterEngine.getAbilityModifier(attrs.getOrDefault(key, 10));
            if (profs.contains(key)) mod += profBonus;
            
            TextView tv = new TextView(getContext());
            tv.setTextAppearance(R.style.QuestRollBody);
            tv.setText(key + ": " + (mod >= 0 ? "+" : "") + mod);
            tv.setPadding(0, 4, 0, 4);
            binding.savingThrowsContainer.addView(tv);
        }
    }

    /**
     * JAVADOC: skillsContainer is updated dynamically by adding programmatically created 
     * TextViews. This allows the UI to adapt to any number of skill bonuses without 
     * a pre-defined list in XML.
     */
    private void displaySkills(Map<String, Integer> skills) {
        if (binding == null || binding.skillsContainer == null || skills == null) return;
        binding.skillsContainer.removeAllViews();
        
        for (Map.Entry<String, Integer> entry : skills.entrySet()) {
            TextView tv = new TextView(getContext());
            tv.setTextAppearance(R.style.QuestRollBody);
            String skillName = entry.getKey().replace("_", " ");
            skillName = skillName.substring(0, 1).toUpperCase() + skillName.substring(1);
            tv.setText(skillName + ": " + (entry.getValue() >= 0 ? "+" : "") + entry.getValue());
            tv.setPadding(0, 4, 0, 4);
            binding.skillsContainer.addView(tv);
        }
    }

    /**
     * JAVADOC: traitsContainer uses addView() for programmatically created TextViews 
     * to display character traits. This dynamic approach is used because the number 
     * and content of traits vary significantly between characters.
     */
    private void displayTraits(List<CharacterTraitEntity> traits) {
        if (binding == null || binding.traitsContainer == null) return;
        binding.traitsContainer.removeAllViews();
        if (traits == null || traits.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("No traits found.");
            tv.setTextAppearance(R.style.QuestRollBody);
            binding.traitsContainer.addView(tv);
            return;
        }
        for (CharacterTraitEntity trait : traits) {
            TextView nameTv = new TextView(getContext());
            nameTv.setText(trait.name);
            nameTv.setTextAppearance(R.style.QuestRollTitle);
            binding.traitsContainer.addView(nameTv);

            TextView descTv = new TextView(getContext());
            markwon.setMarkdown(descTv, trait.description != null ? trait.description : "");
            descTv.setTextAppearance(R.style.QuestRollBody);
            descTv.setPadding(0, 0, 0, 16);
            binding.traitsContainer.addView(descTv);
        }
    }

    /**
     * JAVADOC: inventoryContainer is populated dynamically with TextViews at runtime. 
     * Because the inventory list is generated on the fly, static View Binding 
     * for individual rows in this specific container is not possible.
     */
    private void displayInventory(List<InventoryItemEntity> items) {
        if (binding == null || binding.inventoryContainer == null) return;
        binding.inventoryContainer.removeAllViews();
        if (items == null || items.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("Inventory is empty.");
            tv.setTextAppearance(R.style.QuestRollBody);
            binding.inventoryContainer.addView(tv);
            return;
        }
        for (InventoryItemEntity item : items) {
            TextView tv = new TextView(getContext());
            String name = (item.customName != null) ? item.customName : item.itemKey;
            tv.setText(name + " (x" + item.quantity + ")");
            tv.setTextAppearance(R.style.QuestRollBody);
            tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_regular));
            tv.setPadding(0, 4, 0, 4);
            binding.inventoryContainer.addView(tv);
        }
    }

    private void exportToPdf() {
        if (binding == null) return;
        PrintManager printManager = (PrintManager) requireContext().getSystemService(Context.PRINT_SERVICE);
        printManager.print("CharacterSheet_" + binding.characterName.getText(), new PrintDocumentAdapter() {
            @Override public void onLayout(PrintAttributes old, PrintAttributes newAttr, CancellationSignal sig, LayoutResultCallback cb, Bundle b) {
                cb.onLayoutFinished(new android.print.PrintDocumentInfo.Builder("sheet").setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build(), true);
            }
            @Override public void onWrite(PageRange[] p, ParcelFileDescriptor dest, CancellationSignal sig, WriteResultCallback cb) {
                PdfDocument doc = new PdfDocument();
                PdfDocument.Page page = doc.startPage(new PdfDocument.PageInfo.Builder(595, 842, 1).create());
                Canvas c = page.getCanvas();
                Paint paint = new Paint();
                int y = 60;
                paint.setTextSize(26); paint.setFakeBoldText(true); c.drawText(binding.characterName.getText().toString(), 50, y, paint);
                y += 40; paint.setTextSize(14); paint.setFakeBoldText(false); c.drawText(binding.characterClasses.getText().toString(), 50, y, paint);
                y += 30; c.drawText("STATS: " + binding.hpText.getText() + " | " + binding.acText.getText() + " | " + binding.initiativeText.getText(), 50, y, paint);
                y += 50; paint.setFakeBoldText(true); c.drawText("ATTRIBUTES:", 50, y, paint); paint.setFakeBoldText(false);
                for (int i = 0; i < binding.attributesContainer.getChildCount(); i++) {
                    y += 20; c.drawText(((TextView)binding.attributesContainer.getChildAt(i)).getText().toString(), 60, y, paint);
                }
                doc.finishPage(page);
                try { doc.writeTo(new FileOutputStream(dest.getFileDescriptor())); } catch (IOException e) { cb.onWriteFailed(e.toString()); }
                finally { doc.close(); }
                cb.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        }, null);
    }
}
