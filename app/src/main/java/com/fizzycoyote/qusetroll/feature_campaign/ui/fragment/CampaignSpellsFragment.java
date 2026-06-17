package com.fizzycoyote.qusetroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.character.CharacterSpellEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellEntity;
import com.fizzycoyote.qusetroll.feature_campaign.view_model.CampaignDetailViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class CampaignSpellsFragment extends Fragment {

    private static final String ARG_CAMPAIGN_ID = "campaign_id";
    private long campaignId;
    private CampaignDetailViewModel viewModel;
    private SpellAdapter adapter;
    private Map<String, SpellEntity> spellsCache = new HashMap<>();

    public static CampaignSpellsFragment newInstance(long campaignId) {
        Bundle args = new Bundle();
        args.putLong(ARG_CAMPAIGN_ID, campaignId);
        CampaignSpellsFragment f = new CampaignSpellsFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) campaignId = getArguments().getLong(ARG_CAMPAIGN_ID);
        viewModel = new ViewModelProvider(requireActivity()).get(CampaignDetailViewModel.class);
        viewModel.setCampaignId(campaignId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_campaign_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvEmpty = view.findViewById(R.id.tv_placeholder);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_placeholder);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SpellAdapter(spell -> showSpellDetails(spell));
        recyclerView.setAdapter(adapter);
        tvEmpty.setText("No spells known");

        viewModel.characterWithRelations.observe(getViewLifecycleOwner(), cwr -> {
            if (cwr == null || cwr.spells == null || cwr.spells.isEmpty()) {
                adapter.setItems(new ArrayList<>());
                tvEmpty.setVisibility(View.VISIBLE);
                return;
            }
            tvEmpty.setVisibility(View.GONE);
            loadSpellDetails(cwr.spells);
        });
    }

    private void loadSpellDetails(List<CharacterSpellEntity> characterSpells) {
        new Thread(() -> {
            if (!isAdded()) return;
            String gameSystem = viewModel.getCurrentGameSystem();
            List<SpellEntity> allSpells = Open5eDatabase.getInstance(requireContext())
                    .spellDao().getAllByGameSystem(gameSystem);
            Map<String, SpellEntity> spellMap = new HashMap<>();
            for (SpellEntity spell : allSpells) {
                spellMap.put(spell.key, spell);
            }
            spellsCache = spellMap;

            List<SpellDisplayItem> displayItems = new ArrayList<>();
            for (CharacterSpellEntity cs : characterSpells) {
                SpellEntity spell = spellMap.get(cs.spellKey);
                if (spell != null) {
                    displayItems.add(new SpellDisplayItem(spell, cs.isPrepared));
                } else {
                    SpellDisplayItem fallback = new SpellDisplayItem();
                    fallback.spellKey = cs.spellKey;
                    fallback.name = cs.spellKey;
                    fallback.isPrepared = cs.isPrepared;
                    fallback.spellEntity = null;
                    displayItems.add(fallback);
                }
            }

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                adapter.setItems(displayItems);
            });
        }).start();
    }

    private void showSpellDetails(SpellDisplayItem item) {
        SpellEntity spell = item.spellEntity;
        if (spell == null) {
            Toast.makeText(getContext(), "No details available for " + item.name, Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Level: ").append(spell.level == 0 ? "Cantrip" : spell.level).append("\n");
        sb.append("School: ").append(spell.schoolName != null ? spell.schoolName : "Unknown").append("\n");
        sb.append("Casting time: ").append(spell.castingTime != null ? spell.castingTime : "1 action").append("\n");
        sb.append("Range: ").append(spell.rangeText != null ? spell.rangeText : spell.range + " ft").append("\n");
        sb.append("Duration: ").append(spell.duration != null ? spell.duration : "Instantaneous").append("\n");
        if (spell.ritual) sb.append("Ritual\n");
        if (spell.concentration) sb.append("Concentration\n");
        sb.append("Components: ");
        List<String> comps = new ArrayList<>();
        if (spell.verbal) comps.add("V");
        if (spell.somatic) comps.add("S");
        if (spell.material) comps.add("M" + (spell.materialSpecified != null ? " (" + spell.materialSpecified + ")" : ""));
        sb.append(comps.isEmpty() ? "None" : String.join(", ", comps)).append("\n\n");
        sb.append(spell.desc != null ? spell.desc : "No description");
        if (spell.higherLevel != null && !spell.higherLevel.isEmpty()) {
            sb.append("\n\nAt Higher Levels: ").append(spell.higherLevel);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(spell.name)
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    // ─── Adapter ─────────────────────────────────────────────
    private static class SpellAdapter extends RecyclerView.Adapter<SpellAdapter.ViewHolder> {
        private List<SpellDisplayItem> items = new ArrayList<>();
        private final OnSpellClickListener listener;

        interface OnSpellClickListener {
            void onSpellClick(SpellDisplayItem item);
        }

        SpellAdapter(OnSpellClickListener listener) {
            this.listener = listener;
        }

        void setItems(List<SpellDisplayItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(32, 16, 32, 16);
            tv.setTextSize(16f);
            android.util.TypedValue outValue = new android.util.TypedValue();
            parent.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            tv.setBackgroundResource(outValue.resourceId);
            tv.setClickable(true);
            tv.setFocusable(true);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SpellDisplayItem item = items.get(position);
            String display = item.name + (item.isPrepared ? " [Prepared]" : "");
            holder.textView.setText(display);
            holder.textView.setOnClickListener(v -> listener.onSpellClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(TextView v) { super(v); textView = v; }
        }
    }

    // ─── Data class for display ───────────────────────────────
    private static class SpellDisplayItem {
        String spellKey;
        String name;
        boolean isPrepared;
        SpellEntity spellEntity;

        SpellDisplayItem() {}
        SpellDisplayItem(SpellEntity spell, boolean prepared) {
            this.spellKey = spell.key;
            this.name = spell.name;
            this.isPrepared = prepared;
            this.spellEntity = spell;
        }
    }
}