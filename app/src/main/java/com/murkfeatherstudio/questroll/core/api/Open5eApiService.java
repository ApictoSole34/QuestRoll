package com.murkfeatherstudio.questroll.core.api;

import com.murkfeatherstudio.questroll.core.models.open5e.ability.AbilityResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.alignment.AlignmentResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.background.BackgroundResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.CharacterClassResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.condition.ConditionResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.creature.CreatureResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.creature_type.CreatureTypeResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.damage_type.DamageTypeResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.environment.EnvironmentResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.game_system.GameSystemResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.item.ItemResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.item_category.ItemCategoryResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.item_rarity.ItemRarityResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.item_set.ItemSetResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.language.LanguageResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.license.LicenseResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.publisher.PublisherResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.rule.RuleResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.rule_set.RulesetResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.service.ServiceResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.spell_school.SpellSchoolResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.spell.SpellResponse;
import com.murkfeatherstudio.questroll.core.models.open5e.weapon_property.WeaponPropertyResponse;

import retrofit2.Call;
import retrofit2.http.Query;
import retrofit2.http.GET;

/**
 * Retrofit service interface for the Open5e API.
 * <p>
 * Defines endpoints for fetching various D&D 5e game data including documents,
 * classes, spells, creatures, and rules. Most methods support pagination
 * via the {@code page} query parameter.
 * </p>
 */
public interface Open5eApiService {
    @GET("documents/")
    Call<DocumentResponse> getDocuments();

    @GET("gamesystems/")
    Call<GameSystemResponse> getGameSystems();

    @GET("licenses/")
    Call<LicenseResponse> getLicenses();

    @GET("languages/")
    Call<LanguageResponse> getLanguages();

    @GET("publishers/")
    Call<PublisherResponse> getPublishers();

    @GET("abilities/")
    Call<AbilityResponse> getAbilities();

    @GET("classes/")
    Call<CharacterClassResponse> getCharacterClasses(@Query("page") int page);

    @GET("spells/")
    Call<SpellResponse> getSpellsPage(@Query("page") int page);

    @GET("spellschools/")
    Call<SpellSchoolResponse> getSpellSchools();

    @GET("creatures/")
    Call<CreatureResponse> getCreaturesPage(@Query("page") int page);

    @GET("creaturetypes/")
    Call<CreatureTypeResponse> getCreatureTypesPage(@Query("page") int page);

    @GET("species/")
    Call<SpeciesResponse> getSpeciesPage(@Query("page") int page);

    @GET("backgrounds/")
    Call<BackgroundResponse> getBackgroundsPage(@Query("page") int page);

    @GET("items/")
    Call<ItemResponse> getItemsPage(@Query("page") int page);

    @GET("damagetypes/")
    Call<DamageTypeResponse> getDamageTypesPage(@Query("page") int page);

    @GET("abilities/")
    Call<AbilityResponse> getAbilitiesPage(@Query("page") int page);

    @GET("alignments/")
    Call<AlignmentResponse> getAlignmentsPage(@Query("page") int page);

    @GET("itemrarities/")
    Call<ItemRarityResponse> getItemRarities();

    @GET("weaponproperties/")
    Call<WeaponPropertyResponse> getWeaponProperties();

    @GET("services/")
    Call<ServiceResponse> getServices();

    @GET("environments/")
    Call<EnvironmentResponse> getEnvironmentsPage(@Query("page") int page);

    @GET("rulesets/")
    Call<RulesetResponse> getRulesetsPage(@Query("page") int page);

    @GET("rules/")
    Call<RuleResponse> getRulesPage(@Query("page") int page);

    @GET("conditions/")
    Call<ConditionResponse> getConditionsPage(@Query("page") int page);

    @GET("itemcategories/")
    Call<ItemCategoryResponse> getItemCategoriesPage(@Query("page") int page);

    @GET("itemsets/")
    Call<ItemSetResponse> getItemSetsPage(@Query("page") int page);
}
