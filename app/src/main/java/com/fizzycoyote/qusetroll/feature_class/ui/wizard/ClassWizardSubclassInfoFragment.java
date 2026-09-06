package com.fizzycoyote.qusetroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class ClassWizardSubclassInfoFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private CheckBox cbIsSubclass;
    private LinearLayout parentClassContainer;
    private Spinner spinnerParentClass;
    private List<CombinedClass> parentClasses = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_subclass_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ClassWizardViewModel.class);

        cbIsSubclass = view.findViewById(R.id.cb_is_subclass);
        parentClassContainer = view.findViewById(R.id.parent_class_container);
        spinnerParentClass = view.findViewById(R.id.spinner_parent_class);

        cbIsSubclass.setChecked(viewModel.isSubclass);
        parentClassContainer.setVisibility(viewModel.isSubclass ? View.VISIBLE : View.GONE);

        cbIsSubclass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.isSubclass = isChecked;
            parentClassContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                viewModel.parentClassKey = null;
                viewModel.parentClassName = null;
            }
        });

        loadParentClasses();
    }

    private void loadParentClasses() {
        new Thread(() -> {
            if (!isAdded()) return;

            // Pobierz klasy bazowe (nie subklasy) z Open5e
            List<CharacterClassEntity> open5eClasses = Open5eDatabase.getInstance(requireContext())
                    .characterClassDao().getBaseClassesByGameSystem(viewModel.gameSystem);

            // Pobierz customowe klasy bazowe
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
                            viewModel.gameSystem  // 👈 DODANY gameSystem
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

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

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
                spinnerParentClass.setAdapter(adapter);

                // Przywróć poprzedni wybór
                if (viewModel.parentClassKey != null) {
                    for (int i = 0; i < parentClasses.size(); i++) {
                        if (parentClasses.get(i).getKey().equals(viewModel.parentClassKey)) {
                            spinnerParentClass.setSelection(i);
                            break;
                        }
                    }
                }
            });
        }).start();
    }

    @Override
    public boolean validate() {
        if (viewModel.isSubclass) {
            int position = spinnerParentClass.getSelectedItemPosition();
            if (position < 0 || position >= parentClasses.size()) {
                Toast.makeText(getContext(), "Please select a parent class", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void saveData() {
        if (viewModel.isSubclass) {
            int position = spinnerParentClass.getSelectedItemPosition();
            if (position >= 0 && position < parentClasses.size()) {
                CombinedClass selected = parentClasses.get(position);
                viewModel.parentClassKey = selected.getKey();
                viewModel.parentClassName = selected.getName();
                // Subklasa MUSI dzielić game system z rodzicem - inaczej dostalibyśmy
                // np. subklasę 5e-2024 podpiętą pod klasę bazową z 5e-2014.
                viewModel.gameSystem = selected.getGameSystem();
            }
        } else {
            viewModel.parentClassKey = null;
            viewModel.parentClassName = null;
        }
    }
}