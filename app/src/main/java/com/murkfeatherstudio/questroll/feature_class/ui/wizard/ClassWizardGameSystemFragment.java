package com.murkfeatherstudio.questroll.feature_class.ui.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.AppExecutors;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.game_system.GameSystemEntity;
import com.murkfeatherstudio.questroll.feature_class.view_model.ClassWizardViewModel;

import java.util.ArrayList;
import java.util.List;

public class ClassWizardGameSystemFragment extends Fragment
        implements ClassWizardActivity.ClassWizardStep {

    private ClassWizardViewModel viewModel;
    private Spinner spinnerGameSystem;
    private List<GameSystemEntity> gameSystems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_class_game_system, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ClassWizardViewModel.class);

        // 🔥 Znajdź widoki – z zabezpieczeniem przed null
        TextView tvHeader = view.findViewById(R.id.tv_game_system_header);
        TextView tvHint = view.findViewById(R.id.tv_game_system_hint);
        spinnerGameSystem = view.findViewById(R.id.spinner_game_system);

        // Jeśli layout nie ma tych ID – nie crashuj, tylko zaloguj
        if (tvHeader != null) {
            tvHeader.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
            tvHeader.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        }

        if (tvHint != null) {
            tvHint.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
            tvHint.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
        }

        if (spinnerGameSystem != null) {
            loadGameSystems();
        }
    }

    private void loadGameSystems() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<GameSystemEntity> loaded = Open5eDatabase.getInstance(requireContext())
                    .gameSystemDao().getAllGameSystems();

            if (!isAdded()) return;

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;

                gameSystems = loaded != null ? loaded : new ArrayList<>();

                ArrayAdapter<GameSystemEntity> adapter = new ArrayAdapter<GameSystemEntity>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        gameSystems
                ) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getView(position, convertView, parent);
                        tv.setText(getItem(position) != null ? getItem(position).name : "");
                        tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return tv;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                        tv.setText(getItem(position) != null ? getItem(position).name : "");
                        tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
                        tv.setTextColor(getResources().getColor(R.color.threads_text_primary, null));
                        return tv;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGameSystem.setAdapter(adapter);

                for (int i = 0; i < gameSystems.size(); i++) {
                    if (gameSystems.get(i).key.equals(viewModel.gameSystem)) {
                        spinnerGameSystem.setSelection(i);
                        break;
                    }
                }
            });
        });
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void saveData() {
        if (spinnerGameSystem == null) return;
        int position = spinnerGameSystem.getSelectedItemPosition();
        if (position >= 0 && position < gameSystems.size()) {
            viewModel.gameSystem = gameSystems.get(position).key;
        }
    }
}
