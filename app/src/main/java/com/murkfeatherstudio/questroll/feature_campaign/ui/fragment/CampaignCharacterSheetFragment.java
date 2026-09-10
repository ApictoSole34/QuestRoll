package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.config.MulticlassPrerequisiteConfig;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.PlayerCharacterDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterAttributesEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterClassAssignmentEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterWithRelations;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentCampaignCharacterSheetBinding;
import com.murkfeatherstudio.questroll.feature_campaign.adapter.BackgroundRaceTraitAdapter;
import com.murkfeatherstudio.questroll.feature_campaign.adapter.SimpleTextAdapter;
import com.murkfeatherstudio.questroll.feature_campaign.engine.CharacterEngine;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.CampaignDetailViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CampaignCharacterSheetFragment extends Fragment {

    private static final String ARG_CAMPAIGN_ID = "campaign_id";

    private long campaignId;
    private CampaignDetailViewModel viewModel;
    private CharacterEngine engine;
    private FragmentCampaignCharacterSheetBinding binding;

    private static final int SECTION_SKILLS          = 0;
    private static final int SECTION_LANGUAGES       = 1;
    private static final int SECTION_BACKGROUND_RACE = 2;
    private int expandedSection = -1;

    private SimpleTextAdapter skillsAdapter, languagesAdapter;
    private BackgroundRaceTraitAdapter backgroundRaceAdapter;

    public static CampaignCharacterSheetFragment newInstance(long campaignId) {
        Bundle args = new Bundle();
        args.putLong(ARG_CAMPAIGN_ID, campaignId);
        CampaignCharacterSheetFragment f = new CampaignCharacterSheetFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) campaignId = getArguments().getLong(ARG_CAMPAIGN_ID);
        viewModel = new ViewModelProvider(requireActivity()).get(CampaignDetailViewModel.class);
        viewModel.setCampaignId(campaignId);
        engine = new CharacterEngine();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCampaignCharacterSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapters();
        setupAccordion();
        setupHpButtons();
        setupExhaustionButtons();
        setupInspirationCheckbox();
        binding.btnLevelUp.setOnClickListener(v -> showLevelUpDialog());
        binding.btnAssignCharacter.setOnClickListener(v -> showCharacterPickerDialog());

        viewModel.characterWithRelations.observe(getViewLifecycleOwner(), this::updateUI);

        viewModel.getEffectiveAc().observe(getViewLifecycleOwner(), ac -> {
            if (binding == null) return;
            binding.tvAc.setText(String.valueOf(ac != null ? ac : "10"));
        });
    }

    private void setupAdapters() {
        skillsAdapter    = new SimpleTextAdapter();
        languagesAdapter = new SimpleTextAdapter();
        backgroundRaceAdapter = new BackgroundRaceTraitAdapter();
        setupRv(binding.rvSkills,    skillsAdapter);
        setupRv(binding.rvLanguages, languagesAdapter);
        setupRv(binding.rvBackgroundRace, backgroundRaceAdapter);
    }

    private void setupRv(RecyclerView rv, RecyclerView.Adapter<?> adapter) {
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        rv.setNestedScrollingEnabled(false);
    }

    private void setupAccordion() {
        binding.headerSkills.setOnClickListener(v -> toggleSection(SECTION_SKILLS));
        binding.headerLanguages.setOnClickListener(v -> toggleSection(SECTION_LANGUAGES));
        binding.headerBackgroundRace.setOnClickListener(v -> toggleSection(SECTION_BACKGROUND_RACE));
    }

    private void toggleSection(int tapped) {
        boolean opening = (expandedSection != tapped);
        setSection(SECTION_SKILLS, false);
        setSection(SECTION_LANGUAGES, false);
        setSection(SECTION_BACKGROUND_RACE, false);
        if (opening) {
            setSection(tapped, true);
            expandedSection = tapped;
        } else {
            expandedSection = -1;
        }
    }

    private void setSection(int section, boolean open) {
        if (binding == null) return;
        View container;
        TextView arrow;
        switch (section) {
            case SECTION_SKILLS: container = binding.containerSkills; arrow = binding.arrowSkills; break;
            case SECTION_LANGUAGES: container = binding.containerLanguages; arrow = binding.arrowLanguages; break;
            case SECTION_BACKGROUND_RACE: container = binding.containerBackgroundRace; arrow = binding.arrowBackgroundRace; break;
            default: return;
        }
        container.setVisibility(open ? View.VISIBLE : View.GONE);
        arrow.setText(open ? "▲" : "▼");
    }

    private void setupHpButtons() {
        binding.btnHpMinus.setOnClickListener(v -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setCurrentHp(Math.max(0, c.currentHp - 1));
        });
        binding.btnHpPlus.setOnClickListener(v -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setCurrentHp(Math.min(c.maxHp, c.currentHp + 1));
        });
        binding.btnSetMaxHp.setOnClickListener(v -> showSetHpDialog());
    }

    private void setupExhaustionButtons() {
        binding.btnExhaustionMinus.setOnClickListener(v -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setExhaustion(Math.max(0, c.exhaustionLevel - 1));
        });
        binding.btnExhaustionPlus.setOnClickListener(v -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setExhaustion(Math.min(6, c.exhaustionLevel + 1));
        });
    }

    private void setupInspirationCheckbox() {
        binding.cbInspiration.setOnCheckedChangeListener((btn, checked) -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setInspiration(checked);
        });
    }

    private void updateUI(CharacterWithRelations cwr) {
        if (binding == null) return;
        if (cwr == null || cwr.character == null) {
            binding.contentGroup.setVisibility(View.GONE);
            binding.btnAssignCharacter.setVisibility(View.VISIBLE);
            return;
        }
        binding.contentGroup.setVisibility(View.VISIBLE);
        binding.btnAssignCharacter.setVisibility(View.GONE);

        CharacterEntity c = cwr.character;
        binding.tvCharacterName.setText(c.name != null ? c.name : "—");
        binding.tvClassLevel.setText(buildClassString(cwr.classAssignments, c.totalLevel));
        binding.tvRaceBackground.setText(formatKey(c.speciesKey) + " · " + formatKey(c.backgroundKey));

        updateHpDisplay(c);

        int prof = CharacterEngine.getProficiencyBonus(c.totalLevel);
        binding.tvProfBonus.setText("+" + prof);

        if (cwr.attributes != null) {
            CharacterAttributesEntity a = cwr.attributes;
            setAttr(binding.tvStr, binding.tvStrMod, a.strength, a.strengthMod);
            setAttr(binding.tvDex, binding.tvDexMod, a.dexterity, a.dexterityMod);
            setAttr(binding.tvCon, binding.tvConMod, a.constitution, a.constitutionMod);
            setAttr(binding.tvInt, binding.tvIntMod, a.intelligence, a.intelligenceMod);
            setAttr(binding.tvWis, binding.tvWisMod, a.wisdom, a.wisdomMod);
            setAttr(binding.tvCha, binding.tvChaMod, a.charisma, a.charismaMod);
            binding.tvInitiative.setText(fmtMod(a.dexterityMod));

            boolean percProf = cwr.skillProficiencies != null &&
                    cwr.skillProficiencies.stream().anyMatch(sp -> "perception".equals(sp.skillKey));
            int passive = 10 + a.wisdomMod + (percProf ? prof : 0);
            binding.tvPassivePerception.setText(String.valueOf(passive));
        }

        if (cwr.attributes != null && cwr.savingThrows != null) {
            Map<String, Integer> st = engine.getSavingThrowBonuses(cwr.attributes, cwr.savingThrows, c.totalLevel);
            binding.tvSaveStr.setText("STR " + fmtMod(st.getOrDefault("STR", 0)));
            binding.tvSaveDex.setText("DEX " + fmtMod(st.getOrDefault("DEX", 0)));
            binding.tvSaveCon.setText("CON " + fmtMod(st.getOrDefault("CON", 0)));
            binding.tvSaveInt.setText("INT " + fmtMod(st.getOrDefault("INT", 0)));
            binding.tvSaveWis.setText("WIS " + fmtMod(st.getOrDefault("WIS", 0)));
            binding.tvSaveCha.setText("CHA " + fmtMod(st.getOrDefault("CHA", 0)));
        }

        if (cwr.attributes != null && cwr.skillProficiencies != null) {
            List<String> profKeys = cwr.skillProficiencies.stream().map(sp -> sp.skillKey).collect(Collectors.toList());
            Map<String, Integer> bonuses = engine.getSkillBonuses(cwr.attributes, profKeys, c.totalLevel);
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, Integer> e : bonuses.entrySet()) {
                lines.add(formatKey(e.getKey()) + ":  " + fmtMod(e.getValue()));
            }
            skillsAdapter.setItems(lines);
        } else {
            skillsAdapter.setItems(List.of("—"));
        }

        if (cwr.languages != null && !cwr.languages.isEmpty()) {
            languagesAdapter.setItems(cwr.languages.stream()
                .map(l -> l.languageName != null ? l.languageName : formatKey(l.languageKey))
                .collect(Collectors.toList()));
        } else {
            languagesAdapter.setItems(List.of("—"));
        }

        List<CharacterTraitEntity> bgRaceTraits = new ArrayList<>();
        if (cwr.traits != null) {
            for (CharacterTraitEntity t : cwr.traits) {
                if ("RACE".equals(t.sourceType) || "BACKGROUND".equals(t.sourceType)) {
                    bgRaceTraits.add(t);
                }
            }
        }
        backgroundRaceAdapter.setItems(bgRaceTraits);

        binding.tvExhaustion.setText(String.valueOf(c.exhaustionLevel));
        binding.cbInspiration.setChecked(c.hasInspiration);

        if (binding.ivCharacterImage != null) {
            String imagePath = c.imagePath;
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) binding.ivCharacterImage.setImageURI(Uri.fromFile(imageFile));
                else binding.ivCharacterImage.setImageResource(android.R.drawable.ic_menu_my_calendar);
            } else {
                binding.ivCharacterImage.setImageResource(android.R.drawable.ic_menu_my_calendar);
            }
        }
    }

    private void updateHpDisplay(CharacterEntity c) {
        if (binding == null) return;
        binding.tvHp.setText(c.currentHp + " / " + c.maxHp);
        binding.tvTempHp.setText("Temp HP: " + c.temporaryHp);
        if (binding.pbHp != null) {
            int pct = (c.maxHp > 0) ? Math.round(c.currentHp * 100f / c.maxHp) : 0;
            binding.pbHp.setProgress(Math.max(0, Math.min(100, pct)));
            int color = (pct > 50) ? 0xFF4CAF50 : (pct > 25 ? 0xFFFF9800 : 0xFFF44336);
            binding.pbHp.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
        }
    }

    private void showSetHpDialog() {
        CharacterEntity c = currentCharacter();
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        android.widget.EditText etCurrent = new android.widget.EditText(requireContext());
        etCurrent.setHint("Current HP");
        etCurrent.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (c != null) etCurrent.setText(String.valueOf(c.currentHp));

        android.widget.EditText etMax = new android.widget.EditText(requireContext());
        etMax.setHint("Max HP");
        etMax.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (c != null) etMax.setText(String.valueOf(c.maxHp));

        android.widget.EditText etTemp = new android.widget.EditText(requireContext());
        etTemp.setHint("Temporary HP");
        etTemp.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (c != null) etTemp.setText(String.valueOf(c.temporaryHp));

        layout.addView(etCurrent); layout.addView(etMax); layout.addView(etTemp);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set HP")
                .setView(layout)
                .setPositiveButton("Apply", (d, w) -> {
                    try {
                        viewModel.setCurrentHp(Integer.parseInt(etCurrent.getText().toString().trim()));
                        viewModel.setMaxHp(Integer.parseInt(etMax.getText().toString().trim()));
                        viewModel.setTemporaryHp(Integer.parseInt(etTemp.getText().toString().trim()));
                    } catch (NumberFormatException ignored) {}
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCharacterPickerDialog() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            List<CharacterEntity> chars = PlayerCharacterDatabase.getInstance(requireContext()).characterDao().getAllCharactersSync();
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || chars.isEmpty()) return;
                String[] names = chars.stream().map(c -> c.name).toArray(String[]::new);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Assign character to campaign")
                        .setItems(names, (dialog, which) -> viewModel.assignCharacter(chars.get(which).id))
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
    }

    private void showLevelUpDialog() {
        CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
        if (cwr == null || cwr.character == null) return;
        List<CharacterClassAssignmentEntity> classes = cwr.classAssignments != null ? cwr.classAssignments : new ArrayList<>();
        List<String> options = new ArrayList<>();
        List<String> classKeys = new ArrayList<>();
        for (CharacterClassAssignmentEntity ca : classes) {
            options.add(formatKey(ca.classKey) + " (level " + ca.level + ") → level " + (ca.level + 1));
            classKeys.add(ca.classKey);
        }
        options.add("➕ Add new class");
        classKeys.add(null);

        new AlertDialog.Builder(requireContext())
                .setTitle("Level up which class?")
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    if (which == options.size() - 1) showAddNewClassDialog(cwr.character.id);
                    else openLevelUpWizard(cwr.character.id, classKeys.get(which), false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddNewClassDialog(long characterId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            String gameSystem = viewModel.getCurrentGameSystem();
            List<CharacterClassEntity> baseClasses = Open5eDatabase.getInstance(requireContext()).characterClassDao().getBaseClassesByGameSystem(gameSystem);
            List<CustomCharacterClassEntity> customClasses = UserContentDatabase.getInstance(requireContext()).customCharacterClassDao().getBaseClassesSync(gameSystem);

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
                if (cwr == null || cwr.attributes == null) return;

                Map<String, Integer> attrs = new HashMap<>();
                attrs.put("STR", cwr.attributes.strength);
                attrs.put("DEX", cwr.attributes.dexterity);
                attrs.put("CON", cwr.attributes.constitution);
                attrs.put("INT", cwr.attributes.intelligence);
                attrs.put("WIS", cwr.attributes.wisdom);
                attrs.put("CHA", cwr.attributes.charisma);

                List<String> currentClassKeys = cwr.classAssignments.stream().map(ca -> ca.classKey).collect(Collectors.toList());
                Map<String, Map<String, Integer>> customPrereqsMap = new HashMap<>();
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, Integer>>(){}.getType();
                for (CustomCharacterClassEntity cc : customClasses) {
                    if (cc.multiclassPrereqsJson != null && !cc.multiclassPrereqsJson.isEmpty()) {
                        customPrereqsMap.put("custom_" + cc.id, gson.fromJson(cc.multiclassPrereqsJson, type));
                    }
                }

                List<Object> allPossible = new ArrayList<>();
                allPossible.addAll(baseClasses);
                allPossible.addAll(customClasses);

                List<Object> available = new ArrayList<>();
                List<String> unavailableMessages = new ArrayList<>();

                for (Object obj : allPossible) {
                    String key = (obj instanceof CharacterClassEntity) ? ((CharacterClassEntity)obj).key : "custom_" + ((CustomCharacterClassEntity)obj).id;
                    String name = (obj instanceof CharacterClassEntity) ? ((CharacterClassEntity)obj).name : ((CustomCharacterClassEntity)obj).name;
                    if (MulticlassPrerequisiteConfig.meetsMulticlassPrerequisites(currentClassKeys, key, attrs, customPrereqsMap)) {
                        available.add(obj);
                    } else {
                        unavailableMessages.add(name + " (" + MulticlassPrerequisiteConfig.getPrerequisiteMessage(key, attrs, customPrereqsMap) + ")");
                    }
                }

                if (available.isEmpty()) {
                    StringBuilder sb = new StringBuilder("No new classes available:\n");
                    for (String msg : unavailableMessages) sb.append("• ").append(msg).append("\n");
                    Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_LONG).show();
                    return;
                }

                String[] names = available.stream().map(o -> (o instanceof CharacterClassEntity) ? ((CharacterClassEntity)o).name : ((CustomCharacterClassEntity)o).name).toArray(String[]::new);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Choose new class")
                        .setItems(names, (dialog, which) -> {
                            Object chosen = available.get(which);
                            String key = (chosen instanceof CharacterClassEntity) ? ((CharacterClassEntity)chosen).key : "custom_" + ((CustomCharacterClassEntity)chosen).id;
                            openLevelUpWizard(characterId, key, true);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
    }

    private void openLevelUpWizard(long characterId, String classKey, boolean isNewClass) {
        LevelUpWizardFragment wizard = LevelUpWizardFragment.newInstance(characterId, classKey, isNewClass);
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, wizard).addToBackStack(null).commit();
    }

    private CharacterEntity currentCharacter() {
        CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
        return (cwr != null) ? cwr.character : null;
    }

    private void setAttr(TextView tvVal, TextView tvMod, int value, int mod) {
        if (tvVal != null) tvVal.setText(String.valueOf(value));
        if (tvMod != null) tvMod.setText(fmtMod(mod));
    }

    private String fmtMod(int mod) { return mod >= 0 ? "+" + mod : String.valueOf(mod); }

    private String formatKey(String key) {
        if (key == null || key.isEmpty()) return "—";
        String result = key;
        if (key.startsWith("srd_")) result = key.substring(4);
        else if (key.startsWith("a5e-ag_")) result = key.substring(7);
        else if (key.startsWith("custom_")) result = key.substring(7);

        String[] words = result.split("[_\\-]");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) sb.append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private String buildClassString(List<CharacterClassAssignmentEntity> assignments, int totalLevel) {
        if (assignments == null || assignments.isEmpty()) return "Level " + totalLevel;
        StringBuilder sb = new StringBuilder();
        for (CharacterClassAssignmentEntity ca : assignments) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(formatKey(ca.classKey)).append(" ").append(ca.level);
        }
        return sb + "  (total " + totalLevel + ")";
    }
}
