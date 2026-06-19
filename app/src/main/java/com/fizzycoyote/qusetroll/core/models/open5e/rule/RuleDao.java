package com.fizzycoyote.qusetroll.core.models.open5e.rule;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RuleEntity> rules);

    @Query("SELECT COUNT(*) FROM rules")
    int getCount();

    @Query("DELETE FROM rules")
    void deleteAll();

    @Query("SELECT * FROM rules WHERE rulesetKey = :rulesetKey ORDER BY `index` ASC")
    LiveData<List<RuleEntity>> getByRuleset(String rulesetKey);

    @Query("SELECT * FROM rules WHERE key = :key")
    LiveData<RuleEntity> getByKey(String key);

    @Query("SELECT DISTINCT documentUrl FROM rules WHERE rulesetKey = :rulesetKey")
    LiveData<List<String>> getDistinctSourcesForRuleset(String rulesetKey);

    @Query("SELECT * FROM rules WHERE rulesetKey = :rulesetKey " +
            "AND (:query = '' OR name LIKE '%' || :query || '%') " +
            "AND (:source = '' OR documentUrl LIKE '%' || :source || '%') " +
            "ORDER BY `index` ASC")
    LiveData<List<RuleEntity>> getFilteredRules(String rulesetKey, String query, String source);
}