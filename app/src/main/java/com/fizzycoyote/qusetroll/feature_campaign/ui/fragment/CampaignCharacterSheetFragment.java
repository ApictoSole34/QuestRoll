package com.fizzycoyote.qusetroll.feature_campaign.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.config.MulticlassPrerequisiteConfig;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.PlayerCharacterDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterAttributesEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterClassAssignmentEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterWithRelations;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.feature_campaign.adapter.SimpleTextAdapter;
import com.fizzycoyote.qusetroll.feature_campaign.engine.CharacterEngine;
import com.fizzycoyote.qusetroll.feature_campaign.view_model.CampaignDetailViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
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

    private ImageView ivCharacterImage;

    // ── Content visibility ─────────────────────────────
    private View contentGroup;
    private Button btnAssignCharacter;

    // ── Header ─────────────────────────────────────────
    private TextView tvName, tvClassLevel, tvRaceBackground;

    // ── HP ─────────────────────────────────────────────
    private TextView tvHp, tvTempHp;
    private ProgressBar pbHp;

    // ── Quick stats ────────────────────────────────────
    private TextView tvProfBonus, tvInitiative, tvAc, tvPassivePerception;

    // ── Attributes ─────────────────────────────────────
    private TextView tvStr, tvStrMod, tvDex, tvDexMod,
            tvCon, tvConMod, tvInt, tvIntMod,
            tvWis, tvWisMod, tvCha, tvChaMod;

    // ── Saving throws ──────────────────────────────────
    private TextView tvSaveStr, tvSaveDex, tvSaveCon,
            tvSaveInt, tvSaveWis, tvSaveCha;

    // ── Misc ───────────────────────────────────────────
    private TextView tvExhaustion;
    private Button btnExhaustionMinus, btnExhaustionPlus;
    private CheckBox cbInspiration;

    // ── Accordion: containers + arrows ────────────────
    private View containerSkills, containerLanguages;
    private TextView arrowSkills, arrowLanguages;
    private static final int SECTION_SKILLS    = 0;
    private static final int SECTION_LANGUAGES = 1;
    private int expandedSection = -1; // none open by default

    // ── Adapters ───────────────────────────────────────
    private SimpleTextAdapter skillsAdapter, languagesAdapter;

    // ──────────────────────────────────────────────────
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
        return inflater.inflate(R.layout.fragment_campaign_character_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupAdapters();
        setupAccordion();
        setupHpButtons();
        setupExhaustionButtons();
        setupInspirationCheckbox();
        Button btnLevelUp = view.findViewById(R.id.btn_level_up);
        btnLevelUp.setOnClickListener(v -> showLevelUpDialog());

        btnAssignCharacter.setOnClickListener(v -> showCharacterPickerDialog());

        viewModel.characterWithRelations.observe(getViewLifecycleOwner(), this::updateUI);

        viewModel.getEffectiveAc().observe(getViewLifecycleOwner(), ac -> {
            if (ac != null) {
                tvAc.setText(String.valueOf(ac));
            } else {
                tvAc.setText("10");
            }
        });
    }

    // ──────────────────────────────────────────────────
    // View binding
    // ──────────────────────────────────────────────────

    private void bindViews(View v) {

        ivCharacterImage = v.findViewById(R.id.iv_character_image);

        contentGroup        = v.findViewById(R.id.content_group);
        btnAssignCharacter  = v.findViewById(R.id.btn_assign_character);

        tvName              = v.findViewById(R.id.tv_character_name);
        tvClassLevel        = v.findViewById(R.id.tv_class_level);
        tvRaceBackground    = v.findViewById(R.id.tv_race_background);

        tvHp                = v.findViewById(R.id.tv_hp);
        tvTempHp            = v.findViewById(R.id.tv_temp_hp);
        pbHp                = v.findViewById(R.id.pb_hp);

        tvProfBonus         = v.findViewById(R.id.tv_prof_bonus);
        tvInitiative        = v.findViewById(R.id.tv_initiative);
        tvAc                = v.findViewById(R.id.tv_ac);
        tvPassivePerception = v.findViewById(R.id.tv_passive_perception);

        tvStr = v.findViewById(R.id.tv_str); tvStrMod = v.findViewById(R.id.tv_str_mod);
        tvDex = v.findViewById(R.id.tv_dex); tvDexMod = v.findViewById(R.id.tv_dex_mod);
        tvCon = v.findViewById(R.id.tv_con); tvConMod = v.findViewById(R.id.tv_con_mod);
        tvInt = v.findViewById(R.id.tv_int); tvIntMod = v.findViewById(R.id.tv_int_mod);
        tvWis = v.findViewById(R.id.tv_wis); tvWisMod = v.findViewById(R.id.tv_wis_mod);
        tvCha = v.findViewById(R.id.tv_cha); tvChaMod = v.findViewById(R.id.tv_cha_mod);

        tvSaveStr = v.findViewById(R.id.tv_save_str);
        tvSaveDex = v.findViewById(R.id.tv_save_dex);
        tvSaveCon = v.findViewById(R.id.tv_save_con);
        tvSaveInt = v.findViewById(R.id.tv_save_int);
        tvSaveWis = v.findViewById(R.id.tv_save_wis);
        tvSaveCha = v.findViewById(R.id.tv_save_cha);

        tvExhaustion       = v.findViewById(R.id.tv_exhaustion);
        btnExhaustionMinus = v.findViewById(R.id.btn_exhaustion_minus);
        btnExhaustionPlus  = v.findViewById(R.id.btn_exhaustion_plus);
        cbInspiration      = v.findViewById(R.id.cb_inspiration);

        containerSkills    = v.findViewById(R.id.container_skills);
        containerLanguages = v.findViewById(R.id.container_languages);
        arrowSkills        = v.findViewById(R.id.arrow_skills);
        arrowLanguages     = v.findViewById(R.id.arrow_languages);
    }

    // ──────────────────────────────────────────────────
    // RecyclerViews
    // ──────────────────────────────────────────────────

    private void setupAdapters() {
        skillsAdapter    = new SimpleTextAdapter();
        languagesAdapter = new SimpleTextAdapter();

        setupRv(R.id.rv_skills,    skillsAdapter);
        setupRv(R.id.rv_languages, languagesAdapter);
    }

    private void setupRv(int id, SimpleTextAdapter adapter) {
        RecyclerView rv = requireView().findViewById(id);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        rv.setNestedScrollingEnabled(false);
    }

    // ──────────────────────────────────────────────────
    // Accordion logic (exclusive open/close)
    // ──────────────────────────────────────────────────

    private void setupAccordion() {
        requireView().findViewById(R.id.header_skills)
                .setOnClickListener(v -> toggleSection(SECTION_SKILLS));
        requireView().findViewById(R.id.header_languages)
                .setOnClickListener(v -> toggleSection(SECTION_LANGUAGES));
    }

    private void toggleSection(int tapped) {
        boolean opening = (expandedSection != tapped);

        // Collapse all
        setSection(SECTION_SKILLS, false);
        setSection(SECTION_LANGUAGES, false);

        if (opening) {
            setSection(tapped, true);
            expandedSection = tapped;
        } else {
            expandedSection = -1;
        }
    }

    private void setSection(int section, boolean open) {
        View container;
        TextView arrow;
        switch (section) {
            case SECTION_SKILLS:
                container = containerSkills;
                arrow = arrowSkills;
                break;
            case SECTION_LANGUAGES:
                container = containerLanguages;
                arrow = arrowLanguages;
                break;
            default:
                return;
        }
        if (container == null) return;
        container.setVisibility(open ? View.VISIBLE : View.GONE);
        if (arrow != null) arrow.setText(open ? "▲" : "▼");
    }

    // ──────────────────────────────────────────────────
    // HP buttons
    // ──────────────────────────────────────────────────

    private void setupHpButtons() {
        requireView().findViewById(R.id.btn_hp_minus).setOnClickListener(v -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setCurrentHp(Math.max(0, c.currentHp - 1));
        });
        requireView().findViewById(R.id.btn_hp_plus).setOnClickListener(v -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setCurrentHp(Math.min(c.maxHp, c.currentHp + 1));
        });
        requireView().findViewById(R.id.btn_set_max_hp).setOnClickListener(v ->
                showSetHpDialog());
    }

    private void setupExhaustionButtons() {
        btnExhaustionMinus.setOnClickListener(v -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setExhaustion(Math.max(0, c.exhaustionLevel - 1));
        });
        btnExhaustionPlus.setOnClickListener(v -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setExhaustion(Math.min(6, c.exhaustionLevel + 1));
        });
    }

    private void setupInspirationCheckbox() {
        cbInspiration.setOnCheckedChangeListener((btn, checked) -> {
            CharacterEntity c = currentCharacter();
            if (c != null) viewModel.setInspiration(checked);
        });
    }

    // ──────────────────────────────────────────────────
    // Main UI update
    // ──────────────────────────────────────────────────

    private void updateUI(CharacterWithRelations cwr) {
        if (cwr == null || cwr.character == null) {
            contentGroup.setVisibility(View.GONE);
            btnAssignCharacter.setVisibility(View.VISIBLE);
            return;
        }
        contentGroup.setVisibility(View.VISIBLE);
        btnAssignCharacter.setVisibility(View.GONE);

        CharacterEntity c = cwr.character;

        // Header
        tvName.setText(c.name != null ? c.name : "—");
        tvClassLevel.setText(buildClassString(cwr.classAssignments, c.totalLevel));
        tvRaceBackground.setText(str(c.speciesKey) + " · " + str(c.backgroundKey));

        // HP
        updateHpDisplay(c);

        // Quick stats
        int prof = CampaignDetailViewModel.calcProficiencyBonus(c.totalLevel);
        tvProfBonus.setText("+" + prof);

        // Attributes
        if (cwr.attributes != null) {
            CharacterAttributesEntity a = cwr.attributes;
            setAttr(tvStr, tvStrMod, a.strength, a.strengthMod);
            setAttr(tvDex, tvDexMod, a.dexterity, a.dexterityMod);
            setAttr(tvCon, tvConMod, a.constitution, a.constitutionMod);
            setAttr(tvInt, tvIntMod, a.intelligence, a.intelligenceMod);
            setAttr(tvWis, tvWisMod, a.wisdom, a.wisdomMod);
            setAttr(tvCha, tvChaMod, a.charisma, a.charismaMod);
            tvInitiative.setText(fmtMod(a.dexterityMod));

            // Passive perception
            boolean percProf = cwr.skillProficiencies != null &&
                    cwr.skillProficiencies.stream().anyMatch(sp -> "perception".equals(sp.skillKey));
            int passive = 10 + a.wisdomMod + (percProf ? prof : 0);
            tvPassivePerception.setText(String.valueOf(passive));
        }

        // Saving throws
        if (cwr.attributes != null && cwr.savingThrows != null) {
            Map<String, Integer> st = engine.getSavingThrowBonuses(cwr.attributes, cwr.savingThrows, c.totalLevel);
            tvSaveStr.setText("STR " + fmtMod(st.getOrDefault("STR", 0)));
            tvSaveDex.setText("DEX " + fmtMod(st.getOrDefault("DEX", 0)));
            tvSaveCon.setText("CON " + fmtMod(st.getOrDefault("CON", 0)));
            tvSaveInt.setText("INT " + fmtMod(st.getOrDefault("INT", 0)));
            tvSaveWis.setText("WIS " + fmtMod(st.getOrDefault("WIS", 0)));
            tvSaveCha.setText("CHA " + fmtMod(st.getOrDefault("CHA", 0)));
        }

        // Skills
        if (cwr.attributes != null && cwr.skillProficiencies != null) {
            List<String> profKeys = cwr.skillProficiencies.stream()
                    .map(sp -> sp.skillKey).collect(Collectors.toList());
            Map<String, Integer> bonuses = engine.getSkillBonuses(cwr.attributes, profKeys, c.totalLevel);
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, Integer> e : bonuses.entrySet()) {
                lines.add(e.getKey() + ":  " + fmtMod(e.getValue()));
            }
            skillsAdapter.setItems(lines);
        } else {
            skillsAdapter.setItems(List.of("—"));
        }

        // Languages
        if (cwr.languages != null && !cwr.languages.isEmpty()) {
            languagesAdapter.setItems(
                    cwr.languages.stream().map(l -> l.languageName != null ? l.languageName : l.languageKey)
                            .collect(Collectors.toList()));
        } else {
            languagesAdapter.setItems(List.of("—"));
        }

        // Exhaustion & Inspiration
        tvExhaustion.setText(String.valueOf(c.exhaustionLevel));
        cbInspiration.setChecked(c.hasInspiration);

        if (ivCharacterImage != null) {
            String imagePath = c.imagePath;
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    ivCharacterImage.setImageURI(Uri.fromFile(imageFile));
                } else {
                    ivCharacterImage.setImageResource(android.R.drawable.ic_menu_my_calendar);
                }
            } else {
                ivCharacterImage.setImageResource(android.R.drawable.ic_menu_my_calendar);
            }
        }
    }

    // ──────────────────────────────────────────────────
    // HP display (text + progress bar)
    // ──────────────────────────────────────────────────

    private void updateHpDisplay(CharacterEntity c) {
        tvHp.setText(c.currentHp + " / " + c.maxHp);
        tvTempHp.setText("Temp HP: " + c.temporaryHp);

        if (pbHp != null) {
            int pct = (c.maxHp > 0)
                    ? Math.round(c.currentHp * 100f / c.maxHp)
                    : 0;
            pbHp.setProgress(Math.max(0, Math.min(100, pct)));

            int color;
            if (pct > 50)      color = 0xFF4CAF50;
            else if (pct > 25) color = 0xFFFF9800;
            else               color = 0xFFF44336;
            pbHp.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(color));
        }
    }

    // ──────────────────────────────────────────────────
    // Dialogs
    // ──────────────────────────────────────────────────

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

        layout.addView(etCurrent);
        layout.addView(etMax);
        layout.addView(etTemp);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set HP")
                .setView(layout)
                .setPositiveButton("Apply", (d, w) -> {
                    try {
                        viewModel.setCurrentHp(Integer.parseInt(etCurrent.getText().toString().trim()));
                        viewModel.setMaxHp(Integer.parseInt(etMax.getText().toString().trim()));
                        viewModel.setTemporaryHp(Integer.parseInt(etTemp.getText().toString().trim()));
                    } catch (NumberFormatException ignored) {
                        Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCharacterPickerDialog() {
        new Thread(() -> {
            if (!isAdded()) return;
            List<CharacterEntity> chars = PlayerCharacterDatabase.getInstance(requireContext())
                    .characterDao().getAllCharactersSync();
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                if (chars.isEmpty()) {
                    Toast.makeText(getContext(), "No characters — create one first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] names = chars.stream().map(c -> c.name).toArray(String[]::new);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Assign character to campaign")
                        .setItems(names, (dialog, which) -> viewModel.assignCharacter(chars.get(which).id))
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }).start();
    }

    //--------------------------LEVEL UP -----------------------------------------------------------------
    private void showLevelUpDialog() {
        CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
        if (cwr == null || cwr.character == null) {
            Toast.makeText(getContext(), "No character assigned", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CharacterClassAssignmentEntity> classes = cwr.classAssignments;
        if (classes == null) classes = new ArrayList<>();

        List<String> options = new ArrayList<>();
        List<String> classKeys = new ArrayList<>();

        for (CharacterClassAssignmentEntity ca : classes) {
            options.add(ca.classKey + " (level " + ca.level + ") → level " + (ca.level + 1));
            classKeys.add(ca.classKey);
        }
        options.add("➕ Add new class");
        classKeys.add(null);

        new AlertDialog.Builder(requireContext())
                .setTitle("Level up which class?")
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    if (which == options.size() - 1) {
                        showAddNewClassDialog(cwr.character.id);
                    } else {
                        String classKey = classKeys.get(which);
                        openLevelUpWizard(cwr.character.id, classKey, false);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddNewClassDialog(long characterId) {
        new Thread(() -> {
            if (!isAdded()) return;
            List<CharacterClassEntity> baseClasses = Open5eDatabase.getInstance(requireContext())
                    .characterClassDao().getBaseClassesByGameSystem(viewModel.getCurrentGameSystem());
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
                if (cwr == null || cwr.attributes == null) {
                    Toast.makeText(getContext(), "Cannot check prerequisites", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Integer> attrs = new HashMap<>();
                attrs.put("STR", cwr.attributes.strength);
                attrs.put("DEX", cwr.attributes.dexterity);
                attrs.put("CON", cwr.attributes.constitution);
                attrs.put("INT", cwr.attributes.intelligence);
                attrs.put("WIS", cwr.attributes.wisdom);
                attrs.put("CHA", cwr.attributes.charisma);

                List<String> currentClassKeys = new ArrayList<>();
                for (CharacterClassAssignmentEntity ca : cwr.classAssignments) {
                    currentClassKeys.add(ca.classKey);
                }

                List<CharacterClassEntity> availableClasses = new ArrayList<>();
                List<String> unavailableMessages = new ArrayList<>();
                for (CharacterClassEntity cls : baseClasses) {
                    String classKey = cls.key;
                    if (MulticlassPrerequisiteConfig.meetsMulticlassPrerequisites(currentClassKeys, classKey, attrs)) {
                        availableClasses.add(cls);
                    } else {
                        unavailableMessages.add(cls.name + " (" + MulticlassPrerequisiteConfig.getPrerequisiteMessage(classKey, attrs) + ")");
                    }
                }

                if (availableClasses.isEmpty()) {
                    StringBuilder sb = new StringBuilder("No new classes available due to ability score requirements:\n");
                    for (String msg : unavailableMessages) {
                        sb.append("• ").append(msg).append("\n");
                    }
                    Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_LONG).show();
                    return;
                }

                String[] classNames = availableClasses.stream().map(c -> c.name).toArray(String[]::new);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Choose new class")
                        .setItems(classNames, (dialog, which) -> {
                            String classKey = availableClasses.get(which).key;
                            openLevelUpWizard(characterId, classKey, true);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }).start();
    }

    private void openLevelUpWizard(long characterId, String classKey, boolean isNewClass) {
        LevelUpWizardFragment wizard = LevelUpWizardFragment.newInstance(characterId, classKey, isNewClass);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, wizard)
                .addToBackStack(null)
                .commit();
    }

    //--------------------------------------------------------------------------------------------------

    // ──────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────

    private CharacterEntity currentCharacter() {
        CharacterWithRelations cwr = viewModel.characterWithRelations.getValue();
        return (cwr != null) ? cwr.character : null;
    }

    private void setAttr(TextView tvVal, TextView tvMod, int value, int mod) {
        if (tvVal != null) tvVal.setText(String.valueOf(value));
        if (tvMod != null) tvMod.setText(fmtMod(mod));
    }

    private String fmtMod(int mod) {
        return mod >= 0 ? "+" + mod : String.valueOf(mod);
    }

    private String str(String s) {
        return s != null ? s : "?";
    }

    private String buildClassString(List<CharacterClassAssignmentEntity> assignments, int totalLevel) {
        if (assignments == null || assignments.isEmpty()) return "Level " + totalLevel;
        StringBuilder sb = new StringBuilder();
        for (CharacterClassAssignmentEntity ca : assignments) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(ca.classKey).append(" ").append(ca.level);
        }
        return sb + "  (total " + totalLevel + ")";
    }
}