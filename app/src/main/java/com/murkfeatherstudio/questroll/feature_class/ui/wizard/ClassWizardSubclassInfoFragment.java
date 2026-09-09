package com.murkfeatherstudio.questroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardSubclassInfoBinding;
import com.murkfeatherstudio.questroll.feature_class.model.CombinedClass;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class ClassWizardSubclassInfoFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private FragmentWizardSubclassInfoBinding binding;
    private List<CombinedClass> parentClasses = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWizardSubclassInfoBinding.inflate(inflater, container, false);
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

        binding.cbIsSubclass.setChecked(viewModel.isSubclass);
        binding.parentClassContainer.setVisibility(viewModel.isSubclass ? View.VISIBLE : View.GONE);

        binding.cbIsSubclass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.isSubclass = isChecked;
            binding.parentClassContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                viewModel.parentClassKey = null;
                viewModel.parentClassName = null;
            }
        });

        loadParentClasses();
    }

    private void loadParentClasses() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;

            // Fetch base classes (not subclasses) from Open5e
            List<CharacterClassEntity> open5eClasses = Open5eDatabase.getInstance(requireContext())
                    .characterClassDao().getBaseClassesByGameSystem(viewModel.gameSystem);

            // Fetch custom base classes
            List<CustomCharacterClassEntity> customClasses = UserContentDatabase.getInstance(requireContext())
                    .customCharacterClassDao().getBaseClassesSync(viewModel.gameSystem);

            parentClasses.clear();

            for (CharacterClassEntity cls : open5eClasses) {
                if (cls.subclassOfKey == null || cls.subclassOfKey.isEmpty()) {
                    parentClasses.add(new CombinedClass(
                            cls.key,
                            cls.name,
                            false,
                            null,
                            null,
                            viewModel.gameSystem
                    ));
                }
            }

            for (CustomCharacterClassEntity cls : customClasses) {
                if (cls.subclassOf == null || cls.subclassOf.isEmpty()) {
                    parentClasses.add(new CombinedClass(
                            "custom_" + cls.id,
                            cls.name,
                            true,
                            null,
                            null,
                            cls.gameSystem
                    ));
                }
            }

            parentClasses.sort((a, b) -> a.getName().compareTo(b.getName()));

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;

                ArrayAdapter<CombinedClass> adapter = new ArrayAdapter<CombinedClass>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        parentClasses
                ) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getView(position, convertView, parent);
                        CombinedClass item = getItem(position);
                        tv.setText(item != null ? item.getName() : "");
                        tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return tv;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                        CombinedClass item = getItem(position);
                        tv.setText(item != null ? item.getName() : "");
                        tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return tv;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerParentClass.setAdapter(adapter);

                // Restore previous selection
                if (viewModel.parentClassKey != null) {
                    for (int i = 0; i < parentClasses.size(); i++) {
                        if (parentClasses.get(i).getKey().equals(viewModel.parentClassKey)) {
                            binding.spinnerParentClass.setSelection(i);
                            break;
                        }
                    }
                }
            });
        });
    }

    @Override
    public boolean validate() {
        if (binding == null) return false;
        if (viewModel.isSubclass) {
            int position = binding.spinnerParentClass.getSelectedItemPosition();
            if (position < 0 || position >= parentClasses.size()) {
                Toast.makeText(getContext(), "Please select a parent class", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void saveData() {
        if (binding == null) return;
        if (viewModel.isSubclass) {
            int position = binding.spinnerParentClass.getSelectedItemPosition();
            if (position >= 0 && position < parentClasses.size()) {
                CombinedClass selected = parentClasses.get(position);
                viewModel.parentClassKey = selected.getKey();
                viewModel.parentClassName = selected.getName();
                viewModel.gameSystem = selected.getGameSystem();
            }
        } else {
            viewModel.parentClassKey = null;
            viewModel.parentClassName = null;
        }
    }
}