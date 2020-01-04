package org.grupolys.samulan.util.dictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LemmaStrippersValues implements DictionaryValues<Map<String, List<String>>, List<String>> {

    private Map<String, List<String>> lemmaStrippersValues = new HashMap<>();

    @Override
    public void mergeValues(Map<String, List<String>> values) {
        lemmaStrippersValues.putAll(values);
    }

    @Override
    public Map<String, List<String>> getValues() {
        return Collections.unmodifiableMap(lemmaStrippersValues);
    }

    @Override
    public void setValues(Map<String, List<String>> values) {
        lemmaStrippersValues = new HashMap<>(values);
    }

    @Override
    public void setValue(String classValue, String word, List<String> value) {
        lemmaStrippersValues.put(word, value);
    }

    @Override
    public List<String> getValue(String classValue, String word) {
        return lemmaStrippersValues.get(word);
    }
}
