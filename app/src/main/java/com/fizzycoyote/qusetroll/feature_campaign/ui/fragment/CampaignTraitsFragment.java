package com.fizzycoyote.qusetroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.character.CharacterTraitEntity;
import com.fizzycoyote.qusetroll.core.models.character.CharacterWithRelations;
import com.fizzycoyote.qusetroll.feature_campaign.view_model.CampaignDetailViewModel;

import java.util.ArrayList;
import java.util.List;

public class CampaignTraitsFragment extends Fragment {

    private static final String ARG_CAMPAIGN_ID = "campaign_id";
    private long campaignId;
    private CampaignDetailViewModel viewModel;
    private TraitsAdapter adapter;

    public static CampaignTraitsFragment newInstance(long campaignId) {
        Bundle args = new Bundle();
        args.putLong(ARG_CAMPAIGN_ID, campaignId);
        CampaignTraitsFragment f = new CampaignTraitsFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) campaignId = getArguments().getLong(ARG_CAMPAIGN_ID);
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

        RecyclerView rvTraits = view.findViewById(R.id.rv_traits);
        rvTraits.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TraitsAdapter();
        rvTraits.setAdapter(adapter);
        TextView tvEmpty = view.findViewById(R.id.tv_empty_traits);

        viewModel.characterWithRelations.observe(getViewLifecycleOwner(), cwr -> {
            if (cwr == null || cwr.traits == null || cwr.traits.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                adapter.setItems(new ArrayList<>());
            } else {
                tvEmpty.setVisibility(View.GONE);
                adapter.setItems(cwr.traits);
            }
        });
    }

    // ─── Adapter ─────────────────────────────────────────────
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
}