package com.murkfeatherstudio.questroll.feature_campaign.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.campaign.CampaignEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentQuickCampaignListBinding;
import com.murkfeatherstudio.questroll.databinding.ItemQuickCampaignBinding;
import com.murkfeatherstudio.questroll.feature_campaign.ui.CampaignDetailActivity;
import com.murkfeatherstudio.questroll.feature_campaign.view_model.QuickCampaignViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple drawer fragment that displays a list of campaigns for quick navigation.
 * Updated to use themed buttons for a better look.
 */
public class QuickCampaignFragment extends Fragment {

    private QuickCampaignViewModel viewModel;
    private CampaignAdapter adapter;
    private FragmentQuickCampaignListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQuickCampaignListBinding.inflate(inflater, container, false);
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
        viewModel = new ViewModelProvider(this).get(QuickCampaignViewModel.class);

        binding.rvQuickCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CampaignAdapter();
        binding.rvQuickCampaigns.setAdapter(adapter);

        viewModel.getAllCampaigns().observe(getViewLifecycleOwner(), campaigns -> {
            adapter.setCampaigns(campaigns);
        });
    }

    private class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.ViewHolder> {
        private List<CampaignEntity> campaigns = new ArrayList<>();

        void setCampaigns(List<CampaignEntity> campaigns) {
            this.campaigns = campaigns;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemQuickCampaignBinding itemBinding = ItemQuickCampaignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CampaignEntity campaign = campaigns.get(position);
            holder.binding.tvName.setText(campaign.name);
            holder.binding.tvSystem.setText(campaign.gameSystem);
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CampaignDetailActivity.class);
                intent.putExtra(CampaignDetailActivity.EXTRA_CAMPAIGN_ID, campaign.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return campaigns.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemQuickCampaignBinding binding;
            ViewHolder(ItemQuickCampaignBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
