package com.murkfeatherstudio.questroll.core.local_database.dao.character;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.murkfeatherstudio.questroll.core.models.character.CharacterResourceEntity;

import java.util.List;

@Dao
public interface CharacterResourceDao {
    @Insert
    long insert(CharacterResourceEntity resource);

    @Update
    void update(CharacterResourceEntity resource);

    @Delete
    void delete(CharacterResourceEntity resource);

    @Query("SELECT * FROM character_resources WHERE character_id = :characterId")
    LiveData<List<CharacterResourceEntity>> getByCharacterId(long characterId);

    @Query("SELECT * FROM character_resources WHERE character_id = :characterId")
    List<CharacterResourceEntity> getByCharacterIdSync(long characterId);
}
