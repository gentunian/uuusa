package org.grupolys.dictionary;

import org.apache.commons.lang.NullArgumentException;
import org.grupolys.profiles.PartOfSpeech;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Word implements DictionaryWord {

    private static final String NO_PART_OF_SPEECH = "nopostag";
    private String word;
    private final Map<String, Float> values = new HashMap<>();
    private final Map<String, DictionaryWord> lemmas = new HashMap<>();

    public Word(String word) {
        this(word, 0);
    }

    public Word(String word, float value) {
        this(word, null, value);
    }

    public Word(String word, DictionaryWord lemma) {
        this(word, lemma, 0);
    }

    public Word(String word, DictionaryWord lemma, float value) {
        this(word, lemma, value, (String) null);
    }

    public Word(String word, DictionaryWord lemma, float value, String partOfSpeech) {
        this(word, lemma, value, PartOfSpeech.getPartOfSpeech(partOfSpeech));
    }

    public Word(String word, DictionaryWord lemma, float value, PartOfSpeech partOfSpeech) {
        if (word == null) {
            throw new NullArgumentException("word shouldn't be null.");
        }
        this.word = word;
        addValue(partOfSpeech, value);
        addLemma(partOfSpeech, lemma);
    }

    public void addLemma(PartOfSpeech partOfSpeech, DictionaryWord lemma) {
        // gets part of speech to set value and lemma
        String pos = partOfSpeech != null ?  partOfSpeech.name() : NO_PART_OF_SPEECH;

        // tries to set the lemma for this word and `pos`
        try {
            lemmas.put(pos, lemma);
        } catch (NullArgumentException e) {
            // do nothing if lemma is null.
        }
    }

    @Override
    public DictionaryWord getLemma(PartOfSpeech partOfSpeech) {
        return lemmas.get(partOfSpeech.name());
    }

    @Override
    public String getWord() {
        return word;
    }

    @Override
    public void addValue(PartOfSpeech partOfSpeech, float value) {
        String pos = partOfSpeech != null ?  partOfSpeech.name() : NO_PART_OF_SPEECH;
        values.put(pos, value);
    }

    @Override
    public Float getValue() {
        return values.get(NO_PART_OF_SPEECH);
    }

    @Override
    public Float getValue(PartOfSpeech partOfSpeech) {
        return partOfSpeech != null ? values.get(partOfSpeech.name()) : getValue();
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
    public Set<String> getAllPartOfSpeech() {
        return values.keySet().stream().filter(item -> !item.equals(NO_PART_OF_SPEECH)).collect(Collectors.toSet());
    }
}
