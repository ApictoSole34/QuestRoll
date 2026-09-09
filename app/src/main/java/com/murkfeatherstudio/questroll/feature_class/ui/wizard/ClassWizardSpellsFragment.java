package com.murkfeatherstudio.questroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.spell.SpellEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizzardClassSpellsBinding;
import com.murkfeatherstudio.questroll.feature_class.class_adapter.SpellPickAdapter;
import com.murkfeatherstudio.questroll.feature_class.model.CombinedSpell;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassWizardSpellsFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private SpellPickAdapter adapter;
    private FragmentWizzardClassSpellsBinding binding;
    private Set<String> selectedKeys = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWizzardClassSpellsBinding.inflate(inflater, container, false);
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
        viewModel = new ViewModelProvider(requireActivity()).get(ClassWizardViewModel.class);

        binding.tvSpellsHeader.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
        binding.tvSpellsHeader.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        binding.tvSpellsHint.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.tvSpellsHint.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.tvSpellsHint.setText("NONE".equals(viewModel.casterType)
                ? "This class has no spellcasting set, but you can still attach spells " +
                "(e.g. for a minor spellcasting feature)."
                : "Pick which spells this class can learn. Works with both official " +
                "and your own custom spells.");

        binding.tvSelectedCount.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.tvSelectedCount.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        selectedKeys = new HashSet<>(viewModel.spellKeys);

        binding.rvSpells.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.etSpellSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (adapter != null) adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadSpells();
    }

    private void loadSpells() {
        AppExecutors.getInstance().diskIO().execute(() -> {
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

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;

                adapter = new SpellPickAdapter(combined, selectedKeys,
                        (key, selected) -> updateSelectedCount());
                binding.rvSpells.setAdapter(adapter);
                updateSelectedCount();
            });
        });
    }

    private void updateSelectedCount() {
        if (binding != null) {
            binding.tvSelectedCount.setText(selectedKeys.size() + " spell"
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