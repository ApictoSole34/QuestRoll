package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.alignment.AlignmentEntity;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;

import java.util.List;

public class AlignmentStepFragment extends Fragment {
    private Spinner alignmentSpinner;
    private TextView descriptionText;
    private WizardViewModel viewModel;
    private List<AlignmentEntity> alignmentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_alignment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        alignmentSpinner = view.findViewById(R.id.alignment_spinner);
        descriptionText = view.findViewById(R.id.alignment_description);

        descriptionText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
        descriptionText.setTextColor(getResources().getColor(R.color.threads_text_primary, null));

        Button nextButton = view.findViewById(R.id.next_button);
        Button backButton = view.findViewById(R.id.back_button);

        loadAlignments();

        alignmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (alignmentList != null && position < alignmentList.size()) {
                    viewModel.alignmentKey = alignmentList.get(position).key;
                    showDescription();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        nextButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.next_action));
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadAlignments() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!isAdded()) return;
            alignmentList = Open5eDatabase.getInstance(requireContext())
                    .alignmentDao()
                    .getByGameSystem(viewModel.gameSystem);

            if (!isAdded()) return;
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;

                ArrayAdapter<AlignmentEntity> adapter = new ArrayAdapter<AlignmentEntity>(requireContext(),
                        android.R.layout.simple_spinner_item, alignmentList) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        AlignmentEntity item = getItem(position);
                        view.setText(item != null ? (item.shortName != null ? item.shortName : item.key) : "");
                        view.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                        view.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                        AlignmentEntity item = getItem(position);
                        view.setText(item != null ? (item.shortName != null ? item.shortName : item.key) : "");
                        view.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_regular));
                        view.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                alignmentSpinner.setAdapter(adapter);

                if (viewModel.alignmentKey != null) {
                    for (int i = 0; i < alignmentList.size(); i++) {
                        if (alignmentList.get(i).key.equals(viewModel.alignmentKey)) {
                            alignmentSpinner.setSelection(i);
                            break;
                        }
                    }
                }
                showDescription();
            });
        });
    }

    private void showDescription() {
        if (alignmentList != null && viewModel.alignmentKey != null) {
            for (AlignmentEntity a : alignmentList) {
                if (a.key.equals(viewModel.alignmentKey)) {
                    String desc = a.shortName != null ? a.shortName : a.key;
                    if (a.description != null && !a.description.isEmpty()) {
                        desc += "\n" + a.description;
                    }
                    descriptionText.setText(desc);
                    return;
                }
            }
        }
        descriptionText.setText("");
    }
}
