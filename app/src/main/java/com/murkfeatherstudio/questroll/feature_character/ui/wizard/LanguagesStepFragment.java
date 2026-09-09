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
import com.murkfeatherstudio.questroll.core.models.custom.custom_language.CustomLanguageEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardLanguagesBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LanguagesStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private FragmentWizardLanguagesBinding binding;
    private List<Object> allLanguages = new ArrayList<>();
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private int maxSelections = 0;
    private Set<String> alreadyKnown = new HashSet<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardLanguagesBinding.inflate(inflater, container, false);
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

        alreadyKnown.addAll(viewModel.racialFixedLanguages);
        alreadyKnown.addAll(viewModel.backgroundFixedLanguages);
        alreadyKnown.addAll(viewModel.classFixedLanguages);

        maxSelections = viewModel.backgroundLanguageChoices
                + viewModel.bonusLanguagesFromInt
                + viewModel.classLanguageChoices;

        loadLanguages();

        binding.nextButton.setOnClickListener(v -> {
            viewModel.chosenBonusLanguages.clear();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    Object item = allLanguages.get(i);
                    String key = (item instanceof LanguageEntity) ? ((LanguageEntity) item).key
                            : "custom_" + ((CustomLanguageEntity) item).id;
                    viewModel.chosenBonusLanguages.add(key);
                }
            }
            if (viewModel.chosenBonusLanguages.size() > maxSelections) {
                Toast.makeText(getContext(), "You can select only " + maxSelections + " languages", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void loadLanguages() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;

            List<LanguageEntity> standardLangs = Open5eDatabase.getInstance(requireContext())
                    .languageDao().getAllSync();
            List<CustomLanguageEntity> customLangs = UserContentDatabase.getInstance(requireContext())
                    .customLanguageDao().getAll();

            Map<String, String> keyToName = new HashMap<>();
            for (LanguageEntity lang : standardLangs) {
                keyToName.put(lang.key, lang.name);
            }
            for (CustomLanguageEntity cl : customLangs) {
                keyToName.put("custom_" + cl.id, cl.name);
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

            allLanguages.clear();
            allLanguages.addAll(standardLangs);
            allLanguages.addAll(customLangs);
            allLanguages.sort((a, b) -> {
                String nameA = (a instanceof LanguageEntity) ? ((LanguageEntity) a).name : ((CustomLanguageEntity) a).name;
                String nameB = (b instanceof LanguageEntity) ? ((LanguageEntity) b).name : ((CustomLanguageEntity) b).name;
                return nameA.compareTo(nameB);
            });

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;
                binding.languagesContainer.removeAllViews();
                checkBoxes.clear();

                if (viewModel.backgroundLanguagesDescription != null && !viewModel.backgroundLanguagesDescription.isEmpty()) {
                    TextView descView = new TextView(getContext());
                    descView.setText(viewModel.backgroundLanguagesDescription);
                    descView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                    descView.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
                    descView.setPadding(0, dp(8), 0, dp(8));
                    binding.languagesContainer.addView(descView);
                }

                TextView knownHeader = new TextView(getContext());
                knownHeader.setText("Known languages (from race, background and class):");
                knownHeader.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
                knownHeader.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                knownHeader.setPadding(0, dp(16), 0, dp(8));
                binding.languagesContainer.addView(knownHeader);

                if (knownNames.isEmpty()) {
                    TextView none = new TextView(getContext());
                    none.setText("None");
                    none.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                    none.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
                    binding.languagesContainer.addView(none);
                } else {
                    for (String langName : knownNames) {
                        TextView tv = new TextView(getContext());
                        tv.setText("• " + langName);
                        tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        tv.setPadding(dp(32), dp(4), 0, dp(4));
                        binding.languagesContainer.addView(tv);
                    }
                }

                TextView sourceInfo = new TextView(getContext());
                sourceInfo.setText(String.format(
                        "From background you can choose: %d language(s)\nFrom Intelligence: +%d language(s)\nFrom class: +%d language(s)\nTotal to choose: %d",
                        viewModel.backgroundLanguageChoices,
                        viewModel.bonusLanguagesFromInt,
                        viewModel.classLanguageChoices,
                        maxSelections));
                sourceInfo.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                sourceInfo.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
                sourceInfo.setPadding(0, dp(16), 0, dp(8));
                binding.languagesContainer.addView(sourceInfo);

                if (maxSelections > 0) {
                    TextView selectHeader = new TextView(getContext());
                    selectHeader.setText("Select additional languages (max " + maxSelections + "):");
                    selectHeader.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
                    selectHeader.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                    selectHeader.setPadding(0, dp(24), 0, dp(8));
                    binding.languagesContainer.addView(selectHeader);

                    for (Object obj : allLanguages) {
                        String name = (obj instanceof LanguageEntity) ? ((LanguageEntity) obj).name : ((CustomLanguageEntity) obj).name;
                        if (knownNames.contains(name)) continue;
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(name);
                        cb.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        cb.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        cb.setTag(obj);
                        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked && getCheckedCount() > maxSelections) {
                                cb.setChecked(false);
                                Toast.makeText(getContext(), "You can select only " + maxSelections + " languages", Toast.LENGTH_SHORT).show();
                            }
                        });
                        binding.languagesContainer.addView(cb);
                        checkBoxes.add(cb);
                    }
                } else {
                    TextView info = new TextView(getContext());
                    info.setText("No additional languages to choose.");
                    info.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                    info.setTextColor(getResources().getColor(R.color.threads_text_secondary, null));
                    info.setPadding(0, dp(16), 0, 0);
                    binding.languagesContainer.addView(info);
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