package org.grupolys.dictionary;

import lombok.Data;
import org.grupolys.profiles.PartOfSpeech;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Data
public class WordProperties implements Serializable {

    Boolean negating = false;

    Map<WordType, WordTypeValue> values = new HashMap<WordType, WordTypeValue>() {
        {
            Arrays.stream(PartOfSpeech.values()).forEach(partOfSpeech -> {
                put(partOfSpeech, new WordTypeValue());
            });
        }
    };
}