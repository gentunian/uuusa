package org.grupolys.samulan.util.dictionary;


import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class AdversativeWordsValues implements DictionaryValues<Set<String>, String> {

    private Set<String> adversativeWordsValues = new TreeSet<>();

    @Override
    public void mergeValues(Set<String> values) {
        adversativeWordsValues.addAll(values);
    }

    @Override
    public Set<String> getValues() {
        return Collections.unmodifiableSet(adversativeWordsValues);
    }

    @Override
    public void setValues(Set<String> values) {
        adversativeWordsValues = new TreeSet<>(values);
    }

    @Override
    public void setValue(String classValue, String word, String value) {
        adversativeWordsValues.add(word);
    }

    @Override
    public String getValue(String classValue, String word) {
        return adversativeWordsValues.contains(word) ? word : null;
    }
}
