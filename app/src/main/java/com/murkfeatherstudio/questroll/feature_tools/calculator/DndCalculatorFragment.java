package com.murkfeatherstudio.questroll.feature_tools.calculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;

import java.util.List;

/**
 * Fragment implementing the D&D Calculator tool.
 * Displays the current mathematical operation and maintains a persistent history.
 */
public class DndCalculatorFragment extends Fragment {

    private DndCalculatorViewModel viewModel;
    private TextView display;
    private LinearLayout historyPanel;
    private HistoryAdapter historyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dnd_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DndCalculatorViewModel.class);

        display = view.findViewById(R.id.calc_display);
        historyPanel = view.findViewById(R.id.history_panel);
        RecyclerView historyRecycler = view.findViewById(R.id.history_recycler);
        Button clearHistoryButton = view.findViewById(R.id.btn_clear_history);

        // Styling the display
        display.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_medium));

        historyRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new HistoryAdapter();
        historyRecycler.setAdapter(historyAdapter);

        if (clearHistoryButton != null) {
            clearHistoryButton.setOnClickListener(v -> viewModel.clearHistory());
        }

        setupButtons(view);

        viewModel.getDisplay().observe(getViewLifecycleOwner(), s -> display.setText(s));
        viewModel.getHistory().observe(getViewLifecycleOwner(), list -> historyAdapter.setItems(list));
        viewModel.getIsHistoryOpen().observe(getViewLifecycleOwner(), isOpen -> {
            historyPanel.setVisibility(isOpen ? View.VISIBLE : View.GONE);
            
            // Adjust drawer width in the current Activity when history is toggled
            if (getActivity() instanceof BaseActivity) {
                ((BaseActivity) getActivity()).setDrawerWidth(isOpen);
            }
        });
    }

    private void setupButtons(View view) {
        GridLayout grid = view.findViewById(R.id.calc_grid);
        if (grid == null) return;
        
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                // Exclude the history toggle button if it's already handled elsewhere or uses static ID
                if (btn.getId() != R.id.btn_history) {
                    btn.setOnClickListener(v -> viewModel.onButtonClick(btn.getText().toString()));
                }
            }
        }
        
        Button btnHistory = view.findViewById(R.id.btn_history);
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> viewModel.toggleHistory());
        }
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<String> items = List.of();

        @SuppressLint("NotifyDataSetChanged")
        void setItems(List<String> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(32, 16, 32, 16);
            tv.setTextColor(parent.getContext().getResources().getColor(R.color.threads_text_primary, null));
            tv.setTextSize(14);
            tv.setTypeface(ResourcesCompat.getFont(parent.getContext(), R.font.inter_regular));
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(items.get(position));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View v) { super(v); }
        }
    }
}
