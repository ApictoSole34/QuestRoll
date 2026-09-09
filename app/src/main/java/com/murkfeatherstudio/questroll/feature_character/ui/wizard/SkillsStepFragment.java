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
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_ability.CustomSkillEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.ability.skill.SkillEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardSkillsBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SkillsStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private FragmentWizardSkillsBinding binding;
    private List<Object> allSkills = new ArrayList<>();
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private int maxSelections = 0;
    private Set<String> alreadyKnown = new HashSet<>();
    private Set<String> allowedSkillNames = new HashSet<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardSkillsBinding.inflate(inflater, container, false);
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

        alreadyKnown.addAll(viewModel.backgroundSkillProficiencies);
        maxSelections = viewModel.classSkillChoices;

        allowedSkillNames.clear();
        allowedSkillNames.addAll(viewModel.classSkillOptions);

        loadSkills();

        binding.nextButton.setOnClickListener(v -> {
            viewModel.chosenSkillProficiencies.clear();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    Object item = allSkills.get(i);
                    String key = (item instanceof SkillEntity) ? ((SkillEntity) item).key
                            : "custom_" + ((CustomSkillEntity) item).id;
                    viewModel.chosenSkillProficiencies.add(key);
                }
            }
            if (viewModel.chosenSkillProficiencies.size() > maxSelections) {
                Toast.makeText(getContext(), "You can select only " + maxSelections + " skills", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    /**
     * JAVADOC: skillsContainer is a dynamic layout populated at runtime.
     * We use addView() to insert TextViews for known skills and CheckBoxes 
     * for selectable skills. Since these views are generated programmatically 
     * based on database content and are not defined in the XML, 
     * View Binding cannot be used to reference them.
     */
    private void loadSkills() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;

            List<SkillEntity> standardSkills = Open5eDatabase.getInstance(requireContext())
                    .skillDao().getAllSync();
            List<CustomSkillEntity> customSkills = UserContentDatabase.getInstance(requireContext())
                    .customSkillDao().getAllSync();

            Map<String, String> keyToName = new HashMap<>();
            for (SkillEntity s : standardSkills) {
                keyToName.put(s.key, s.name);
            }
            for (CustomSkillEntity cs : customSkills) {
                keyToName.put("custom_" + cs.id, cs.name);
            }

            Set<String> knownNames = new HashSet<>();
            for (String item : alreadyKnown) {
                String name = keyToName.get(item);
                if (name != null) {
                    knownNames.add(name);
                } else {
                    knownNames.add(item);
                }
            }

            allSkills.clear();
            allSkills.addAll(standardSkills);
            allSkills.addAll(customSkills);
            allSkills.sort((a, b) -> {
                String nameA = (a instanceof SkillEntity) ? ((SkillEntity) a).name : ((CustomSkillEntity) a).name;
                String nameB = (b instanceof SkillEntity) ? ((SkillEntity) b).name : ((CustomSkillEntity) b).name;
                return nameA.compareTo(nameB);
            });

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;
                binding.skillsContainer.removeAllViews();
                checkBoxes.clear();

                TextView knownHeader = new TextView(getContext());
                knownHeader.setText("Skills already known (from background):");
                knownHeader.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
                knownHeader.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                knownHeader.setPadding(0, dp(16), 0, dp(8));
                binding.skillsContainer.addView(knownHeader);

                if (knownNames.isEmpty()) {
                    TextView none = new TextView(getContext());
                    none.setText("None");
                    none.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                    none.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
                    binding.skillsContainer.addView(none);
                } else {
                    for (String skillName : knownNames) {
                        TextView tv = new TextView(getContext());
                        tv.setText("• " + skillName);
                        tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        tv.setPadding(dp(32), dp(4), 0, dp(4));
                        binding.skillsContainer.addView(tv);
                    }
                }

                if (maxSelections > 0) {
                    TextView selectHeader = new TextView(getContext());
                    selectHeader.setText("Select skills from class (max " + maxSelections + "):");
                    selectHeader.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
                    selectHeader.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                    selectHeader.setPadding(0, dp(24), 0, dp(8));
                    binding.skillsContainer.addView(selectHeader);

                    for (Object obj : allSkills) {
                        String name = (obj instanceof SkillEntity) ? ((SkillEntity) obj).name : ((CustomSkillEntity) obj).name;
                        if (knownNames.contains(name)) continue;
                        if (!allowedSkillNames.isEmpty() && !allowedSkillNames.contains(name)) continue;
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(name);
                        cb.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        cb.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        cb.setTag(obj);
                        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked && getCheckedCount() > maxSelections) {
                                cb.setChecked(false);
                                Toast.makeText(getContext(), "You can select only " + maxSelections + " skills", Toast.LENGTH_SHORT).show();
                            }
                        });
                        binding.skillsContainer.addView(cb);
                        checkBoxes.add(cb);
                    }

                    if (checkBoxes.isEmpty()) {
                        TextView info = new TextView(getContext());
                        info.setText("No eligible skills to choose from class.");
                        info.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        info.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
                        info.setPadding(0, dp(16), 0, 0);
                        binding.skillsContainer.addView(info);
                    }
                } else {
                    TextView info = new TextView(getContext());
                    info.setText("No additional skills to choose.");
                    info.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                    info.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
                    info.setPadding(0, dp(16), 0, 0);
                    binding.skillsContainer.addView(info);
                }
            });
        });
    }

    private int getCheckedCount() {
        int count = 0;
        for (CheckBox cb : checkBoxes) if (cb.isChecked()) count++;
        return count;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}