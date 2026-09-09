package com.murkfeatherstudio.questroll.feature_tools.calculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.murkfeatherstudio.questroll.databinding.FragmentDndCalculatorBinding;

import java.util.List;

/**
 * Fragment implementing the D&D Calculator tool.
 * Displays the current mathematical operation and maintains a persistent history.
 */
public class DndCalculatorFragment extends Fragment {

    private DndCalculatorViewModel viewModel;
    private HistoryAdapter historyAdapter;
    private FragmentDndCalculatorBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDndCalculatorBinding.inflate(inflater, container, false);
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

        viewModel = new ViewModelProvider(this).get(DndCalculatorViewModel.class);

        // Styling the display
        binding.calcDisplay.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_medium));

        binding.historyRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new HistoryAdapter();
        binding.historyRecycler.setAdapter(historyAdapter);

        binding.btnClearHistory.setOnClickListener(v -> viewModel.clearHistory());

        setupButtons();

        viewModel.getDisplay().observe(getViewLifecycleOwner(), s -> binding.calcDisplay.setText(s));
        viewModel.getHistory().observe(getViewLifecycleOwner(), list -> historyAdapter.setItems(list));
        viewModel.getIsHistoryOpen().observe(getViewLifecycleOwner(), isOpen -> {
            binding.historyPanel.setVisibility(isOpen ? View.VISIBLE : View.GONE);
            
            // Adjust drawer width in the current Activity when history is toggled
            if (getActivity() instanceof BaseActivity) {
                ((BaseActivity) getActivity()).setDrawerWidth(isOpen);
            }
        });
    }

    /**
     * Initializes click listeners for all calculator buttons.
     * 
     * <p>Note: Buttons in the GridLayout are accessed by iterating through child views. 
     * Since View Binding does not generate a collection for all children, this approach 
     * is the most efficient way to assign listeners to a large grid of similar buttons 
     * without code duplication.</p>
     */
    private void setupButtons() {
        if (binding == null) return;
        for (int i = 0; i < binding.calcGrid.getChildCount(); i++) {
            View child = binding.calcGrid.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                int id = btn.getId();
                // Exclude the history toggle button
                if (id != R.id.btn_history) {
                    btn.setOnClickListener(v -> viewModel.onButtonClick(btn.getText().toString()));
                }
            }
        }
        
        binding.btnHistory.setOnClickListener(v -> viewModel.toggleHistory());
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
