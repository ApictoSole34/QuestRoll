package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.murkfeatherstudio.questroll.databinding.FragmentCampaignDiceBinding;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.CampaignDetailViewModel;
import com.murkfeatherstudio.questroll.feature_dice.adapter.DiceAdapter;
import com.murkfeatherstudio.questroll.feature_dice.model.Dice;
import com.murkfeatherstudio.questroll.feature_dice.ui.DialogManageDice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment providing dice rolling functionality within the Campaign view.
 * Uses CampaignDetailViewModel to persist dice state across fragment transitions.
 */
public class CampaignDiceFragment extends Fragment implements DialogManageDice.DiceManageListener, SensorEventListener {

    private CampaignDetailViewModel viewModel;
    private DiceAdapter diceAdapter;
    private FragmentCampaignDiceBinding binding;
    
    private Map<String, Integer> localDiceCounts = new HashMap<>();
    private SensorManager sensorManager;
    private boolean isShakeToRollEnabled = false;
    private Sensor accelerometer;

    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int SHAKE_COUNT_REQUIRED = 4;
    private static final long SHAKE_WINDOW_MS = 1500;
    private static final long SETTLE_DELAY_MS = 400;
    private static final long POST_ROLL_COOLDOWN_MS = 2000;

    private int shakeCount = 0;
    private long firstShakeTime = 0;
    private long lastRollTime = 0;
    private boolean shakePrimed = false;

    private final Handler settleHandler = new Handler(Looper.getMainLooper());
    private final Runnable settleRunnable = () -> {
        if (shakePrimed && isShakeToRollEnabled) {
            long now = System.currentTimeMillis();
            if (now - lastRollTime > POST_ROLL_COOLDOWN_MS) {
                rollDice();
                lastRollTime = now;
            }
        }
        resetShakeState();
    };

    public static CampaignDiceFragment newInstance() {
        return new CampaignDiceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCampaignDiceBinding.inflate(inflater, container, false);
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

        // Access the shared ViewModel from the Activity
        viewModel = new ViewModelProvider(requireActivity()).get(CampaignDetailViewModel.class);

        binding.diceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        diceAdapter = new DiceAdapter(new ArrayList<>());
        binding.diceRecyclerView.setAdapter(diceAdapter);

        // Restore State from ViewModel
        viewModel.getDiceCounts().observe(getViewLifecycleOwner(), counts -> {
            this.localDiceCounts = counts;
        });

        viewModel.getRolledDice().observe(getViewLifecycleOwner(), diceList -> {
            diceAdapter.updateDiceList(new ArrayList<>(diceList));
        });

        viewModel.getLastRollResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty()) {
                binding.resultText.setText(result);
                if (binding.showResultSwitch.isChecked()) {
                    binding.resultWindow.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.rollButton.setOnClickListener(v -> rollDice());

        binding.showResultSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !binding.resultText.getText().toString().isEmpty()) {
                binding.resultWindow.setVisibility(View.VISIBLE);
            } else {
                binding.resultWindow.setVisibility(View.GONE);
            }
        });

        binding.shakeToRollSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isShakeToRollEnabled = isChecked;
            if (isChecked && accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            } else {
                sensorManager.unregisterListener(this);
                resetShakeState();
            }
        });

        binding.manageDiceButton.setOnClickListener(v -> {
            DialogManageDice dialogManageDice = new DialogManageDice(requireContext(), localDiceCounts, this);
            dialogManageDice.show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isShakeToRollEnabled && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        settleHandler.removeCallbacks(settleRunnable);
        resetShakeState();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isShakeToRollEnabled) return;
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
        float acceleration = Math.abs(magnitude - SensorManager.GRAVITY_EARTH);

        long now = System.currentTimeMillis();
        if (now - lastRollTime < POST_ROLL_COOLDOWN_MS) return;

        if (acceleration > SHAKE_THRESHOLD) {
            if (shakeCount == 0 || now - firstShakeTime > SHAKE_WINDOW_MS) {
                shakeCount = 1;
                firstShakeTime = now;
            } else {
                shakeCount++;
            }

            if (shakeCount >= SHAKE_COUNT_REQUIRED) {
                shakePrimed = true;
            }

            if (shakePrimed) {
                settleHandler.removeCallbacks(settleRunnable);
                settleHandler.postDelayed(settleRunnable, SETTLE_DELAY_MS);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void resetShakeState() {
        shakeCount = 0;
        firstShakeTime = 0;
        shakePrimed = false;
        settleHandler.removeCallbacks(settleRunnable);
    }

    @Override
    public void onDiceCountUpdated(Map<String, Integer> updatedDiceCounts) {
        this.localDiceCounts = updatedDiceCounts;
        List<Dice> updatedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : localDiceCounts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                try {
                    int diceType = Integer.parseInt(entry.getKey().substring(1));
                    updatedList.add(new Dice(diceType));
                } catch (Exception e) {
                    Log.e("DiceFragment", "Error parsing dice type", e);
                }
            }
        }
        
        // Save state to ViewModel
        viewModel.updateDiceState(localDiceCounts, updatedList, "");
    }

    @SuppressLint("NotifyDataSetChanged")
    private void rollDice() {
        resetShakeState();
        lastRollTime = System.currentTimeMillis();

        StringBuilder result = new StringBuilder("Result: ");
        List<Dice> diceList = new ArrayList<>();
        
        for (int i = 0; i < diceAdapter.getItemCount(); i++) {
            Dice dice = diceAdapter.getItem(i);
            dice.setAnimationPlayed(false);
            int rollResult = dice.roll();
            result.append("d").append(dice.getType()).append(": ").append(rollResult).append(", ");
            diceList.add(dice);
        }

        if (result.length() > 2) {
            result.delete(result.length() - 2, result.length());
        }

        String currentResultStr = result.toString();
        
        // Persist roll results in ViewModel
        viewModel.updateDiceState(localDiceCounts, diceList, currentResultStr);
        
        diceAdapter.notifyDataSetChanged();

        if (binding.showResultSwitch.isChecked()) {
            binding.resultText.setText(currentResultStr);
            binding.resultWindow.setVisibility(View.VISIBLE);
        }
    }
}
