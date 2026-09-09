package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.murkfeatherstudio.questroll.databinding.FragmentCampaignTraitsBinding;
import com.murkfeatherstudio.questroll.databinding.ItemSimpleTextBinding;
import com.murkfeatherstudio.questroll.databinding.ItemTraitBinding;
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

    private FragmentCampaignTraitsBinding binding;

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
        binding = FragmentCampaignTraitsBinding.inflate(inflater, container, false);
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

        binding.rvTraits.setLayoutManager(new LinearLayoutManager(requireContext()));
        traitsAdapter = new TraitsAdapter();
        binding.rvTraits.setAdapter(traitsAdapter);

        binding.rvSpells.setLayoutManager(new LinearLayoutManager(requireContext()));
        spellAdapter = new SpellAdapter(this::showSpellDetails);
        binding.rvSpells.setAdapter(spellAdapter);

        binding.btnToggleTraits.setOnClickListener(v -> setViewMode(false));
        binding.btnToggleSpells.setOnClickListener(v -> setViewMode(true));

        viewModel.getCanCastSpells().observe(getViewLifecycleOwner(), canCast -> {
            binding.btnToggleSpells.setVisibility(canCast ? View.VISIBLE : View.GONE);
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
        binding.rvTraits.setVisibility(spells ? View.GONE : View.VISIBLE);
        binding.rvSpells.setVisibility(spells ? View.VISIBLE : View.GONE);
        
        updateToggleButtons();

        if (spells) {
            binding.tvEmptyTraits.setVisibility(View.GONE);
            updateEmptySpellsVisibility();
        } else {
            binding.tvEmptySpells.setVisibility(View.GONE);
            updateEmptyTraitsVisibility();
        }
    }

    private void updateToggleButtons() {
        int activeColor = getResources().getColor(R.color.threads_gold, null);
        int inactiveColor = getResources().getColor(android.R.color.darker_gray, null);

        if (showSpells) {
            binding.btnToggleSpells.setTextColor(activeColor);
            binding.btnToggleSpells.setAlpha(1.0f);
            binding.btnToggleTraits.setTextColor(inactiveColor);
            binding.btnToggleTraits.setAlpha(0.7f);
        } else {
            binding.btnToggleTraits.setTextColor(activeColor);
            binding.btnToggleTraits.setAlpha(1.0f);
            binding.btnToggleSpells.setTextColor(inactiveColor);
            binding.btnToggleSpells.setAlpha(0.7f);
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
        if (!showSpells && binding != null) {
            binding.tvEmptyTraits.setVisibility(traitsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
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
        if (showSpells && binding != null) {
            binding.tvEmptySpells.setVisibility(spellAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
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

        /**
         * JAVADOC: findViewById is used here with system/library resource IDs (android.R.id.message, 
         * alertTitle) because these views are part of the internal hierarchy of the standard 
         * AlertDialog/AppCompatDialog. View Binding only generates classes for layouts defined 
         * in our own project, not for platform-provided dialog structures.
         */
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
            ItemTraitBinding binding = ItemTraitBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CharacterTraitEntity t = items.get(position);
            holder.binding.tvTraitName.setText(t.name);
            if (t.description != null && !t.description.isEmpty()) {
                holder.binding.tvTraitDesc.setText(t.description);
                holder.binding.tvTraitDesc.setVisibility(View.VISIBLE);
            } else {
                holder.binding.tvTraitDesc.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemTraitBinding binding;
            ViewHolder(ItemTraitBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
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
            ItemSimpleTextBinding binding = ItemSimpleTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SpellDisplayItem item = items.get(position);
            String display = item.name + (item.isPrepared ? " [Prepared]" : "");
            holder.binding.tvText.setText(display);
            holder.binding.getRoot().setOnClickListener(v -> listener.onSpellClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemSimpleTextBinding binding;
            ViewHolder(ItemSimpleTextBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
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
