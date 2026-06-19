package com.fizzycoyote.qusetroll.feature_dice.ui;


import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.base.BaseActivity;
import com.fizzycoyote.qusetroll.feature_dice.adapter.DiceAdapter;
import com.fizzycoyote.qusetroll.feature_dice.model.Dice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RollDiceActivity extends BaseActivity implements DialogManageDice.DiceManageListener, SensorEventListener {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch showResultSwitch;
    private Switch shakeToRollSwitch;
    private LinearLayout resultWindow;
    private TextView resultText;
    private RecyclerView diceRecyclerView;
    private DiceAdapter diceAdapter;
    private Map<String, Integer> diceCounts = new HashMap<>();
    private String currentResult = "";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll_dice);
        diceCounts.clear();

        diceRecyclerView = findViewById(R.id.diceRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        diceRecyclerView.setLayoutManager(gridLayoutManager);
        Button rollButton = findViewById(R.id.rollButton);
        showResultSwitch = findViewById(R.id.showResultSwitch);
        shakeToRollSwitch = findViewById(R.id.shakeToRollSwitch);
        resultWindow = findViewById(R.id.resultWindow);
        Button manageDiceButton = findViewById(R.id.manageDiceButton);
        resultText = findViewById(R.id.resultText);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Log.e("RollDiceActivity", "SensorManager is null");
        }

        List<Dice> diceList = new ArrayList<>();
        diceAdapter = new DiceAdapter(diceList);
        diceRecyclerView.setAdapter(diceAdapter);

        rollButton.setOnClickListener(v -> rollDice());

        showResultSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                resultText.setText(currentResult);
                resultWindow.setVisibility(View.VISIBLE);
            } else {
                resultWindow.setVisibility(View.GONE);
            }
        });

        shakeToRollSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isShakeToRollEnabled = isChecked;
            if (isChecked && accelerometer != null) {
                sensorManager.registerListener(this, accelerometer,
                        SensorManager.SENSOR_DELAY_GAME);
            } else {
                sensorManager.unregisterListener(this);
                resetShakeState();
            }
        });

        manageDiceButton.setOnClickListener(v -> {
            DialogManageDice dialogManageDice = new DialogManageDice(this, diceCounts, this);
            dialogManageDice.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isShakeToRollEnabled && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
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
            // Reset okna jeśli minęło za dużo czasu od pierwszego uderzenia
            if (shakeCount == 0 || now - firstShakeTime > SHAKE_WINDOW_MS) {
                shakeCount = 1;
                firstShakeTime = now;
            } else {
                shakeCount++;
            }

            Log.d("ShakeDetect", "shake count: " + shakeCount + ", accel: " + acceleration);

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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void resetShakeState() {
        shakeCount = 0;
        firstShakeTime = 0;
        shakePrimed = false;
        settleHandler.removeCallbacks(settleRunnable);
    }

    @Override
    public void onDiceCountUpdated(Map<String, Integer> updatedDiceCounts) {
        diceCounts = updatedDiceCounts;
        List<Dice> updatedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : diceCounts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                try {
                    int diceType = Integer.parseInt(entry.getKey().substring(1));
                    updatedList.add(new Dice(diceType));
                } catch (NumberFormatException e) {
                    Log.e("RollDiceActivity", "Invalid key: " + entry.getKey(), e);
                }
            }
        }
        diceAdapter.updateDiceList(updatedList);
    }

    private void toggleDiceRecyclerView() {
        if (diceRecyclerView.getVisibility() == View.VISIBLE) {
            diceRecyclerView.setVisibility(View.GONE);
        } else {
            diceRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void rollDice() {
        // Resetujemy stan shake przed rzutem, żeby nie było podwójnego wyzwolenia
        resetShakeState();
        lastRollTime = System.currentTimeMillis();

        StringBuilder result = new StringBuilder("Wynik rzutu: ");
        for (int i = 0; i < diceAdapter.getItemCount(); i++) {
            Dice dice = diceAdapter.getItem(i);
            dice.setAnimationPlayed(false);
            int rollResult = dice.roll();
            result.append("d").append(dice.getType()).append(": ").append(rollResult).append(", ");
        }

        if (result.length() > 2) {
            result.delete(result.length() - 2, result.length());
        }

        currentResult = result.toString();
        diceAdapter.notifyDataSetChanged();

        if (showResultSwitch.isChecked()) {
            resultText.setText(currentResult);
            resultWindow.setVisibility(View.VISIBLE);
        }
    }
}