package com.fizzycoyote.qusetroll.feature_dice.ui;


import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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
import com.fizzycoyote.qusetroll.feature_dice.adapter.DiceAdapter;
import com.fizzycoyote.qusetroll.feature_dice.model.Dice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RollDiceActivity extends AppCompatActivity implements DialogManageDice.DiceManageListener, SensorEventListener {

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
    private long lastShakeTime = 0;
    private float lastX = 0;
    private int shakeStep = 0;

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

        manageDiceButton.setOnClickListener(v -> toggleDiceRecyclerView());
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
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                sensorManager.unregisterListener(this);
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

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float deltaX = Math.abs(x - lastX);
            lastX = x;

            float shakeThreshold = 2.0f;

            if (deltaX > shakeThreshold) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > 500) {
                    lastShakeTime = currentTime;

                    if(x < 0) {
                        if (shakeStep == 0 || shakeStep == 2){
                            shakeStep++;
                        } else {
                            shakeStep = 0;
                        }
                    } else if (x > 0) {
                        if (shakeStep == 1) {
                            shakeStep++;
                        } else {
                            shakeStep = 0;
                        }
                    }
                    if (shakeStep == 3) {
                        shakeStep = 0;
                        rollDice();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
        Log.d("UIVisibility", "Dice RecyclerView: " + diceRecyclerView.getVisibility());
        if (diceRecyclerView.getVisibility() == View.VISIBLE) {
            diceRecyclerView.setVisibility(View.GONE);
        } else {
            diceRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void rollDice() {
        StringBuilder result = new StringBuilder("Roll resoult: ");
        for (int i = 0; i < diceAdapter.getItemCount(); i++) {
            Dice dice = diceAdapter.getItem(i);
            dice.setAnimationPlayed(false);
            int rollResult = dice.roll();
            result.append(dice.getType()).append(" : ").append(rollResult).append(", ");
        }

        result.delete(result.length() - 2, result.length());

        currentResult = result.toString();

        diceAdapter.notifyDataSetChanged();

        if (showResultSwitch.isChecked()) {
            resultText.setText(currentResult);
            resultWindow.setVisibility(View.VISIBLE);
        }
    }

}