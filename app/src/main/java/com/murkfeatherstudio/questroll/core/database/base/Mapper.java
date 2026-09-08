package com.murkfeatherstudio.questroll.core.database.base;

public interface Mapper<E, D> {
    E toEntity(D dto);
}
