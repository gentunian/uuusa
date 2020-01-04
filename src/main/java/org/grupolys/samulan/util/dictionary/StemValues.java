package org.grupolys.samulan.util.dictionary;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class StemValues implements DictionaryValues<Map<String, Float>, Float> {

    private Map<String, Float> stemValues = new TreeMap<>((s1, s2) -> {
        if (s1.length() > s2.length()) {
            return -1;
        } else if (s1.length() < s2.length()) {
            return 1;
        } else {
            return s1.compareTo(s2);
        }
    });

    @Override
    public void mergeValues(Map<String, Float> values) {
        stemValues.putAll(values);
    }

    @Override
    public Map<String, Float> getValues() {
        return Collections.unmodifiableMap(stemValues);
    }

    @Override
    public void setValues(Map<String, Float> values) {
        stemValues = new TreeMap<>(values);
    }

    @Override
    public void setValue(String classValue, String word, Float value) {
        stemValues.put(word, value);
    }

    @Override
    public Float getValue(String classValue, String word) {
        Float value = null;
        Iterator<String> it = stemValues.keySet().iterator();
        while(it.hasNext() && value == null) {
            String stem = it.next();
            if (word.startsWith(stem)) {
                value = stemValues.get(stem);
            }
        }
        return value;
    }
}
