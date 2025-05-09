package com.fizzycoyote.qusetroll.core.models.open5e.game_system;

public class GameSystemMapper {
    public static GameSystemEntity dtoToEntity(GameSystemDto dto) {
        GameSystemEntity entity = new GameSystemEntity();
        entity.key = dto.key;
        entity.url = dto.url;
        entity.name = dto.name;
        entity.desc = dto.desc;
        entity.contentPrefix = dto.contentPrefix;
        return entity;
    }
}
