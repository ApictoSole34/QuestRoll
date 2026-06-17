package com.fizzycoyote.qusetroll.feature_campaign.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fizzycoyote.qusetroll.R;

public class CampaignDiceFragment extends Fragment {

    public static CampaignDiceFragment newInstance() {
        return new CampaignDiceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_campaign_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvPlaceholder = view.findViewById(R.id.tv_placeholder);
        tvPlaceholder.setText("Dice roller – coming soon");
    }
}