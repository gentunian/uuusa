package org.grupolys.samulan.util.dictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClassLemmasValues implements DictionaryValues<Map<String, Map<String, String>>, String> {

    private Map<String, Map<String, String>> classLemmasValues = new HashMap<>();

    @Override
    public void mergeValues(Map<String, Map<String, String>> values) {
        classLemmasValues.putAll(values);
    }

    @Override
    public Map<String, Map<String, String>> getValues() {
        return Collections.unmodifiableMap(classLemmasValues);
    }

    @Override
    public void setValues(Map<String, Map<String, String>> values) {
        classLemmasValues = new HashMap<>(values);
    }

    @Override
    public void setValue(String classValue, String word, String value) {
        Map<String, String> values = classLemmasValues.get(classValue);
        if (values == null) {
            values = new HashMap<>();
        }

        values.put(word, value);
        classLemmasValues.put(classValue, values);
    }

    @Override
    public String getValue(String classValue, String word) {
        String lemma = null;
        Map<String, String> mapWordsLemmas = classLemmasValues.get(classValue);
        if (mapWordsLemmas != null) {
            lemma = mapWordsLemmas.get(word);
        }
        return lemma;
    }
}
