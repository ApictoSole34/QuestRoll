package com.fizzycoyote.qusetroll;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import java.util.HashMap;
import java.util.Map;

public class DialogManageDice {

    private final Context context;
    private final Map<String, Integer> diceCounts;
    private DiceManageListener listener;
    private AlertDialog dialog;


    public interface DiceManageListener {
        void onDiceCountUpdated(Map<String, Integer> diceCounts);
    }

    public DialogManageDice(Context context, Map<String, Integer> diceCounts, DiceManageListener listener) {
        this.context = context;
        this.listener = listener;
        this.diceCounts = diceCounts != null ? diceCounts : new HashMap<>();
    }


    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_manage_dice, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        String[] diceTypes = {"d20", "d12", "d10", "d8", "d6", "d4"};

        for (String diceType : diceTypes) {
            int btnPlusId = context.getResources().getIdentifier("btnPlus" + diceType.toUpperCase(), "id", context.getPackageName());
            int btnMinusId = context.getResources().getIdentifier("btnMinus" + diceType.toUpperCase(), "id", context.getPackageName());
            int counterId = context.getResources().getIdentifier("counter" + diceType.toUpperCase(), "id", context.getPackageName());

            Button btnPlus = dialogView.findViewById(btnPlusId);
            Button btnMinus = dialogView.findViewById(btnMinusId);
            TextView counter = dialogView.findViewById(counterId);
            Button applyButton = dialogView.findViewById(R.id.applyChangesButton);

            counter.setText(String.valueOf(getDiceCount(diceType)));

            applyButton.setOnClickListener(v -> applyChanges());

            btnPlus.setOnClickListener(v -> {
                addDice(diceType);
                counter.setText(String.valueOf(getDiceCount(diceType)));
            });
            btnMinus.setOnClickListener(v -> {
                removeDice(diceType);
                counter.setText(String.valueOf(getDiceCount(diceType)));
            });
        }

        dialog = builder.create();
        dialog.show();
    }

    private void addDice(String diceType) {
        int currentCount = getDiceCount(diceType);
        diceCounts.put(diceType, currentCount + 1);
    }

    private void removeDice(String diceType) {
        int currentCount = getDiceCount(diceType);
        if (currentCount > 0) {
            diceCounts.put(diceType, currentCount - 1);
        }
    }

    private int getDiceCount(String diceType) {
        return diceCounts.getOrDefault(diceType, 0);
    }


    private void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void applyChanges() {
        if (listener != null) {
            listener.onDiceCountUpdated(diceCounts);
        }
        dismiss();
    }
}
