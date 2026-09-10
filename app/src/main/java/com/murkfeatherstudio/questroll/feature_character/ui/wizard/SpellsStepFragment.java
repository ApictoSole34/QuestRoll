package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomSpellEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.spell.SpellEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardSpellsBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class SpellsStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private FragmentWizardSpellsBinding binding;
    private List<CheckBox> cantripCheckboxes = new ArrayList<>();
    private List<CheckBox> spellCheckboxes = new ArrayList<>();
    private int maxCantrips = 0;
    private int maxSpells = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardSpellsBinding.inflate(inflater, container, false);
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

        maxCantrips = viewModel.cantripsCount;
        if (viewModel.isPreparedCaster) {
            maxSpells = viewModel.spellcastingAbilityMod + 1;
        } else {
            maxSpells = viewModel.spellsKnownCount;
        }

        if (maxCantrips == 0 && maxSpells == 0) {
            Navigation.findNavController(view).navigate(R.id.next_action);
            return;
        }

        loadSpells();

        binding.nextButton.setOnClickListener(v -> {
            viewModel.chosenCantripKeys.clear();
            for (CheckBox cb : cantripCheckboxes) {
                if (cb.isChecked()) {
                    viewModel.chosenCantripKeys.add((String) cb.getTag());
                }
            }
            viewModel.chosenSpellKeys.clear();
            for (CheckBox cb : spellCheckboxes) {
                if (cb.isChecked()) {
                    viewModel.chosenSpellKeys.add((String) cb.getTag());
                }
            }
            if (viewModel.chosenCantripKeys.size() > maxCantrips) {
                Toast.makeText(getContext(), "You can select only " + maxCantrips + " cantrips", Toast.LENGTH_SHORT).show();
                return;
            }
            if (viewModel.chosenSpellKeys.size() > maxSpells) {
                Toast.makeText(getContext(), "You can select only " + maxSpells + " 1st-level spells", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void loadSpells() {
        new Thread(() -> {
            // 1. Get spells for the currently selected game system (official compendium)
            List<SpellEntity> systemSpells = Open5eDatabase.getInstance(requireContext())
                    .spellDao()
                    .getAllByGameSystem(viewModel.gameSystem);
            
            // 2. Get Custom spells for the same game system
            List<CustomSpellEntity> customSpells = UserContentDatabase.getInstance(requireContext())
                    .customSpellDao()
                    .getAllByGameSystemSync(viewModel.gameSystem);

            List<SpellEntity> combined = new ArrayList<>();
            if (systemSpells != null) combined.addAll(systemSpells);
            
            if (customSpells != null) {
                for (CustomSpellEntity cs : customSpells) {
                    SpellEntity se = new SpellEntity();
                    se.key = "custom_" + cs.id;
                    se.name = cs.name + " (Custom)";
                    se.level = cs.level;
                    // For custom spells, we make them available to the current class in the picker
                    se.classes = new ArrayList<>();
                    if (viewModel.classAssignments.size() > 0) {
                        se.classes.add(viewModel.classAssignments.get(0).className);
                    }
                    combined.add(se);
                }
            }

            if (viewModel.classAssignments.isEmpty()) return;
            String className = viewModel.classAssignments.get(0).className;

            List<SpellEntity> cantrips = new ArrayList<>();
            List<SpellEntity> firstLevelSpells = new ArrayList<>();

            for (SpellEntity spell : combined) {
                if (spell.classes != null && spell.classes.stream().anyMatch(c -> c.equalsIgnoreCase(className))) {
                    if (spell.level == 0) {
                        cantrips.add(spell);
                    } else if (spell.level == 1) {
                        firstLevelSpells.add(spell);
                    }
                }
            }

            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                binding.spellsContainer.removeAllViews();
                cantripCheckboxes.clear();
                spellCheckboxes.clear();

                if (cantrips.isEmpty() && firstLevelSpells.isEmpty()) {
                    TextView info = new TextView(getContext());
                    info.setText("No spells available for this class in " + viewModel.gameSystem + ".");
                    info.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                    info.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
                    info.setPadding(0, dp(16), 0, 0);
                    binding.spellsContainer.addView(info);
                    return;
                }

                if (maxCantrips > 0 && !cantrips.isEmpty()) {
                    TextView header = new TextView(getContext());
                    header.setText("Select cantrips (max " + maxCantrips + "):");
                    header.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
                    header.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                    header.setPadding(0, dp(16), 0, dp(8));
                    binding.spellsContainer.addView(header);

                    for (SpellEntity spell : cantrips) {
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(spell.name);
                        cb.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        cb.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        cb.setTag(spell.key);
                        binding.spellsContainer.addView(cb);
                        cantripCheckboxes.add(cb);
                    }
                }

                if (maxSpells > 0 && !firstLevelSpells.isEmpty()) {
                    TextView header = new TextView(getContext());
                    header.setText("Select 1st-level spells (max " + maxSpells + "):");
                    header.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
                    header.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                    header.setPadding(0, dp(24), 0, dp(8));
                    binding.spellsContainer.addView(header);

                    for (SpellEntity spell : firstLevelSpells) {
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(spell.name);
                        cb.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        cb.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        cb.setTag(spell.key);
                        binding.spellsContainer.addView(cb);
                        spellCheckboxes.add(cb);
                    }
                }
            });
        }).start();
    }

    private int getCheckedCount(List<CheckBox> boxes) {
        int count = 0;
        for (CheckBox cb : boxes) if (cb.isChecked()) count++;
        return count;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
