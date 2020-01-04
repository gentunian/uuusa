package org.grupolys.samulan.util.dictionary;

public interface DictionaryValues<T, V> {

    void mergeValues(T values);
    T getValues();
    void setValues(T values);
    void setValue(String classValue, String word, V value);
    V getValue(String classValue, String word);
}
