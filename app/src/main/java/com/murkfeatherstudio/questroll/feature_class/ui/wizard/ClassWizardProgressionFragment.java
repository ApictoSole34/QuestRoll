package com.murkfeatherstudio.questroll.feature_class.ui.wizard;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardProgressionBinding;
import com.murkfeatherstudio.questroll.feature_class.class_adapter.ProgressionAdapter;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassWizardProgressionFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private static final String[] ORDINALS = {
            "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th"
    };

    private ClassWizardViewModel viewModel;
    private FragmentWizardProgressionBinding binding;
    private ProgressionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWizardProgressionBinding.inflate(inflater, container, false);
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

        binding.rvProgression.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProgressionAdapter(
                viewModel.progression,
                viewModel.features,
                viewModel.casterType,
                new ProgressionAdapter.OnLevelClickListener() {
                    @Override
                    public void onEdit(int position, ClassWizardViewModel.ClassProgressionRow row) {
                        showEditLevelDialog(position, row);
                    }

                    @Override
                    public void onDelete(int position) {
                        viewModel.progression.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                }
        );
        binding.rvProgression.setAdapter(adapter);

        if (viewModel.progression.isEmpty()) {
            for (int i = 1; i <= 5; i++) {
                viewModel.progression.add(new ClassWizardViewModel.ClassProgressionRow(i));
            }
            adapter.notifyDataSetChanged();
        }

        binding.btnAddLevel.setOnClickListener(v -> {
            int nextLevel = viewModel.progression.isEmpty() ? 1 :
                    viewModel.progression.get(viewModel.progression.size() - 1).level + 1;
            if (nextLevel > 20) return;
            viewModel.progression.add(new ClassWizardViewModel.ClassProgressionRow(nextLevel));
            adapter.notifyItemInserted(viewModel.progression.size() - 1);
            binding.rvProgression.scrollToPosition(viewModel.progression.size() - 1);
        });
    }

    private int maxSpellLevelForCasterType() {
        switch (viewModel.casterType) {
            case "FULL": return 9;
            case "HALF": return 5;
            case "THIRD": return 4;
            case "WARLOCK": return 5;
            default: return 0;
        }
    }

    /**
     * JAVADOC: This dialog constructs its UI programmatically by inflating a ScrollView 
     * and a LinearLayout, and adding input fields via addView(). 
     * Since the number of spell slot fields depends on the class's caster type 
     * (e.g., Full Caster needs 9 levels of slots), the UI is dynamic and not 
     * defined in a static XML file. Therefore, View Binding cannot be used 
     * for these generated fields.
     */
    private void showEditLevelDialog(int position, ClassWizardViewModel.ClassProgressionRow row) {
        Context ctx = requireContext();

        ScrollView scrollView = new ScrollView(ctx);
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(20);
        layout.setPadding(pad, pad, pad, pad);
        scrollView.addView(layout);

        EditText etProfBonus = addNumberField(ctx, layout, "Proficiency Bonus", row.proficiencyBonus);

        EditText etCantrips = null;
        Map<Integer, EditText> slotFields = new LinkedHashMap<>();

        int maxSlotLevel = maxSpellLevelForCasterType();
        if (maxSlotLevel > 0) {
            etCantrips = addNumberField(ctx, layout, "Cantrips Known", row.cantripsKnown);

            TextView slotsHeader = new TextView(ctx);
            slotsHeader.setText("Spell Slots");
            slotsHeader.setTextSize(14);
            slotsHeader.setPadding(0, dpToPx(12), 0, dpToPx(4));
            layout.addView(slotsHeader);

            for (int lvl = 1; lvl <= maxSlotLevel; lvl++) {
                EditText et = addNumberField(ctx, layout,
                        ORDINALS[lvl - 1] + " Level Slots", row.getSlotForLevel(lvl));
                slotFields.put(lvl, et);
            }
        }

        EditText finalEtCantrips = etCantrips;

        new AlertDialog.Builder(ctx)
                .setTitle("Level " + row.level)
                .setView(scrollView)
                .setPositiveButton("Save", (dialog, which) -> {
                    row.proficiencyBonus = parseIntOrDefault(etProfBonus, row.proficiencyBonus);

                    if (finalEtCantrips != null) {
                        row.cantripsKnown = parseIntOrDefault(finalEtCantrips, row.cantripsKnown);
                    }
                    for (Map.Entry<Integer, EditText> entry : slotFields.entrySet()) {
                        int value = parseIntOrDefault(entry.getValue(),
                                row.getSlotForLevel(entry.getKey()));
                        row.setSlotForLevel(entry.getKey(), value);
                    }

                    adapter.notifyItemChanged(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private EditText addNumberField(Context ctx, LinearLayout parent, String label, int currentValue) {
        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextSize(13);
        tv.setPadding(0, dpToPx(8), 0, dpToPx(2));
        parent.addView(tv);

        EditText et = new EditText(ctx);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(currentValue));
        parent.addView(et);
        return et;
    }

    private int parseIntOrDefault(EditText et, int fallback) {
        try {
            return Integer.parseInt(et.getText().toString().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean validate() {
        return viewModel != null && !viewModel.progression.isEmpty();
    }

    @Override
    public void saveData() {
        // Data is already in ViewModel — modified in real-time by the edit dialog.
    }
}