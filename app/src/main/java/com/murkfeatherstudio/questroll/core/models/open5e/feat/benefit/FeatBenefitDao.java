package com.murkfeatherstudio.questroll.core.models.open5e.feat.benefit;

import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface FeatBenefitDao extends BaseDao<FeatBenefitEntity> {
    @Query("SELECT * FROM feat_benefits WHERE featKey = :featKey")
    List<FeatBenefitEntity> getBenefitsForFeat(String featKey);
}
