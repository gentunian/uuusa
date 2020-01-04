package org.grupolys.samulan.util.dictionary;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class AdverbsIntensifiersValues implements DictionaryValues<Set<String>, String> {

    private Set<String> adverbsIntensifiersValues = new TreeSet<>();

    @Override
    public void mergeValues(Set<String> values) {
        adverbsIntensifiersValues.addAll(values);
    }

    @Override
    public Set<String> getValues() {
        return Collections.unmodifiableSet(adverbsIntensifiersValues);
    }

    @Override
    public void setValues(Set<String> values) {
        adverbsIntensifiersValues = new TreeSet<>(values);
    }

    @Override
    public void setValue(String classValue, String word, String value) {
        adverbsIntensifiersValues.add(word);
    }

    @Override
    public String getValue(String classValue, String word) {
        return adverbsIntensifiersValues.contains(word) ? word : null;
    }
}
