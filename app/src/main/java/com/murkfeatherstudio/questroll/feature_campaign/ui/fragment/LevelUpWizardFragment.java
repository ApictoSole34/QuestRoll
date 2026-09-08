package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.spell.SpellEntity;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.LevelUpWizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class LevelUpWizardFragment extends Fragment {

    private static final String ARG_CHARACTER_ID = "character_id";
    private static final String ARG_CLASS_KEY    = "class_key";
    private static final String ARG_IS_NEW_CLASS = "is_new_class";

    private enum Step { HP, SUBCLASS, ASI, SPELLS, FEATURES }
    private final List<Step> steps = new ArrayList<>();
    private int currentStepIndex = 0;

    private LevelUpWizardViewModel viewModel;
    private List<FeatureEntity>        features   = new ArrayList<>();
    private List<CharacterClassEntity> subclasses = new ArrayList<>();

    private LinearLayout stepContainer;
    private TextView     tvStepTitle, tvStepHint;
    private Button       btnNext, btnBack, btnCancel;

    public static LevelUpWizardFragment newInstance(long characterId, String classKey, boolean isNewClass) {
        Bundle args = new Bundle();
        args.putLong(ARG_CHARACTER_ID, characterId);
        args.putString(ARG_CLASS_KEY, classKey);
        args.putBoolean(ARG_IS_NEW_CLASS, isNewClass);
        LevelUpWizardFragment f = new LevelUpWizardFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LevelUpWizardViewModel.class);
        Bundle args = requireArguments();
        viewModel.init(
                args.getLong(ARG_CHARACTER_ID),
                args.getString(ARG_CLASS_KEY),
                args.getBoolean(ARG_IS_NEW_CLASS)
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_level_up_wizard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stepContainer = view.findViewById(R.id.step_container);
        tvStepTitle   = view.findViewById(R.id.tv_step_title);
        tvStepHint    = view.findViewById(R.id.tv_step_hint);
        btnNext       = view.findViewById(R.id.btn_next);
        btnBack       = view.findViewById(R.id.btn_back);
        btnCancel     = view.findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> dismiss());
        btnBack.setOnClickListener(v -> goBack());
        btnNext.setOnClickListener(v -> goNext());

        viewModel.isLoading.observe(getViewLifecycleOwner(), loading ->
                btnNext.setEnabled(!Boolean.TRUE.equals(loading)));

        viewModel.initDone.observe(getViewLifecycleOwner(), done -> {
            if (!Boolean.TRUE.equals(done)) return;
            buildStepList();
            showCurrentStep();
        });

        viewModel.getAvailableSubclasses().observe(getViewLifecycleOwner(), subs -> {
            if (subs != null) subclasses = subs;
        });
        viewModel.getFeatures().observe(getViewLifecycleOwner(), feats -> {
            if (feats != null) features = feats;
        });

        viewModel.levelUpComplete.observe(getViewLifecycleOwner(), done -> {
            if (!Boolean.TRUE.equals(done)) return;
            Toast.makeText(requireContext(),
                    "Level up complete! Now level " + viewModel.newLevel + " " + viewModel.className + "!",
                    Toast.LENGTH_LONG).show();
            dismiss();
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), err -> {
            if (err == null || err.isEmpty()) return;
            Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
        });
    }

    private void buildStepList() {
        steps.clear();
        steps.add(Step.HP);
        if (viewModel.needsSubclass) steps.add(Step.SUBCLASS);
        if (viewModel.needsAsi) steps.add(Step.ASI);
        if (viewModel.needsSpells) steps.add(Step.SPELLS);
        steps.add(Step.FEATURES);
        currentStepIndex = 0;
        updateNavButtons();
    }

    private void goNext() {
        if (!validateCurrentStep()) return;
        currentStepIndex++;
        if (currentStepIndex >= steps.size()) {
            confirmLevelUp();
        } else {
            showCurrentStep();
        }
    }

    private void goBack() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            showCurrentStep();
        }
    }

    private void updateNavButtons() {
        if (btnBack != null) btnBack.setVisibility(currentStepIndex > 0 ? View.VISIBLE : View.GONE);
        if (btnNext != null) {
            Step current = steps.isEmpty() ? null : steps.get(currentStepIndex);
            boolean hideNext = (current == Step.SUBCLASS || current == Step.SPELLS || current == Step.FEATURES);
            btnNext.setVisibility(hideNext ? View.GONE : View.VISIBLE);
        }
    }

    private void showCurrentStep() {
        if (steps.isEmpty()) return;
        stepContainer.removeAllViews();
        Step step = steps.get(currentStepIndex);
        updateNavButtons();

        switch (step) {
            case HP:       showHpStep();       break;
            case SUBCLASS: showSubclassStep(); break;
            case ASI:      showAsiStep();      break;
            case SPELLS:   showSpellsStep();   break;
            case FEATURES: showFeaturesStep(); break;
        }
    }

    // ---------- HP Step ----------
    private void showHpStep() {
        if (tvStepTitle != null) tvStepTitle.setText("Step " + (currentStepIndex + 1) + " of " + steps.size() + " — Hit Points");
        if (tvStepHint != null) tvStepHint.setText("Roll your d" + viewModel.hitDiceSides + " or take the average.");

        LinearLayout inner = new LinearLayout(getContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView tvDie = new TextView(getContext());
        tvDie.setText("Hit die: d" + viewModel.hitDiceSides);
        tvDie.setTextSize(28f);
        tvDie.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvDie.setTypeface(null, android.graphics.Typeface.BOLD);
        tvDie.setPadding(0, 0, 0, dp(4));
        inner.addView(tvDie);

        TextView tvCon = new TextView(getContext());
        tvCon.setText("CON modifier: " + fmtMod(viewModel.conMod));
        tvCon.setTextSize(14f);
        tvCon.setTextColor(0xFF666666);
        tvCon.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvCon.setPadding(0, 0, 0, dp(20));
        inner.addView(tvCon);

        TextView tvResult = new TextView(getContext());
        tvResult.setTextSize(40f);
        tvResult.setTypeface(null, android.graphics.Typeface.BOLD);
        tvResult.setTextColor(0xFF388E3C);
        tvResult.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvResult.setPadding(0, dp(8), 0, dp(8));
        if (viewModel.chosenHpGain > 0) tvResult.setText("+" + viewModel.chosenHpGain + " HP");
        inner.addView(tvResult);

        TextView tvDetail = new TextView(getContext());
        tvDetail.setTextSize(13f);
        tvDetail.setTextColor(0xFF888888);
        tvDetail.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvDetail.setPadding(0, 0, 0, dp(24));
        inner.addView(tvDetail);

        Button btnRoll = new Button(getContext());
        btnRoll.setText("🎲  Roll d" + viewModel.hitDiceSides);
        btnRoll.setTextSize(16f);
        LinearLayout.LayoutParams rollLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rollLp.setMargins(0, 0, 0, dp(10));
        btnRoll.setLayoutParams(rollLp);
        btnRoll.setOnClickListener(btn -> {
            int gain = viewModel.rollHp();
            tvResult.setText("+" + gain + " HP");
            tvDetail.setText("Roll: " + viewModel.lastRollValue + (viewModel.conMod >= 0 ? " + " : " − ") + Math.abs(viewModel.conMod) + " (CON)");
        });
        inner.addView(btnRoll);

        Button btnAvg = new Button(getContext());
        btnAvg.setText("Take average  (⌀" + (int) Math.ceil(viewModel.hitDiceSides / 2.0) + ")");
        btnAvg.setTextSize(14f);
        btnAvg.setOnClickListener(btn -> {
            int gain = viewModel.averageHp();
            tvResult.setText("+" + gain + " HP");
            tvDetail.setText("Average: " + viewModel.lastRollValue + (viewModel.conMod >= 0 ? " + " : " − ") + Math.abs(viewModel.conMod) + " (CON)");
        });
        inner.addView(btnAvg);

        stepContainer.addView(inner);
    }

    // ---------- Subclass Step ----------
    private void showSubclassStep() {
        if (tvStepTitle != null) tvStepTitle.setText("Step " + (currentStepIndex + 1) + " of " + steps.size() + " — Choose Subclass");
        if (tvStepHint != null) tvStepHint.setText("Select your " + viewModel.className + " subclass (archetype).");

        LinearLayout inner = new LinearLayout(getContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(8), dp(8), dp(8), dp(8));

        RecyclerView rv = new RecyclerView(getContext());
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SubclassAdapter(subclasses, chosen -> {
            viewModel.setSelectedSubclass(chosen.key);
            currentStepIndex++;
            showCurrentStep();
        }, requireContext()));
        inner.addView(rv);
        stepContainer.addView(inner);
    }

    // ---------- ASI Step ----------
    private void showAsiStep() {
        if (tvStepTitle != null) tvStepTitle.setText("Step " + (currentStepIndex + 1) + " of " + steps.size() + " — Ability Score Improvement");
        if (tvStepHint != null) tvStepHint.setText("Distribute +2 among ability scores. Pick one stat (+2) or two stats (+1 each).");

        View v = LayoutInflater.from(getContext()).inflate(R.layout.step_level_up_asi, stepContainer, false);
        LinearLayout attrContainer = v.findViewById(R.id.attr_container);
        Button btnConfirm = v.findViewById(R.id.btn_confirm_asi);

        String[] statKeys   = {"strength","dexterity","constitution","intelligence","wisdom","charisma"};
        String[] statLabels = {"Strength","Dexterity","Constitution","Intelligence","Wisdom","Charisma"};
        CheckBox[] checks = new CheckBox[statKeys.length];
        for (int i = 0; i < statLabels.length; i++) {
            CheckBox cb = new CheckBox(getContext());
            cb.setText(statLabels[i]);
            cb.setTextSize(15f);
            cb.setPadding(dp(4), dp(8), 0, dp(8));
            attrContainer.addView(cb);
            checks[i] = cb;
        }

        btnConfirm.setOnClickListener(confirm -> {
            List<String> chosen = new ArrayList<>();
            int count = 0;
            for (int i = 0; i < checks.length; i++) {
                if (checks[i].isChecked()) { chosen.add(statKeys[i]); count++; }
            }
            if (count == 0) {
                Toast.makeText(getContext(), "Select at least one ability score.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (count > 2) {
                Toast.makeText(getContext(), "Select at most 2 ability scores.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (count == 1) chosen.add(chosen.get(0));

            viewModel.setAsiChoices(chosen);
            currentStepIndex++;
            if (currentStepIndex >= steps.size()) {
                confirmLevelUp();
            } else {
                showCurrentStep();
            }
        });
        stepContainer.addView(v);
    }

    // ---------- Spells Step ----------
    private void showSpellsStep() {
        if (tvStepTitle != null) tvStepTitle.setText("Step " + (currentStepIndex + 1) + " of " + steps.size() + " — Choose Spells");
        if (tvStepHint != null) tvStepHint.setText("Select " + viewModel.getSpellsToChoose().getValue() + " new spells.");

        View v = LayoutInflater.from(getContext()).inflate(R.layout.step_level_up_spells, stepContainer, false);
        RecyclerView rv = v.findViewById(R.id.rv_spells);
        Button btnConfirm = v.findViewById(R.id.btn_confirm_spells);

        final Integer toChoose = viewModel.getSpellsToChoose().getValue();
        final int needed = (toChoose == null) ? 0 : toChoose;

        final SpellSelectionAdapter adapter = new SpellSelectionAdapter(viewModel.getAvailableSpells().getValue(), needed);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        btnConfirm.setOnClickListener(btn -> {
            if (adapter.getSelectedCount() < needed) {
                Toast.makeText(getContext(), "Please select " + needed + " spells.", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.setSelectedSpells(adapter.getSelectedKeys());
            goNext();
        });

        stepContainer.addView(v);
    }

    // ---------- Features (Summary) Step ----------
    private void showFeaturesStep() {
        if (tvStepTitle != null) tvStepTitle.setText("Step " + (currentStepIndex + 1) + " of " + steps.size() + " — Summary");
        if (tvStepHint != null) tvStepHint.setText("Review and confirm your level up.");

        View v = LayoutInflater.from(getContext()).inflate(R.layout.step_level_up_features, stepContainer, false);
        TextView tvContent = v.findViewById(R.id.tv_features_content);

        StringBuilder sb = new StringBuilder();
        sb.append("🎉  ").append(viewModel.className).append("  →  Level ").append(viewModel.newLevel).append("\n\n");
        sb.append("❤  Max HP +").append(viewModel.chosenHpGain).append("\n\n");

        if (viewModel.chosenSubclassKey != null) {
            sb.append("📖  Subclass: ").append(viewModel.chosenSubclassKey).append("\n\n");
        }

        if (!viewModel.asiIncreases.isEmpty()) {
            sb.append("⬆  Ability Score Improvement:\n");
            java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
            for (String s : viewModel.asiIncreases) counts.merge(s, 1, Integer::sum);
            for (java.util.Map.Entry<String, Integer> e : counts.entrySet()) {
                sb.append("   • ").append(capitalize(e.getKey())).append(" +").append(e.getValue()).append("\n");
            }
            sb.append("\n");
        }

        if (!viewModel.getSelectedSpells().isEmpty()) {
            sb.append("✨  New spells:\n");
            for (String spellKey : viewModel.getSelectedSpells()) {
                sb.append("   • ").append(spellKey).append("\n");
            }
            sb.append("\n");
        }

        if (!features.isEmpty()) {
            sb.append("✨  New features:\n");
            for (FeatureEntity f : features) {
                sb.append("   • ").append(f.name != null ? f.name : "?").append("\n");
                if (f.desc != null && !f.desc.isEmpty()) {
                    String desc = f.desc.length() > 200 ? f.desc.substring(0, 200) + "…" : f.desc;
                    sb.append("       ").append(desc.replace("\n", "\n       ")).append("\n");
                }
                sb.append("\n");
            }
        } else {
            sb.append("No new class features at this level.\n");
        }

        tvContent.setText(sb.toString());

        Button btnConfirm = v.findViewById(R.id.btn_confirm_level_up);
        if (btnConfirm != null) {
            btnConfirm.setText("✅  Confirm Level Up!");
            btnConfirm.setOnClickListener(btn -> confirmLevelUp());
        }

        stepContainer.addView(v);
    }

    // ---------- Validation ----------
    private boolean validateCurrentStep() {
        if (steps.isEmpty()) return true;
        Step step = steps.get(currentStepIndex);
        switch (step) {
            case HP:
                if (viewModel.chosenHpGain == 0) {
                    Toast.makeText(getContext(), "Roll the die or take the average first.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case SUBCLASS:
                if (viewModel.chosenSubclassKey == null) {
                    Toast.makeText(getContext(), "Please choose a subclass.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case ASI:
            case SPELLS:
            case FEATURES:
                break;
        }
        return true;
    }

    private void confirmLevelUp() {
        if (viewModel.chosenHpGain == 0) viewModel.averageHp();
        viewModel.confirmLevelUp(null);
    }

    private void dismiss() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    // ---------- Helpers ----------
    private String fmtMod(int mod) { return mod >= 0 ? "+" + mod : String.valueOf(mod); }
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    private int dp(int value) {
        return Math.round(value * requireContext().getResources().getDisplayMetrics().density);
    }

    // ---------- Subclass Adapter ----------
    private static class SubclassAdapter extends RecyclerView.Adapter<SubclassAdapter.ViewHolder> {
        interface OnSelect { void onSelect(CharacterClassEntity item); }
        private final List<CharacterClassEntity> list;
        private final OnSelect listener;
        private final int selectableItemBackground;
        SubclassAdapter(List<CharacterClassEntity> list, OnSelect listener, Context context) {
            this.list = list;
            this.listener = listener;
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            selectableItemBackground = outValue.resourceId;
        }
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(48, 24, 48, 24);
            tv.setTextSize(16f);
            tv.setBackgroundResource(selectableItemBackground);
            tv.setClickable(true);
            tv.setFocusable(true);
            return new ViewHolder(tv);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CharacterClassEntity item = list.get(position);
            holder.textView.setText(item.name != null ? item.name : "?");
            holder.textView.setOnClickListener(v -> listener.onSelect(item));
        }
        @Override public int getItemCount() { return list.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(TextView v) { super(v); textView = v; }
        }
    }

    // ---------- Spell Selection Adapter ----------
    private static class SpellSelectionAdapter extends RecyclerView.Adapter<SpellSelectionAdapter.ViewHolder> {
        private final List<SpellEntity> spells;
        private final int maxSelection;
        private final boolean[] selected;
        private final List<String> selectedKeys = new ArrayList<>();

        SpellSelectionAdapter(List<SpellEntity> spells, int maxSelection) {
            this.spells = spells != null ? spells : new ArrayList<>();
            this.maxSelection = maxSelection;
            this.selected = new boolean[this.spells.size()];
        }

        int getSelectedCount() { return selectedKeys.size(); }
        List<String> getSelectedKeys() { return new ArrayList<>(selectedKeys); }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CheckBox cb = new CheckBox(parent.getContext());
            cb.setPadding(32, 16, 32, 16);
            cb.setTextSize(14f);
            return new ViewHolder(cb);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SpellEntity spell = spells.get(position);
            holder.checkBox.setText(spell.name != null ? spell.name : spell.key);
            holder.checkBox.setChecked(selected[position]);
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && selectedKeys.size() >= maxSelection) {
                    buttonView.setChecked(false);
                    Toast.makeText(buttonView.getContext(), "You can select at most " + maxSelection + " spells.", Toast.LENGTH_SHORT).show();
                    return;
                }
                selected[position] = isChecked;
                if (isChecked) {
                    selectedKeys.add(spell.key);
                } else {
                    selectedKeys.remove(spell.key);
                }
            });
        }
        @Override public int getItemCount() { return spells.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            ViewHolder(CheckBox v) { super(v); checkBox = v; }
        }
    }
}