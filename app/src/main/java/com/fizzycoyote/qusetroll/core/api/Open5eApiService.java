package com.fizzycoyote.qusetroll.core.api;

import com.fizzycoyote.qusetroll.core.models.open5e.ability.AbilityResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.background.BackgroundResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.creature.CreatureResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.language.LanguageResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.publisher.PublisherResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.species.SpeciesResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.spell_school.SpellSchoolResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.spell.SpellResponse;
import com.fizzycoyote.qusetroll.core.models.open5e.weapon.WeaponResponse;

import retrofit2.Call;
import retrofit2.http.Query;
import retrofit2.http.GET;
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
    Call<CharacterClassResponse> getCharacterClasses();

    @GET("spells/")
    Call<SpellResponse> getSpellsPage(@Query("page") int page);

    @GET("spellschools/")
    Call<SpellSchoolResponse> getSpellSchools();

    @GET("creatures/")
    Call<CreatureResponse> getCreaturesPage(@Query("page") int page);

    @GET("species/")
    Call<SpeciesResponse> getSpeciesPage(@Query("page") int page);

    @GET("backgrounds/")
    Call<BackgroundResponse> getBackgroundsPage(@Query("page") int page);

    @GET("weapons/")
    Call<WeaponResponse> getWeaponsPage(@Query("page") int page);
}
