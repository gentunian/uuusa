package org.grupolys.dictionary;

import org.grupolys.profiles.PartOfSpeech;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Word implements DictionaryWord, Serializable {

    private final Map<PartOfSpeech, Float> values = new HashMap<>();
    private final Map<PartOfSpeech, DictionaryWord> lemmas = new HashMap<>();
    private String word;
    private float booster = 1.0f;
    private boolean negating;

    public Word(String word) {
        this(word, 0);
    }

    public Word(String word, float value) throws IllegalArgumentException {
        this(word, null, value);
    }

    public Word(String word, DictionaryWord lemma) throws IllegalArgumentException {
        this(word, lemma, 0);
    }

    public Word(String word, DictionaryWord lemma, float value) throws IllegalArgumentException {
        this(word, lemma, value, null);
    }

    public Word(String word, DictionaryWord lemma, float value, PartOfSpeech partOfSpeech)
            throws IllegalArgumentException {
        this(word, lemma, value, partOfSpeech, false);
    }

    public Word(String word, DictionaryWord lemma, float value, PartOfSpeech partOfSpeech, boolean negating)
            throws IllegalArgumentException {
        this(word, lemma, value, partOfSpeech, negating, 1f);
    }

    public Word(String word, DictionaryWord lemma, float value, PartOfSpeech partOfSpeech, boolean negating,
                float booster) throws IllegalArgumentException {
        if (word == null || word.equals("")) {
            throw new IllegalArgumentException("word shouldn't be null nor empty.");
        }
        this.word = word;
        this.booster = booster;
        this.negating = negating;
        addLemma(lemma, partOfSpeech);
        addValue(value, partOfSpeech);
    }

    @Override
    public DictionaryWord addLemma(DictionaryWord lemma, PartOfSpeech partOfSpeech) {
        if (lemma != null) {
            lemmas.put(PartOfSpeech.getPartOfSpeech(partOfSpeech), lemma);
        }
        return lemma;
    }

    @Override
    public boolean removeLemma(PartOfSpeech partOfSpeech) {
        boolean found = getLemmas().get(partOfSpeech) != null;
        if (found) {
            lemmas.remove(partOfSpeech);
        }
        return found;
    }

    @Override
    public DictionaryWord getLemma(PartOfSpeech partOfSpeech) {
        return lemmas.get(PartOfSpeech.getPartOfSpeech(partOfSpeech));
    }

    @Override
    public Map<PartOfSpeech, DictionaryWord> getLemmas() {
        return Collections.unmodifiableMap(this.lemmas);
    }

    @Override
    public String getWord() {
        return word;
    }

    @Override
    public Float addValue(float value, PartOfSpeech partOfSpeech) {
        values.put(PartOfSpeech.getPartOfSpeech(partOfSpeech), value);
        return value;
    }

    @Override
    public boolean removeValue(PartOfSpeech partOfSpeech) {
        boolean found = getAllPartOfSpeech().contains(partOfSpeech);
        if (found) {
            values.remove(partOfSpeech);
        }
        return found;
    }

    @Override
    public Float getValue() {
        return values.get(PartOfSpeech.NOPOSTAG);
    }

    @Override
    public Map<PartOfSpeech, Float> getValues() {
        return values;
    }

    @Override
    public Float getValue(PartOfSpeech partOfSpeech) {
        return values.get(PartOfSpeech.getPartOfSpeech(partOfSpeech));
    }

    @Override
    public Float getOptimisticValue(PartOfSpeech partOfSpeech) {
        DictionaryWord word = getLemma(partOfSpeech);

        if (word != null) {
            Float value = word.getValue();
            if (value == null || value == 0) {
                value = word.getValue(partOfSpeech);
                if (value == null || value == 0) {
                    return getValue(partOfSpeech);
                } else {
                    return value;
                }
            } else {
                return value;
            }
        } else {
            return getValue(partOfSpeech);
        }
    }

    @Override
    public Set<PartOfSpeech> getAllPartOfSpeech() {
        return values.keySet();
    }

    @Override
    public boolean isNegating() {
        return this.negating;
    }

    @Override
    public boolean isBooster() {
        return this.booster != 1;
    }

    @Override
    public float getBoosterValue() {
        return booster;
    }
}
