package com.murkfeatherstudio.questroll.feature_tools.calculator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the D&D Calculator. 
 * State is stored in static fields to persist data across different activity/fragment lifecycles.
 */
public class DndCalculatorViewModel extends ViewModel {
    // Static LiveData to ensure history and display persist when navigating between activities
    private static final MutableLiveData<String> display = new MutableLiveData<>("0");
    private static final MutableLiveData<List<String>> history = new MutableLiveData<>(new ArrayList<>());
    private static final MutableLiveData<Boolean> isHistoryOpen = new MutableLiveData<>(false);

    private static String currentInput = "";
    private static double firstOperand = Double.NaN;
    private static String operator = "";
    private static boolean isResultDisplayed = false;

    public LiveData<String> getDisplay() { return display; }
    public LiveData<List<String>> getHistory() { return history; }
    public LiveData<Boolean> getIsHistoryOpen() { return isHistoryOpen; }

    public void onButtonClick(String text) {
        if (text.matches("[0-9]")) {
            if (isResultDisplayed) {
                currentInput = text;
                isResultDisplayed = false;
            } else {
                if (currentInput.equals("0")) currentInput = "";
                currentInput += text;
            }
            updateDisplay();
        } else if (text.equals("C")) {
            clear();
        } else if (text.equals("DEL") || text.equals("←")) {
            backspace();
        } else if (text.equals("=")) {
            calculate();
        } else if (text.equals("H")) {
            toggleHistory();
        } else {
            // Operator clicked (+, -, *, /)
            if (!currentInput.isEmpty()) {
                if (!Double.isNaN(firstOperand)) {
                    calculate(); // Chained calculation
                }
                firstOperand = Double.parseDouble(currentInput);
                operator = text;
                currentInput = "";
                isResultDisplayed = false;
                updateDisplay();
            } else if (!Double.isNaN(firstOperand)) {
                // Change operator if already selected
                operator = text;
                updateDisplay();
            }
        }
    }

    private void clear() {
        currentInput = "";
        firstOperand = Double.NaN;
        operator = "";
        isResultDisplayed = false;
        display.setValue("0");
    }

    private void backspace() {
        if (isResultDisplayed) {
            clear();
            return;
        }

        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        } else if (!operator.isEmpty()) {
            operator = "";
            currentInput = formatNumber(firstOperand);
            firstOperand = Double.NaN;
        }
        updateDisplay();
    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder();
        if (!Double.isNaN(firstOperand)) {
            sb.append(formatNumber(firstOperand)).append(" ").append(operator).append(" ");
        }
        sb.append(currentInput);
        String out = sb.toString().trim();
        display.setValue(out.isEmpty() ? "0" : out);
    }

    private void calculate() {
        if (Double.isNaN(firstOperand) || currentInput.isEmpty()) return;
        try {
            double secondOperand = Double.parseDouble(currentInput);
            double result = 0;
            switch (operator) {
                case "+": result = firstOperand + secondOperand; break;
                case "-": result = firstOperand - secondOperand; break;
                case "*": result = firstOperand * secondOperand; break;
                case "/": result = secondOperand != 0 ? firstOperand / secondOperand : 0; break;
            }
            
            String calculation = formatNumber(firstOperand) + " " + operator + " " + formatNumber(secondOperand) + " = " + formatNumber(result);
            List<String> currentHistory = new ArrayList<>(history.getValue());
            currentHistory.add(0, calculation);
            history.setValue(currentHistory);

            currentInput = formatNumber(result);
            firstOperand = Double.NaN;
            operator = "";
            isResultDisplayed = true;
            display.setValue(currentInput);
        } catch (NumberFormatException ignored) {}
    }

    public void clearHistory() {
        history.setValue(new ArrayList<>());
    }

    private String formatNumber(double d) {
        if (Double.isNaN(d)) return "";
        if (d == (long) d) return String.valueOf((long) d);
        else return String.valueOf(d);
    }

    public void toggleHistory() {
        isHistoryOpen.setValue(!Boolean.TRUE.equals(isHistoryOpen.getValue()));
    }
}
