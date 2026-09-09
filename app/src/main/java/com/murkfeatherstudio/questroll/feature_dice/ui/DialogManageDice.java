package com.murkfeatherstudio.questroll.feature_dice.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.databinding.DialogManageDiceBinding;

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
        DialogManageDiceBinding binding = DialogManageDiceBinding.inflate(LayoutInflater.from(context));
        builder.setView(binding.getRoot());

        dialog = builder.create();

        String[] diceTypes = {"d20", "d12", "d10", "d8", "d6", "d4"};

        for (String diceType : diceTypes) {
            /**
             * JAVADOC: We use getIdentifier and findViewById here because the dice type keys 
             * are processed in a loop. View Binding generates static fields for views, 
             * which does not allow dynamic access by string-based IDs without reflection.
             * This approach is more concise for grids with many repeating elements.
             */
            int btnPlusId = context.getResources().getIdentifier("btnPlus" + diceType.toUpperCase(), "id", context.getPackageName());
            int btnMinusId = context.getResources().getIdentifier("btnMinus" + diceType.toUpperCase(), "id", context.getPackageName());
            int counterId = context.getResources().getIdentifier("counter" + diceType.toUpperCase(), "id", context.getPackageName());

            Button btnPlus = binding.getRoot().findViewById(btnPlusId);
            Button btnMinus = binding.getRoot().findViewById(btnMinusId);
            TextView counter = binding.getRoot().findViewById(counterId);

            if (counter != null) {
                counter.setText(String.valueOf(getDiceCount(diceType)));
            }

            if (btnPlus != null) {
                btnPlus.setOnClickListener(v -> {
                    addDice(diceType);
                    if (counter != null) counter.setText(String.valueOf(getDiceCount(diceType)));
                });
            }
            
            if (btnMinus != null) {
                btnMinus.setOnClickListener(v -> {
                    removeDice(diceType);
                    if (counter != null) counter.setText(String.valueOf(getDiceCount(diceType)));
                });
            }
        }

        binding.applyChangesButton.setOnClickListener(v -> applyChanges());

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
