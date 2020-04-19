package org.grupolys.dictionary;

import lombok.Data;

import java.io.Serializable;

@Data
public class WordTypeValue implements Serializable {
    Double value;
    String lemma;
    public WordTypeValue() {
        this(null, null);
    }
    public WordTypeValue(Double value, String lemma) {
        this.value = value;
        this.lemma = lemma;
    }
    public WordTypeValue(Double value) {
        this(value, null);
    }
}
