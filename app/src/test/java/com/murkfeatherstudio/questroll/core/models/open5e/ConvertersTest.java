package com.murkfeatherstudio.questroll.core.models.open5e;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import com.murkfeatherstudio.questroll.core.models.open5e.character_class.gained_at.GainedAtDto;

public class ConvertersTest {

    @Test
    public void stringListConversion_isCorrect() {
        List<String> list = Arrays.asList("Common", "Elvish");
        String json = Converters.fromList(list);
        
        List<String> result = Converters.toList(json);
        assertEquals(2, result.size());
        assertEquals("Common", result.get(0));
        assertEquals("Elvish", result.get(1));
    }

    @Test
    public void integerListConversion_isCorrect() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        String json = Converters.fromIntegerList(list);
        
        List<Integer> result = Converters.toIntegerList(json);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(2), result.get(1));
    }

    @Test
    public void gainedAtListConversion_isCorrect() {
        GainedAtDto dto = new GainedAtDto();
        dto.level = 1;
        List<GainedAtDto> list = Arrays.asList(dto);
        
        String json = Converters.gainedAtListToJson(list);
        List<GainedAtDto> result = Converters.gainedAtListFromJson(json);
        
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).level);
    }

    @Test
    public void handlesNullInput() {
        assertTrue(Converters.toList(null).isEmpty());
        assertTrue(Converters.toIntegerList(null).isEmpty());
        assertTrue(Converters.gainedAtListFromJson(null).isEmpty());
    }
}
