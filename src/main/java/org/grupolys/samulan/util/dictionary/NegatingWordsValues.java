package org.grupolys.samulan.util.dictionary;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class NegatingWordsValues implements DictionaryValues<Set<String>, String> {

    private Set<String> negatingWordsValues = new TreeSet<>();

    @Override
    public void mergeValues(Set<String> values) {
        negatingWordsValues.addAll(values);
    }

    @Override
    public Set<String> getValues() {
        return Collections.unmodifiableSet(negatingWordsValues);
    }

    @Override
    public void setValues(Set<String> values) {
        negatingWordsValues = new TreeSet<>(values);
    }

    @Override
    public void setValue(String classValue, String word, String value) {
        negatingWordsValues.add(word);
    }

    @Override
    public String getValue(String classValue, String word) {
        return negatingWordsValues.contains(word) ? word : null;
    }
}
