package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassWithFeatures;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardSubclassBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;

public class SubclassChoiceStepFragment extends Fragment {

    private WizardViewModel viewModel;
    private FragmentWizardSubclassBinding binding;
    private List<Object> availableSubclasses = new ArrayList<>();
    private Markwon markwon;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardSubclassBinding.inflate(inflater, container, false);
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
        markwon = Markwon.create(requireContext());

        loadSubclasses();

        binding.subclassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < availableSubclasses.size()) {
                    Object selected = availableSubclasses.get(position);
                    viewModel.chosenSubclassKey = getKey(selected);
                    loadSubclassTraits(selected);
                    showDescription(selected);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.nextButton.setOnClickListener(v -> {
            if (viewModel.chosenSubclassKey == null) {
                Toast.makeText(getContext(), "Please select a subclass", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.next_action);
        });

        binding.subclassDescription.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        binding.subclassDescription.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadSubclasses() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;

            if (viewModel.classAssignments.isEmpty()) return;

            String classKey   = viewModel.classAssignments.get(0).classKey;
            String gameSystem = viewModel.gameSystem;

            List<CharacterClassEntity> standardSubs = new ArrayList<>();
            List<CustomCharacterClassEntity> customSubs = new ArrayList<>();

            if (classKey.startsWith("custom_")) {
                try {
                    long id = Long.parseLong(classKey.substring(7));
                    CustomCharacterClassEntity parent = UserContentDatabase.getInstance(requireContext())
                            .customCharacterClassDao().getClassByIdSync(id);
                    if (parent != null) {
                        String parentKey = "custom_" + parent.id;
                        customSubs = UserContentDatabase.getInstance(requireContext())
                                .customCharacterClassDao()
                                .getSubclassesByParentKeySync(parentKey, gameSystem);
                    }
                } catch (NumberFormatException ignored) {}
            } else {
                standardSubs = Open5eDatabase.getInstance(requireContext())
                        .characterClassDao().getSubclassesByParentKeySync(classKey);
            }

            availableSubclasses.clear();
            availableSubclasses.addAll(standardSubs);
            availableSubclasses.addAll(customSubs);
            availableSubclasses.sort((a, b) -> getName(a).compareTo(getName(b)));

            if (!isAdded()) return;

            final List<Object> finalList = new ArrayList<>(availableSubclasses);

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;

                if (finalList.isEmpty()) {
                    Toast.makeText(getContext(),
                            "No subclasses available for this class — skipping",
                            Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.next_action);
                    return;
                }

                ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        finalList) {
                    @NonNull
                    @Override
                    public View getView(int pos, View cv, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getView(pos, cv, parent);
                        tv.setText(getName(getItem(pos)));
                        tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return tv;
                    }
                    @Override
                    public View getDropDownView(int pos, View cv, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getDropDownView(pos, cv, parent);
                        tv.setText(getName(getItem(pos)));
                        tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return tv;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.subclassSpinner.setAdapter(adapter);

                if (viewModel.chosenSubclassKey != null) {
                    for (int i = 0; i < finalList.size(); i++) {
                        if (viewModel.chosenSubclassKey.equals(getKey(finalList.get(i)))) {
                            binding.subclassSpinner.setSelection(i);
                            return;
                        }
                    }
                }
                binding.subclassSpinner.setSelection(0);
            });
        });
    }

    private void loadSubclassTraits(Object subclassObj) {
        String subclassKey = getKey(subclassObj);
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            List<CharacterTraitEntity> subclassTraits = new ArrayList<>();
            int order = 0;

            if (subclassObj instanceof CharacterClassEntity) {
                List<FeatureEntity> features = Open5eDatabase.getInstance(requireContext())
                        .featureDao().getFeaturesForClassSync(subclassKey);
                for (FeatureEntity f : features) {
                    if (f.featureType != null && ("STARTING_EQUIPMENT".equals(f.featureType) || "PROFICIENCIES".equals(f.featureType))) continue;
                    boolean available = false;
                    if (f.gainedAt != null) {
                        for (var gained : f.gainedAt) {
                            if (gained.level <= 1) { available = true; break; }
                        }
                    } else { available = true; }
                    
                    if (available) {
                        CharacterTraitEntity t = new CharacterTraitEntity();
                        t.sourceType = "SUBCLASS";
                        t.sourceKey = subclassKey;
                        t.name = f.name;
                        t.description = f.desc;
                        t.levelRequirement = 1;
                        t.displayOrder = order++;
                        subclassTraits.add(t);
                    }
                }
            } else if (subclassObj instanceof CustomCharacterClassEntity) {
                long customId = ((CustomCharacterClassEntity) subclassObj).id;
                CustomCharacterClassWithFeatures data = UserContentDatabase.getInstance(requireContext())
                        .customCharacterClassDao().getClassWithFeaturesSync(customId);
                if (data != null && data.features != null) {
                    for (CustomFeatureEntity f : data.features) {
                        if ("STARTING_EQUIPMENT".equals(f.type) || "PROFICIENCIES".equals(f.type)) continue;
                        boolean available = false;
                        if (f.customGainedAt != null && !f.customGainedAt.isEmpty()) {
                            for (CustomGainedAt gained : f.customGainedAt) {
                                if (gained.level <= 1) { available = true; break; }
                            }
                        } else { available = true; }

                        if (available) {
                            CharacterTraitEntity t = new CharacterTraitEntity();
                            t.sourceType = "SUBCLASS";
                            t.sourceKey = subclassKey;
                            t.name = f.name;
                            t.description = f.description;
                            t.levelRequirement = 1;
                            t.displayOrder = order++;
                            subclassTraits.add(t);
                        }
                    }
                }
            }

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;
                // FIX: Remove only SUBCLASS traits, keeping CLASS and RACE traits intact
                viewModel.characterTraits.removeIf(t -> "SUBCLASS".equals(t.sourceType));
                viewModel.characterTraits.addAll(subclassTraits);
            });
        });
    }

    private String getName(Object obj) {
        if (obj instanceof CharacterClassEntity)       return ((CharacterClassEntity) obj).name;
        if (obj instanceof CustomCharacterClassEntity) return ((CustomCharacterClassEntity) obj).name;
        return "";
    }

    private String getKey(Object obj) {
        if (obj instanceof CharacterClassEntity)       return ((CharacterClassEntity) obj).key;
        if (obj instanceof CustomCharacterClassEntity) return "custom_" + ((CustomCharacterClassEntity) obj).id;
        return "";
    }

    private void showDescription(Object obj) {
        if (binding == null) return;
        String descText = "";
        if (obj instanceof CharacterClassEntity) {
            CharacterClassEntity cls = (CharacterClassEntity) obj;
            descText = "**" + cls.name + "**";
            if (cls.casterType != null && !cls.casterType.isEmpty())
                descText += "\n\n**Spellcasting:** " + cls.casterType;
        } else if (obj instanceof CustomCharacterClassEntity) {
            CustomCharacterClassEntity custom = (CustomCharacterClassEntity) obj;
            descText = (custom.description != null) ? custom.description : custom.name;
        }
        
        if (markwon != null) {
            markwon.setMarkdown(binding.subclassDescription, !descText.isEmpty() ? descText : "No description available.");
        } else {
            binding.subclassDescription.setText(!descText.isEmpty() ? descText : "No description available.");
        }
    }
}
