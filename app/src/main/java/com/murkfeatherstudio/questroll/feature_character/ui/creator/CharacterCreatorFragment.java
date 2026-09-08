package com.murkfeatherstudio.questroll.feature_character.ui.creator;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.open5e.alignment.AlignmentEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesEntity;
import com.murkfeatherstudio.questroll.feature_character.utils.AttributeGenerator;
import com.murkfeatherstudio.questroll.feature_character.view_model.CharacterCreatorViewModel;

import java.util.ArrayList;
import java.util.List;

public class CharacterCreatorFragment extends Fragment {

    private CharacterCreatorViewModel viewModel;

    private EditText nameEdit;
    private Spinner alignmentSpinner, backgroundSpinner, speciesSpinner;
    private Button addClassButton, saveButton;
    private LinearLayout classContainer;
    private RadioGroup attrMethodGroup;
    private LinearLayout attributesStaticContainer;
    private LinearLayout attributesPointbuyContainer;
    private List<View> pointBuyViews = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_creator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CharacterCreatorViewModel.class);

        nameEdit = view.findViewById(R.id.character_name);
        alignmentSpinner = view.findViewById(R.id.alignment_spinner);
        backgroundSpinner = view.findViewById(R.id.background_spinner);
        speciesSpinner = view.findViewById(R.id.species_spinner);
        addClassButton = view.findViewById(R.id.add_class_button);
        saveButton = view.findViewById(R.id.save_button);
        classContainer = view.findViewById(R.id.class_container);
        attrMethodGroup = view.findViewById(R.id.attr_method_group);
        attributesStaticContainer = view.findViewById(R.id.attributes_static_container);
        attributesPointbuyContainer = view.findViewById(R.id.attributes_pointbuy_container);

        // Observe reference data (alignments, backgrounds, species, classes)
        viewModel.getAlignments().observe(getViewLifecycleOwner(), this::setupAlignmentSpinner);
        viewModel.getBackgrounds().observe(getViewLifecycleOwner(), this::setupBackgroundSpinner);
        viewModel.getSpecies().observe(getViewLifecycleOwner(), this::setupSpeciesSpinner);
        viewModel.getClasses().observe(getViewLifecycleOwner(), this::setupClassSpinner);

        // Switch UI based on attribute generation method
        viewModel.getAttributeMethod().observe(getViewLifecycleOwner(), method -> {
            if ("POINT_BUY".equals(method)) {
                attributesStaticContainer.setVisibility(View.GONE);
                attributesPointbuyContainer.setVisibility(View.VISIBLE);
                rebuildPointBuyUI();
            } else {
                attributesStaticContainer.setVisibility(View.VISIBLE);
                attributesPointbuyContainer.setVisibility(View.GONE);
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

        saveButton.setOnClickListener(v -> {
            viewModel.setCharacterName(nameEdit.getText().toString());
            viewModel.saveCharacter();
        });

        attrMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
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
        ArrayAdapter<AlignmentEntity> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, alignments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alignmentSpinner.setAdapter(adapter);
        alignmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setSelectedAlignment(alignments.get(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupBackgroundSpinner(List<BackgroundEntity> backgrounds) {
        ArrayAdapter<BackgroundEntity> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, backgrounds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        backgroundSpinner.setAdapter(adapter);
        backgroundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setSelectedBackground(backgrounds.get(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSpeciesSpinner(List<SpeciesEntity> speciesList) {
        ArrayAdapter<SpeciesEntity> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, speciesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesSpinner.setAdapter(adapter);
        speciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setSelectedSpecies(speciesList.get(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // -------------------- Classes (multiclass) --------------------

    private void setupClassSpinner(List<CharacterClassEntity> classes) {
        addClassButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Add class");
            final String[] classNames = classes.stream().map(c -> c.name).toArray(String[]::new);
            builder.setItems(classNames, (dialog, which) -> {
                CharacterClassEntity selected = classes.get(which);
                EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(getContext())
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

    private void refreshClassList() {
        classContainer.removeAllViews();
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
                classContainer.addView(tv);
            }
        }
    }

    // -------------------- Attributes (static display) --------------------

    private void displayStaticAttributes() {
        attributesStaticContainer.removeAllViews();
        List<Integer> attrs = viewModel.getAttributes().getValue();
        if (attrs == null) return;
        String[] names = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (int i = 0; i < names.length; i++) {
            int val = attrs.get(i);
            int mod = (val - 10) / 2;
            TextView tv = new TextView(getContext());
            tv.setText(names[i] + ": " + val + " (" + (mod >= 0 ? "+" + mod : String.valueOf(mod)) + ")");
            attributesStaticContainer.addView(tv);
        }
    }

    // -------------------- Point Buy UI --------------------

    private void rebuildPointBuyUI() {
        attributesPointbuyContainer.removeAllViews();
        pointBuyViews.clear();
        String[] names = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        List<Integer> currentVals = viewModel.getAttributes().getValue();
        if (currentVals == null) return;

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_pointbuy_attribute, attributesPointbuyContainer, false);
            TextView label = item.findViewById(R.id.attr_label);
            TextView valueView = item.findViewById(R.id.attr_value);
            TextView costView = item.findViewById(R.id.attr_cost);
            SeekBar seekBar = item.findViewById(R.id.attr_seekbar);

            label.setText(names[i]);
            int val = currentVals.get(i);
            valueView.setText(String.valueOf(val));
            int cost = AttributeGenerator.getPointCost(val);
            costView.setText("cost: " + cost);
            seekBar.setProgress(val - 8); // 8 -> 0, 15 -> 7

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                    if (!fromUser) return;
                    int newVal = 8 + progress;
                    viewModel.adjustAttributeForPointBuy(index, newVal - val);
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {}
            });

            attributesPointbuyContainer.addView(item);
            pointBuyViews.add(item);
        }
    }

    private void updatePointBuyUI(List<Integer> attrs) {
        for (int i = 0; i < pointBuyViews.size() && i < attrs.size(); i++) {
            View item = pointBuyViews.get(i);
            TextView valueView = item.findViewById(R.id.attr_value);
            TextView costView = item.findViewById(R.id.attr_cost);
            SeekBar seekBar = item.findViewById(R.id.attr_seekbar);
            int val = attrs.get(i);
            valueView.setText(String.valueOf(val));
            int cost = AttributeGenerator.getPointCost(val);
            costView.setText("cost: " + cost);
            seekBar.setProgress(val - 8);
        }
    }
}