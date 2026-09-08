package com.murkfeatherstudio.questroll.feature_campaign.view_model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.murkfeatherstudio.questroll.core.local_database.CampaignDatabase;
import com.murkfeatherstudio.questroll.core.models.campaign.CampaignEntity;

import java.util.List;

/**
 * ViewModel for the quick-access campaign list drawer.
 * Provides a LiveData list of all user campaigns.
 */
public class QuickCampaignViewModel extends AndroidViewModel {
    private final LiveData<List<CampaignEntity>> allCampaigns;

    public QuickCampaignViewModel(@NonNull Application application) {
        super(application);
        // Pobieramy kampanie z dedykowanej bazy CampaignDatabase
        allCampaigns = CampaignDatabase.getInstance(application).campaignDao().getAll();
    }

    public LiveData<List<CampaignEntity>> getAllCampaigns() {
        return allCampaigns;
    }
}
