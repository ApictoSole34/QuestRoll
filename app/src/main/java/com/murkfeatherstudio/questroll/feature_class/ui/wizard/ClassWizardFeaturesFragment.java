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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.murkfeatherstudio.questroll.core.models.custom.custom_character_class.custom_gained_at.CustomGainedAt;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardFeaturesBinding;
import com.murkfeatherstudio.questroll.feature_class.class_adapter.FeatureAdapter;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class ClassWizardFeaturesFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private FragmentWizardFeaturesBinding binding;
    private FeatureAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWizardFeaturesBinding.inflate(inflater, container, false);
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

        binding.rvFeatures.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FeatureAdapter(new FeatureAdapter.OnFeatureClickListener() {
            @Override
            public void onEdit(CustomFeatureEntity feature, int index) {
                showFeatureDialog(index, feature);
            }

            @Override
            public void onDelete(int index) {
                viewModel.features.remove(index);
                adapter.submitList(new ArrayList<>(viewModel.features));
            }
        });
        adapter.setDeleteEnabled(true);
        binding.rvFeatures.setAdapter(adapter);

        adapter.submitList(new ArrayList<>(viewModel.features));

        binding.btnAddFeature.setOnClickListener(v -> showFeatureDialog(-1, null));
    }

    /**
     * JAVADOC: This dialog builds its UI programmatically using a ScrollView and LinearLayout. 
     * Since the form fields (etName, etDesc, etLevels) are created dynamically in code 
     * and not defined in a standalone XML layout for this specific dialog, 
     * View Binding is not applicable here. Traditional programmatical UI construction is kept.
     */
    private void showFeatureDialog(int index, CustomFeatureEntity existing) {
        Context ctx = requireContext();
        boolean isEdit = existing != null;

        ScrollView scrollView = new ScrollView(ctx);
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(20);
        layout.setPadding(pad, pad, pad, pad);
        scrollView.addView(layout);

        TextView nameLabel = new TextView(ctx);
        nameLabel.setText("Feature Name");
        nameLabel.setTextSize(13);
        layout.addView(nameLabel);

        EditText etName = new EditText(ctx);
        etName.setText(isEdit ? existing.name : "");
        layout.addView(etName);

        TextView descLabel = new TextView(ctx);
        descLabel.setText("Description");
        descLabel.setTextSize(13);
        descLabel.setPadding(0, dpToPx(12), 0, 0);
        layout.addView(descLabel);

        EditText etDesc = new EditText(ctx);
        etDesc.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etDesc.setMinLines(3);
        etDesc.setText(isEdit ? existing.description : "");
        layout.addView(etDesc);

        TextView levelsLabel = new TextView(ctx);
        levelsLabel.setText("Gained at level(s) — comma separated, e.g. \"4, 8, 12\"");
        levelsLabel.setTextSize(13);
        levelsLabel.setPadding(0, dpToPx(12), 0, 0);
        layout.addView(levelsLabel);

        EditText etLevels = new EditText(ctx);
        etLevels.setInputType(InputType.TYPE_CLASS_TEXT);
        etLevels.setText(isEdit ? formatGainedAtLevels(existing.customGainedAt) : "1");
        layout.addView(etLevels);

        new AlertDialog.Builder(ctx)
                .setTitle(isEdit ? "Edit Feature" : "Add Feature")
                .setView(scrollView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(ctx, "Feature name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CustomFeatureEntity feature = isEdit ? existing : new CustomFeatureEntity();
                    feature.name = name;
                    feature.description = etDesc.getText().toString().trim();
                    feature.type = "CLASS_LEVEL_FEATURE";
                    feature.customGainedAt = parseGainedAtLevels(etLevels.getText().toString());

                    if (isEdit) {
                        viewModel.features.set(index, feature);
                    } else {
                        viewModel.features.add(feature);
                    }
                    adapter.submitList(new ArrayList<>(viewModel.features));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatGainedAtLevels(List<CustomGainedAt> gainedAt) {
        if (gainedAt == null || gainedAt.isEmpty()) return "1";
        StringBuilder sb = new StringBuilder();
        for (CustomGainedAt g : gainedAt) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(g.level);
        }
        return sb.toString();
    }

    private List<CustomGainedAt> parseGainedAtLevels(String csv) {
        List<CustomGainedAt> result = new ArrayList<>();
        if (csv == null) return result;
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            try {
                int level = Integer.parseInt(trimmed);
                result.add(new CustomGainedAt(level, null));
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void saveData() {}
}
