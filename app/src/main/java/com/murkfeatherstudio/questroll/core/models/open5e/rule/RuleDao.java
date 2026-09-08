package com.murkfeatherstudio.questroll.core.models.open5e.rule;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.murkfeatherstudio.questroll.core.database.base.BaseDao;

import java.util.List;

/**
 * Data Access Object for D&D 5e game rules stored in the compendium.
 */
@Dao
public interface RuleDao extends BaseDao<RuleEntity> {

    @Query("SELECT COUNT(*) FROM rules")
    int getCount();

    @Query("DELETE FROM rules")
    void deleteAll();

    /**
     * Retrieves all rules belonging to a specific ruleset.
     *
     * @param rulesetKey The unique key of the ruleset.
     * @return LiveData list of rule entities, ordered by their index.
     */
    @Query("SELECT * FROM rules WHERE rulesetKey = :rulesetKey ORDER BY `index` ASC")
    LiveData<List<RuleEntity>> getByRuleset(String rulesetKey);

    /**
     * Retrieves a specific rule by its unique key.
     */
    @Query("SELECT * FROM rules WHERE key = :key")
    LiveData<RuleEntity> getByKey(String key);

    /**
     * Retrieves a list of unique source document URLs for a given ruleset.
     */
    @Query("SELECT DISTINCT documentUrl FROM rules WHERE rulesetKey = :rulesetKey")
    LiveData<List<String>> getDistinctSourcesForRuleset(String rulesetKey);

    /**
     * Searches and filters rules within a ruleset based on name and source.
     *
     * @param rulesetKey The ruleset to search within.
     * @param query      Partial name to match.
     * @param source     Partial source document URL to match.
     * @return LiveData list of matching rule entities.
     */
    @Query("SELECT * FROM rules WHERE rulesetKey = :rulesetKey " +
            "AND (:query = '' OR name LIKE '%' || :query || '%') " +
            "AND (:source = '' OR documentUrl LIKE '%' || :source || '%') " +
            "ORDER BY `index` ASC")
    LiveData<List<RuleEntity>> getFilteredRules(String rulesetKey, String query, String source);
}
