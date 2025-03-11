package com.fizzycoyote.qusetroll;


import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RollDiceActivity extends AppCompatActivity implements DialogManageDice.DiceManageListener {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch showResultSwitch;
    private LinearLayout resultWindow;
    private TextView resultText;
    private RecyclerView diceRecyclerView;
    private DiceAdapter diceAdapter;
    private Map<String, Integer> diceCounts = new HashMap<>();
    private String currentResult = "";

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
        resultWindow = findViewById(R.id.resultWindow);
        Button manageDiceButton = findViewById(R.id.manageDiceButton);
        resultText = findViewById(R.id.resultText);

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


        manageDiceButton.setOnClickListener(v -> {
            DialogManageDice dialogManageDice = new DialogManageDice(this, diceCounts, this);
            dialogManageDice.show();
        });
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
            int rollResult = dice.roll(); // Rzut kostką
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