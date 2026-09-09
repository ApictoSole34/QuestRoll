package com.murkfeatherstudio.questroll.feature_character.ui.creator;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.open5e.alignment.AlignmentEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentCharacterCreatorBinding;
import com.murkfeatherstudio.questroll.databinding.ItemPointbuyAttributeBinding;
import com.murkfeatherstudio.questroll.feature_character.utils.AttributeGenerator;
import com.murkfeatherstudio.questroll.feature_character.view_model.CharacterCreatorViewModel;

import java.util.ArrayList;
import java.util.List;

public class CharacterCreatorFragment extends Fragment {

    private CharacterCreatorViewModel viewModel;
    private FragmentCharacterCreatorBinding binding;
    private List<ItemPointbuyAttributeBinding> pointBuyBindings = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCharacterCreatorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        pointBuyBindings.clear();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CharacterCreatorViewModel.class);

        // Observe reference data (alignments, backgrounds, species, classes)
        viewModel.getAlignments().observe(getViewLifecycleOwner(), this::setupAlignmentSpinner);
        viewModel.getBackgrounds().observe(getViewLifecycleOwner(), this::setupBackgroundSpinner);
        viewModel.getSpecies().observe(getViewLifecycleOwner(), this::setupSpeciesSpinner);
        viewModel.getClasses().observe(getViewLifecycleOwner(), this::setupClassSpinner);

        // Switch UI based on attribute generation method
        viewModel.getAttributeMethod().observe(getViewLifecycleOwner(), method -> {
            if ("POINT_BUY".equals(method)) {
                binding.attributesStaticContainer.setVisibility(View.GONE);
                binding.attributesPointbuyContainer.setVisibility(View.VISIBLE);
                rebuildPointBuyUI();
            } else {
                binding.attributesStaticContainer.setVisibility(View.VISIBLE);
                binding.attributesPointbuyContainer.setVisibility(View.GONE);
                displayStaticAttributes();
            }
        });

        // Update attribute display when values change
        viewModel.getAttributes().observe(getViewLifecycleOwner(), vals -> {
            String method = viewModel.getAttributeMethod().getValue();
            if (!"POINT_BUY".equals(method)) {
                displayStaticAttributes();
            } else {
                updatePointBuyUI(vals);
            }
        });

        // Observe save result
        viewModel.getSavedCharacterId().observe(getViewLifecycleOwner(), id -> {
            if (id != null && id > 0) {
                Toast.makeText(getContext(), "Character saved!", Toast.LENGTH_SHORT).show();
                Bundle args = new Bundle();
                args.putLong("character_id", id);
                Navigation.findNavController(requireView()).navigate(R.id.action_creator_to_sheet, args);
            } else if (id != null && id == -1) {
                Toast.makeText(getContext(), "Save error", Toast.LENGTH_SHORT).show();
            }
        });

        binding.saveButton.setOnClickListener(v -> {
            viewModel.setCharacterName(binding.characterName.getText().toString());
            viewModel.saveCharacter();
        });

        binding.attrMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String method = "STANDARD";
            if (checkedId == R.id.radio_roll) method = "ROLL";
            else if (checkedId == R.id.radio_point_buy) method = "POINT_BUY";
            viewModel.generateAttributes(method);
        });

        // Default to standard array
        viewModel.generateAttributes("STANDARD");
    }

    // -------------------- Spinners --------------------

    private void setupAlignmentSpinner(List<AlignmentEntity> alignments) {
        ArrayAdapter<AlignmentEntity> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, alignments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.alignmentSpinner.setAdapter(adapter);
        binding.alignmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setSelectedAlignment(alignments.get(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupBackgroundSpinner(List<BackgroundEntity> backgrounds) {
        ArrayAdapter<BackgroundEntity> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, backgrounds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.backgroundSpinner.setAdapter(adapter);
        binding.backgroundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setSelectedBackground(backgrounds.get(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSpeciesSpinner(List<SpeciesEntity> speciesList) {
        ArrayAdapter<SpeciesEntity> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, speciesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.speciesSpinner.setAdapter(adapter);
        binding.speciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setSelectedSpecies(speciesList.get(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // -------------------- Classes (multiclass) --------------------

    private void setupClassSpinner(List<CharacterClassEntity> classes) {
        binding.addClassButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Add class");
            final String[] classNames = classes.stream().map(c -> c.name).toArray(String[]::new);
            builder.setItems(classNames, (dialog, which) -> {
                CharacterClassEntity selected = classes.get(which);
                EditText input = new EditText(requireContext());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Level")
                        .setView(input)
                        .setPositiveButton("OK", (d, w) -> {
                            try {
                                int lvl = Integer.parseInt(input.getText().toString());
                                viewModel.addClass(selected, lvl);
                                refreshClassList();
                            } catch (NumberFormatException e) {
                                Toast.makeText(getContext(), "Enter a valid level", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            });
            builder.show();
        });
    }

    /**
     * JAVADOC: classContainer is a dynamic layout managed via addView(). 
     * TextViews are created programmatically to display multiclass assignments 
     * because the quantity and content are determined by user input at runtime. 
     * Since these views are not defined in the static XML, View Binding is not applicable here.
     */
    private void refreshClassList() {
        if (binding == null) return;
        binding.classContainer.removeAllViews();
        List<CharacterCreatorViewModel.ClassAssignment> assignments = viewModel.getClassAssignments().getValue();
        if (assignments != null) {
            for (int i = 0; i < assignments.size(); i++) {
                CharacterCreatorViewModel.ClassAssignment ca = assignments.get(i);
                TextView tv = new TextView(getContext());
                tv.setText(ca.className + " level " + ca.level);
                tv.setPadding(16, 8, 16, 8);
                final int index = i;
                tv.setOnLongClickListener(v -> {
                    viewModel.removeClass(index);
                    refreshClassList();
                    return true;
                });
                binding.classContainer.addView(tv);
            }
        }
    }

    // -------------------- Attributes (static display) --------------------

    /**
     * JAVADOC: attributesStaticContainer is a dynamic layout populated with TextViews 
     * at runtime. We use removeAllViews() and addView() because the content 
     * (labels and values) is generated programmatically based on the current 
     * attribute values. These dynamically added views are not accessible via View Binding.
     */
    private void displayStaticAttributes() {
        if (binding == null) return;
        binding.attributesStaticContainer.removeAllViews();
        List<Integer> attrs = viewModel.getAttributes().getValue();
        if (attrs == null) return;
        String[] names = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (int i = 0; i < names.length; i++) {
            int val = attrs.get(i);
            int mod = (val - 10) / 2;
            TextView tv = new TextView(getContext());
            tv.setText(names[i] + ": " + val + " (" + (mod >= 0 ? "+" + mod : String.valueOf(mod)) + ")");
            binding.attributesStaticContainer.addView(tv);
        }
    }

    // -------------------- Point Buy UI --------------------

    /**
     * JAVADOC: attributesPointbuyContainer is a dynamic layout that holds several 
     * instances of a sub-layout. We use removeAllViews() and then inflate 
     * ItemPointbuyAttributeBinding instances to add them to the container. 
     * This approach allows us to dynamically build the Point Buy interface 
     * while still benefiting from View Binding for each individual attribute row.
     */
    private void rebuildPointBuyUI() {
        if (binding == null) return;
        binding.attributesPointbuyContainer.removeAllViews();
        pointBuyBindings.clear();
        String[] names = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        List<Integer> currentVals = viewModel.getAttributes().getValue();
        if (currentVals == null) return;

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            ItemPointbuyAttributeBinding itemBinding = ItemPointbuyAttributeBinding.inflate(getLayoutInflater(), binding.attributesPointbuyContainer, false);
            
            itemBinding.attrLabel.setText(names[i]);
            int val = currentVals.get(i);
            itemBinding.attrValue.setText(String.valueOf(val));
            int cost = AttributeGenerator.getPointCost(val);
            itemBinding.attrCost.setText("cost: " + cost);
            itemBinding.attrSeekbar.setProgress(val - 8); // 8 -> 0, 15 -> 7

            itemBinding.attrSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                    if (!fromUser) return;
                    int newVal = 8 + progress;
                    viewModel.adjustAttributeForPointBuy(index, newVal - val);
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {}
            });

            binding.attributesPointbuyContainer.addView(itemBinding.getRoot());
            pointBuyBindings.add(itemBinding);
        }
    }

    private void updatePointBuyUI(List<Integer> attrs) {
        for (int i = 0; i < pointBuyBindings.size() && i < attrs.size(); i++) {
            ItemPointbuyAttributeBinding itemBinding = pointBuyBindings.get(i);
            int val = attrs.get(i);
            itemBinding.attrValue.setText(String.valueOf(val));
            int cost = AttributeGenerator.getPointCost(val);
            itemBinding.attrCost.setText("cost: " + cost);
            itemBinding.attrSeekbar.setProgress(val - 8);
        }
    }
}