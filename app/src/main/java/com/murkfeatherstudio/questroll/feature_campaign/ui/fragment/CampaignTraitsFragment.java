package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.character.CharacterSpellEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterTraitEntity;
import com.murkfeatherstudio.questroll.core.models.character.CharacterWithRelations;
import com.murkfeatherstudio.questroll.core.models.open5e.spell.SpellEntity;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.CampaignDetailViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CampaignTraitsFragment extends Fragment {

    private static final String ARG_CAMPAIGN_ID = "campaign_id";
    private static final String ARG_SHOW_SPELLS = "show_spells";

    private long campaignId;
    private boolean initialShowSpells;
    private CampaignDetailViewModel viewModel;

    private Button btnToggleTraits, btnToggleSpells;
    private RecyclerView rvTraits, rvSpells;
    private TextView tvEmptyTraits, tvEmptySpells;

    private TraitsAdapter traitsAdapter;
    private SpellAdapter spellAdapter;

    private boolean showSpells = false;

    public static CampaignTraitsFragment newInstance(long campaignId, boolean showSpells) {
        Bundle args = new Bundle();
        args.putLong(ARG_CAMPAIGN_ID, campaignId);
        args.putBoolean(ARG_SHOW_SPELLS, showSpells);
        CampaignTraitsFragment f = new CampaignTraitsFragment();
        f.setArguments(args);
        return f;
    }

    public static CampaignTraitsFragment newInstance(long campaignId) {
        return newInstance(campaignId, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            campaignId = getArguments().getLong(ARG_CAMPAIGN_ID);
            initialShowSpells = getArguments().getBoolean(ARG_SHOW_SPELLS, false);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(CampaignDetailViewModel.class);
        viewModel.setCampaignId(campaignId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_campaign_traits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnToggleTraits = view.findViewById(R.id.btn_toggle_traits);
        btnToggleSpells = view.findViewById(R.id.btn_toggle_spells);
        rvTraits = view.findViewById(R.id.rv_traits);
        rvSpells = view.findViewById(R.id.rv_spells);
        tvEmptyTraits = view.findViewById(R.id.tv_empty_traits);
        tvEmptySpells = view.findViewById(R.id.tv_empty_spells);

        rvTraits.setLayoutManager(new LinearLayoutManager(requireContext()));
        traitsAdapter = new TraitsAdapter();
        rvTraits.setAdapter(traitsAdapter);

        rvSpells.setLayoutManager(new LinearLayoutManager(requireContext()));
        spellAdapter = new SpellAdapter(this::showSpellDetails);
        rvSpells.setAdapter(spellAdapter);

        btnToggleTraits.setOnClickListener(v -> setViewMode(false));
        btnToggleSpells.setOnClickListener(v -> setViewMode(true));

        viewModel.getCanCastSpells().observe(getViewLifecycleOwner(), canCast -> {
            btnToggleSpells.setVisibility(canCast ? View.VISIBLE : View.GONE);
            if (!canCast) setViewMode(false);
        });

        viewModel.characterWithRelations.observe(getViewLifecycleOwner(), cwr -> {
            updateTraits(cwr);
            updateSpells(cwr);
        });

        setViewMode(initialShowSpells);
    }

    private void setViewMode(boolean spells) {
        this.showSpells = spells;
        rvTraits.setVisibility(spells ? View.GONE : View.VISIBLE);
        rvSpells.setVisibility(spells ? View.VISIBLE : View.GONE);
        
        updateToggleButtons();

        if (spells) {
            tvEmptyTraits.setVisibility(View.GONE);
            updateEmptySpellsVisibility();
        } else {
            tvEmptySpells.setVisibility(View.GONE);
            updateEmptyTraitsVisibility();
        }
    }

    private void updateToggleButtons() {
        int activeColor = getResources().getColor(R.color.threads_gold, null);
        int inactiveColor = getResources().getColor(android.R.color.darker_gray, null);

        if (showSpells) {
            btnToggleSpells.setTextColor(activeColor);
            btnToggleSpells.setAlpha(1.0f);
            btnToggleTraits.setTextColor(inactiveColor);
            btnToggleTraits.setAlpha(0.7f);
        } else {
            btnToggleTraits.setTextColor(activeColor);
            btnToggleTraits.setAlpha(1.0f);
            btnToggleSpells.setTextColor(inactiveColor);
            btnToggleSpells.setAlpha(0.7f);
        }
    }

    private void updateTraits(CharacterWithRelations cwr) {
        if (cwr == null || cwr.traits == null) {
            traitsAdapter.setItems(new ArrayList<>());
            updateEmptyTraitsVisibility();
            return;
        }

        List<CharacterTraitEntity> classTraits = cwr.traits.stream()
                .filter(t -> "CLASS".equals(t.sourceType))
                .collect(Collectors.toList());

        traitsAdapter.setItems(classTraits);
        updateEmptyTraitsVisibility();
    }

    private void updateEmptyTraitsVisibility() {
        if (!showSpells) {
            tvEmptyTraits.setVisibility(traitsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void updateSpells(CharacterWithRelations cwr) {
        if (cwr == null || cwr.spells == null || cwr.spells.isEmpty()) {
            spellAdapter.setItems(new ArrayList<>());
            updateEmptySpellsVisibility();
            return;
        }
        loadSpellDetails(cwr.spells);
    }

    private void updateEmptySpellsVisibility() {
        if (showSpells) {
            tvEmptySpells.setVisibility(spellAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
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
                spellAdapter.setItems(displayItems);
                updateEmptySpellsVisibility();
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

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(spell.name)
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .create();
        
        dialog.show();

        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_regular));
        }
        
        TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
        if (titleView != null) {
            titleView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.cinzel_bold));
        }
    }

    private static class TraitsAdapter extends RecyclerView.Adapter<TraitsAdapter.ViewHolder> {
        private List<CharacterTraitEntity> items = new ArrayList<>();

        void setItems(List<CharacterTraitEntity> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_trait, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CharacterTraitEntity t = items.get(position);
            holder.tvName.setText(t.name);
            if (t.description != null && !t.description.isEmpty()) {
                holder.tvDesc.setText(t.description);
                holder.tvDesc.setVisibility(View.VISIBLE);
            } else {
                holder.tvDesc.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_trait_name);
                tvDesc = itemView.findViewById(R.id.tv_trait_desc);
            }
        }
    }

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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_text, parent, false);
            return new ViewHolder((TextView) v);
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