package org.grupolys.samulan.util.dictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WordsValues implements DictionaryValues<Map<String, Float>, Float> {

    private Map<String, Float> wordsValues = new HashMap<>();

    @Override
    public void mergeValues(Map<String, Float> values) {
        wordsValues.putAll(values);
    }

    @Override
    public Map<String, Float> getValues() {
        return Collections.unmodifiableMap(wordsValues);
    }

    @Override
    public void setValues(Map<String, Float> values) {
        this.wordsValues = new HashMap<>(values);
    }

    @Override
    public void setValue(String classValue, String word, Float value) {
        wordsValues.put(word, value);
    }

    @Override
    public Float getValue(String classValue, String word) {
        return wordsValues.get(word);
    }
}
