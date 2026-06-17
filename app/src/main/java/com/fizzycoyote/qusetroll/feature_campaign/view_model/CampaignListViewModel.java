package com.fizzycoyote.qusetroll.feature_campaign.view_model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.fizzycoyote.qusetroll.core.local_database.CampaignDatabase;
import com.fizzycoyote.qusetroll.core.models.campaign.CampaignEntity;

import java.util.List;

public class CampaignListViewModel extends AndroidViewModel {

    private final CampaignDatabase db;
    public final LiveData<List<CampaignEntity>> campaigns;

    public CampaignListViewModel(Application application) {
        super(application);
        db = CampaignDatabase.getInstance(application);
        campaigns = db.campaignDao().getAll();
    }

    public void deleteCampaign(CampaignEntity campaign) {
        CampaignDatabase.databaseWriteExecutor.execute(() -> {
            db.campaignNoteDao().deleteForCampaign(campaign.id);
            db.campaignDao().delete(campaign);
        });
    }
}
