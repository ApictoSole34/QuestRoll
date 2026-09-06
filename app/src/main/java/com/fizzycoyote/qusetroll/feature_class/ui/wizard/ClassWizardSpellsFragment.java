package com.fizzycoyote.qusetroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellEntity;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.SpellPickAdapter;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedSpell;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassWizardSpellsFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private RecyclerView rvSpells;
    private SpellPickAdapter adapter;
    private TextView tvSelectedCount;
    private Set<String> selectedKeys = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 🔥 POPRAWA – używamy poprawnej nazwy layoutu
        return inflater.inflate(R.layout.fragment_wizzard_class_spells, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ClassWizardViewModel.class);

        TextView tvHeader = view.findViewById(R.id.tv_spells_header);
        TextView tvHint = view.findViewById(R.id.tv_spells_hint);
        EditText etSearch = view.findViewById(R.id.et_spell_search);
        tvSelectedCount = view.findViewById(R.id.tv_selected_count);
        rvSpells = view.findViewById(R.id.rv_spells);

        if (tvHeader != null) {
            tvHeader.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
            tvHeader.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        }

        if (tvHint != null) {
            tvHint.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            tvHint.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
            tvHint.setText("NONE".equals(viewModel.casterType)
                    ? "This class has no spellcasting set, but you can still attach spells " +
                    "(e.g. for a minor spellcasting feature)."
                    : "Pick which spells this class can learn. Works with both official " +
                    "and your own custom spells.");
        }

        if (tvSelectedCount != null) {
            tvSelectedCount.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            tvSelectedCount.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        }

        selectedKeys = new HashSet<>(viewModel.spellKeys);

        if (rvSpells != null) {
            rvSpells.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                    if (adapter != null) adapter.filter(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        loadSpells();
    }

    private void loadSpells() {
        new Thread(() -> {
            List<SpellEntity> officialSpells = Open5eDatabase.getInstance(requireContext())
                    .spellDao().getAllSync();
            List<CustomSpellEntity> customSpells = UserContentDatabase.getInstance(requireContext())
                    .customSpellDao().getAllSync();

            List<CombinedSpell> combined = new ArrayList<>();
            if (officialSpells != null) {
                for (SpellEntity s : officialSpells) {
                    combined.add(new CombinedSpell(s.key, s.name, s.level, false));
                }
            }
            if (customSpells != null) {
                for (CustomSpellEntity s : customSpells) {
                    combined.add(new CombinedSpell("custom_" + s.id, s.name, s.level, true));
                }
            }
            combined.sort(Comparator
                    .comparingInt(CombinedSpell::getLevel)
                    .thenComparing(CombinedSpell::getName));

            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                adapter = new SpellPickAdapter(combined, selectedKeys,
                        (key, selected) -> updateSelectedCount());
                if (rvSpells != null) {
                    rvSpells.setAdapter(adapter);
                }
                updateSelectedCount();
            });
        }).start();
    }

    private void updateSelectedCount() {
        if (tvSelectedCount != null) {
            tvSelectedCount.setText(selectedKeys.size() + " spell"
                    + (selectedKeys.size() == 1 ? "" : "s") + " selected");
        }
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void saveData() {
        viewModel.spellKeys = new ArrayList<>(selectedKeys);
    }
}