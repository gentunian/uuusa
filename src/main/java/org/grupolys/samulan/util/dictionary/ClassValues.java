package org.grupolys.samulan.util.dictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClassValues implements DictionaryValues<Map<String, Map<String, Float>>, Float> {

    private Map<String, Map<String, Float>> classValues = new HashMap<>();

    @Override
    public void mergeValues(Map<String, Map<String, Float>> values) {
        classValues.putAll(values);
    }

    @Override
    public Map<String, Map<String, Float>> getValues() {
        return Collections.unmodifiableMap(classValues);
    }

    @Override
    public void setValues(Map<String, Map<String, Float>> values) {
        classValues = new HashMap<>(values);
    }

    @Override
    public void setValue(String classValue, String word, Float value) {
        Map<String, Float> values = classValues.get(classValue);
        if (values == null) {
            values = new HashMap<>();
        }

        values.put(word, value);
        classValues.put(classValue, values);
    }

    @Override
    public Float getValue(String classValue, String word) {
        Float value = null;
        Map<String, Float> valuesMap = classValues.get(classValue);

        if (valuesMap != null) {
            value = valuesMap.get(word);
        }

        return value;
    }
}
