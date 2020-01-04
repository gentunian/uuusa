package org.grupolys.samulan.util.dictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EmoticonsValues implements DictionaryValues<Map<String, Float>, Float> {

    private Map<String, Float> emoticonsValue = new HashMap<>();

    @Override
    public void mergeValues(Map<String, Float> values) {
        emoticonsValue.putAll(values);
    }

    @Override
    public Map<String, Float> getValues() {
        return Collections.unmodifiableMap(emoticonsValue);
    }

    @Override
    public void setValues(Map<String, Float> values) {
        emoticonsValue = new HashMap<>(values);
    }

    @Override
    public void setValue(String classValue, String word, Float value) {
        emoticonsValue.put(word, value);
    }

    @Override
    public Float getValue(String classValue, String word) {
        return emoticonsValue.get(word);
    }
}
