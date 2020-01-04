package org.grupolys.samulan.util.dictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LemmasValues implements DictionaryValues<Map<String, String>, String> {

    private Map<String, String> lemmasValues = new HashMap<>();

    @Override
    public void mergeValues(Map<String, String> values) {
        lemmasValues.putAll(values);
    }

    @Override
    public Map<String, String> getValues() {
        return Collections.unmodifiableMap(lemmasValues);
    }

    @Override
    public void setValues(Map<String, String> values) {
        lemmasValues = new HashMap<>(values);
    }

    @Override
    public void setValue(String classValue, String word, String value) {
        lemmasValues.put(word, value);
    }

    @Override
    public String getValue(String classValue, String word) {
        return lemmasValues.get(word);
    }
}
