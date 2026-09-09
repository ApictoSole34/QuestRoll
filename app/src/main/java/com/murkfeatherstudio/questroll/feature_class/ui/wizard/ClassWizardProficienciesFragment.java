package com.murkfeatherstudio.questroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.skill.SkillEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardProficienciesBinding;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassWizardProficienciesFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private FragmentWizardProficienciesBinding binding;

    private List<CheckBox> savingThrowCheckboxes = new ArrayList<>();
    private List<CheckBox> skillOptionCheckboxes = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWizardProficienciesBinding.inflate(inflater, container, false);
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

        binding.etSkillChoices.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.etSkillChoices.setText(String.valueOf(viewModel.skillChoicesCount));

        loadSavingThrows();
        loadSkillOptions();
    }

    /**
     * JAVADOC: savingThrowsContainer is a dynamic container. We use removeAllViews() 
     * and addView() to add CheckBoxes programmatically because the list of abilities 
     * is loaded from the database at runtime. View Binding cannot be used for these 
     * dynamic children.
     */
    private void loadSavingThrows() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<AbilityEntity> abilities = Open5eDatabase.getInstance(requireContext())
                    .abilityDao().getAllSync();

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;
                binding.savingThrowsContainer.removeAllViews();
                savingThrowCheckboxes.clear();

                for (AbilityEntity ability : abilities) {
                    CheckBox cb = new CheckBox(requireContext());
                    cb.setText(ability.name);
                    cb.setTag(ability.key);
                    cb.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                    cb.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

                    if (viewModel.savingThrows.contains(ability.key)) {
                        cb.setChecked(true);
                    }

                    binding.savingThrowsContainer.addView(cb);
                    savingThrowCheckboxes.add(cb);
                }
            });
        });
    }

    /**
     * JAVADOC: skillOptionsContainer is a dynamic layout managed via addView(). 
     * CheckBoxes are created programmatically based on the standard and custom skills 
     * available in the database. Since these views do not exist in the XML layout, 
     * View Binding is not applicable.
     */
    private void loadSkillOptions() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<SkillEntity> standardSkills = Open5eDatabase.getInstance(requireContext())
                    .skillDao().getAllSync();
            List<CustomSkillEntity> customSkills = UserContentDatabase.getInstance(requireContext())
                    .customSkillDao().getAllSync();

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;
                binding.skillOptionsContainer.removeAllViews();
                skillOptionCheckboxes.clear();

                for (SkillEntity skill : standardSkills) {
                    addSkillCheckbox(skill.name, skill.key);
                }
                for (CustomSkillEntity skill : customSkills) {
                    addSkillCheckbox(skill.name, "custom_" + skill.id);
                }
            });
        });
    }

    private void addSkillCheckbox(String name, String key) {
        CheckBox cb = new CheckBox(requireContext());
        cb.setText(name);
        cb.setTag(key);
        cb.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        cb.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        if (viewModel.skillOptions.contains(key)) {
            cb.setChecked(true);
        }

        binding.skillOptionsContainer.addView(cb);
        skillOptionCheckboxes.add(cb);
    }

    @Override
    public boolean validate() {
        if (binding == null) return false;
        String choices = binding.etSkillChoices.getText().toString().trim();
        if (choices.isEmpty()) {
            binding.etSkillChoices.setError("Number of skill choices is required");
            return false;
        }
        return true;
    }

    @Override
    public void saveData() {
        if (binding == null) return;
        Set<String> savingThrows = new HashSet<>();
        for (CheckBox cb : savingThrowCheckboxes) {
            if (cb.isChecked() && cb.getTag() != null) {
                savingThrows.add((String) cb.getTag());
            }
        }
        viewModel.savingThrows = savingThrows;

        try {
            viewModel.skillChoicesCount = Integer.parseInt(binding.etSkillChoices.getText().toString());
        } catch (NumberFormatException e) {
            viewModel.skillChoicesCount = 0;
        }

        viewModel.skillOptions.clear();
        for (CheckBox cb : skillOptionCheckboxes) {
            if (cb.isChecked() && cb.getTag() != null) {
                viewModel.skillOptions.add((String) cb.getTag());
            }
        }
    }
}