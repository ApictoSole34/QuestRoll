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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.character.InventoryItemEntity;
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
    private TextView nameView, speciesView, classesView, hpView, acView, initiativeView;
    private LinearLayout attributesContainer, savingThrowsContainer, skillsContainer, traitsContainer, inventoryContainer;
    private ImageView fullImageView;
    private Markwon markwon;
    private long characterId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        characterId = getArguments() != null ? getArguments().getLong("character_id", -1) : -1;

        markwon = Markwon.create(requireContext());
        viewModel = new ViewModelProvider(this).get(CharacterSheetViewModel.class);
        viewModel.init(characterId, requireContext());

        // UI Binding - Basic Info
        nameView = view.findViewById(R.id.character_name);
        speciesView = view.findViewById(R.id.character_species);
        classesView = view.findViewById(R.id.character_classes);
        hpView = view.findViewById(R.id.hp_text);
        acView = view.findViewById(R.id.ac_text);
        initiativeView = view.findViewById(R.id.initiative_text);
        fullImageView = view.findViewById(R.id.character_full_image);

        // UI Binding - Containers
        attributesContainer = view.findViewById(R.id.attributes_container);
        savingThrowsContainer = view.findViewById(R.id.saving_throws_container);
        skillsContainer = view.findViewById(R.id.skills_container);
        traitsContainer = view.findViewById(R.id.traits_container);
        inventoryContainer = view.findViewById(R.id.inventory_container);

        setupObservers();

        view.findViewById(R.id.btn_export_pdf).setOnClickListener(v -> exportToPdf());
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
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
        viewModel.getCharacter().observe(getViewLifecycleOwner(), c -> { if (c != null) nameView.setText(c.name); });
        viewModel.getSpeciesName().observe(getViewLifecycleOwner(), name -> speciesView.setText("Race: " + name));
        viewModel.getClassNames().observe(getViewLifecycleOwner(), names -> classesView.setText("Classes: " + String.join(", ", names)));
        
        viewModel.getCurrentHp().observe(getViewLifecycleOwner(), hp -> updateHpDisplay());
        viewModel.getMaxHp().observe(getViewLifecycleOwner(), max -> updateHpDisplay());
        
        viewModel.getArmorClass().observe(getViewLifecycleOwner(), ac -> acView.setText(String.valueOf(ac)));
        viewModel.getInitiative().observe(getViewLifecycleOwner(), in -> initiativeView.setText((in >= 0 ? "+" : "") + in));
        
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
            if (path != null && !path.isEmpty() && new File(path).exists()) {
                Glide.with(this).load(new File(path)).into(fullImageView);
            } else {
                Glide.with(this).load(Uri.parse("file:///android_asset/characters_images/ai-generated-9221232_1920.png")).into(fullImageView);
            }
        });
    }

    private void updateHpDisplay() {
        Integer cur = viewModel.getCurrentHp().getValue();
        Integer max = viewModel.getMaxHp().getValue();
        if (cur != null && max != null) {
            hpView.setText(cur + " / " + max);
        }
    }

    private void displayAttributes(Map<String, Integer> attrs) {
        if (attrs == null) return;
        attributesContainer.removeAllViews();
        String[] order = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (String key : order) {
            Integer val = attrs.getOrDefault(key, 10);
            int mod = CharacterEngine.getAbilityModifier(val);
            TextView tv = new TextView(getContext());
            tv.setTextAppearance(R.style.QuestRollBody);
            tv.setText(key + ": " + val + " (" + (mod >= 0 ? "+" + mod : mod) + ")");
            tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_semibold));
            tv.setPadding(0, 8, 0, 8);
            attributesContainer.addView(tv);
        }
    }

    private void displaySavingThrows(List<String> profs) {
        if (savingThrowsContainer == null || profs == null) return;
        savingThrowsContainer.removeAllViews();
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
            savingThrowsContainer.addView(tv);
        }
    }

    private void displaySkills(Map<String, Integer> skills) {
        if (skillsContainer == null || skills == null) return;
        skillsContainer.removeAllViews();
        
        for (Map.Entry<String, Integer> entry : skills.entrySet()) {
            TextView tv = new TextView(getContext());
            tv.setTextAppearance(R.style.QuestRollBody);
            String skillName = entry.getKey().replace("_", " ");
            skillName = skillName.substring(0, 1).toUpperCase() + skillName.substring(1);
            tv.setText(skillName + ": " + (entry.getValue() >= 0 ? "+" : "") + entry.getValue());
            tv.setPadding(0, 4, 0, 4);
            skillsContainer.addView(tv);
        }
    }

    private void displayTraits(List<CharacterTraitEntity> traits) {
        if (traitsContainer == null) return;
        traitsContainer.removeAllViews();
        if (traits == null || traits.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("No traits found.");
            tv.setTextAppearance(R.style.QuestRollBody);
            traitsContainer.addView(tv);
            return;
        }
        for (CharacterTraitEntity trait : traits) {
            TextView nameTv = new TextView(getContext());
            nameTv.setText(trait.name);
            nameTv.setTextAppearance(R.style.QuestRollTitle);
            traitsContainer.addView(nameTv);

            TextView descTv = new TextView(getContext());
            markwon.setMarkdown(descTv, trait.description != null ? trait.description : "");
            descTv.setTextAppearance(R.style.QuestRollBody);
            descTv.setPadding(0, 0, 0, 16);
            traitsContainer.addView(descTv);
        }
    }

    private void displayInventory(List<InventoryItemEntity> items) {
        if (inventoryContainer == null) return;
        inventoryContainer.removeAllViews();
        if (items == null || items.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("Inventory is empty.");
            tv.setTextAppearance(R.style.QuestRollBody);
            inventoryContainer.addView(tv);
            return;
        }
        for (InventoryItemEntity item : items) {
            TextView tv = new TextView(getContext());
            String name = (item.customName != null) ? item.customName : item.itemKey;
            tv.setText(name + " (x" + item.quantity + ")");
            tv.setTextAppearance(R.style.QuestRollBody);
            tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_regular));
            tv.setPadding(0, 4, 0, 4);
            inventoryContainer.addView(tv);
        }
    }

    private void exportToPdf() {
        PrintManager printManager = (PrintManager) requireContext().getSystemService(Context.PRINT_SERVICE);
        printManager.print("CharacterSheet_" + nameView.getText(), new PrintDocumentAdapter() {
            @Override public void onLayout(PrintAttributes old, PrintAttributes newAttr, CancellationSignal sig, LayoutResultCallback cb, Bundle b) {
                cb.onLayoutFinished(new android.print.PrintDocumentInfo.Builder("sheet").setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build(), true);
            }
            @Override public void onWrite(PageRange[] p, ParcelFileDescriptor dest, CancellationSignal sig, WriteResultCallback cb) {
                PdfDocument doc = new PdfDocument();
                PdfDocument.Page page = doc.startPage(new PdfDocument.PageInfo.Builder(595, 842, 1).create());
                Canvas c = page.getCanvas();
                Paint paint = new Paint();
                int y = 60;
                paint.setTextSize(26); paint.setFakeBoldText(true); c.drawText(nameView.getText().toString(), 50, y, paint);
                y += 40; paint.setTextSize(14); paint.setFakeBoldText(false); c.drawText(classesView.getText().toString(), 50, y, paint);
                y += 30; c.drawText("STATS: " + hpView.getText() + " | " + acView.getText() + " | " + initiativeView.getText(), 50, y, paint);
                y += 50; paint.setFakeBoldText(true); c.drawText("ATTRIBUTES:", 50, y, paint); paint.setFakeBoldText(false);
                for (int i = 0; i < attributesContainer.getChildCount(); i++) {
                    y += 20; c.drawText(((TextView)attributesContainer.getChildAt(i)).getText().toString(), 60, y, paint);
                }
                doc.finishPage(page);
                try { doc.writeTo(new FileOutputStream(dest.getFileDescriptor())); } catch (IOException e) { cb.onWriteFailed(e.toString()); }
                finally { doc.close(); }
                cb.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        }, null);
    }
}
