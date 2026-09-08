package com.murkfeatherstudio.questroll.core.models.open5e.rule_set;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

@Dao
public interface RulesetDao extends BaseDao<RulesetEntity> {

    @Query("SELECT COUNT(*) FROM rulesets")
    int getCount();

    @Query("DELETE FROM rulesets")
    void deleteAll();

    @Query("SELECT * FROM rulesets ORDER BY name ASC")
    LiveData<List<RulesetEntity>> getAll();

    @Query("SELECT * FROM rulesets WHERE key = :key")
    LiveData<RulesetEntity> getByKey(String key);

    @Query("SELECT * FROM rulesets WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%') AND " +
            "(:source = '' OR documentName = :source) " +
            "ORDER BY name ASC")
    LiveData<List<RulesetEntity>> getFiltered(String query, String source);

    @Query("SELECT DISTINCT documentName FROM rulesets WHERE documentName IS NOT NULL ORDER BY documentName ASC")
    LiveData<List<String>> getDistinctSources();
}
