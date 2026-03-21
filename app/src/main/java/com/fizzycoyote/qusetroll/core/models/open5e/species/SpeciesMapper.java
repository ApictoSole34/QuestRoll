package com.fizzycoyote.qusetroll.core.models.open5e.species;

import com.google.gson.Gson;

import java.util.List;
import java.util.stream.Collectors;

public class SpeciesMapper {

    public static SpeciesEntity dtoToEntity(SpeciesDto dto) {
        SpeciesEntity e = new SpeciesEntity();
        Gson gson = new Gson();

        e.key = dto.key;
        e.name = dto.name;
        e.desc = dto.desc;
        e.isSubspecies = dto.isSubspecies;

        if (dto.subspeciesOf != null && !dto.subspeciesOf.isEmpty()) {
            String url = dto.subspeciesOf.endsWith("/")
                    ? dto.subspeciesOf.substring(0, dto.subspeciesOf.length() - 1)
                    : dto.subspeciesOf;
            String[] parts = url.split("/");
            e.subspeciesOf = parts[parts.length - 1];
        }

        if (dto.traits != null) {
            List<SpeciesDto.SpeciesTraitDto> sorted = dto.traits.stream()
                    .sorted((a, b) -> {
                        if (a.order == null && b.order == null) return 0;
                        if (a.order == null) return 1;
                        if (b.order == null) return -1;
                        return Integer.compare(a.order, b.order);
                    })
                    .collect(Collectors.toList());
            e.traitsJson = gson.toJson(sorted);
        }

        if (dto.document != null) {
            e.documentName = dto.document.name;
            e.documentKey = dto.document.key;
        }

        return e;
    }
}
