package com.murkfeatherstudio.questroll.core.models.open5e.game_system;

import com.murkfeatherstudio.questroll.core.database.base.Mapper;

public class GameSystemMapper implements Mapper<GameSystemEntity, GameSystemDto> {
    @Override
    public GameSystemEntity toEntity(GameSystemDto dto) {
        GameSystemEntity entity = new GameSystemEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.contentPrefix = dto.contentPrefix;
        return entity;
    }
}
