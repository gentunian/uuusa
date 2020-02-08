package org.grupolys.dictionary;

import org.grupolys.profiles.PartOfSpeech;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractWordsDictionary implements WordsDictionary {

    private final Map<String, DictionaryWord> words = new HashMap<>();
    private final String language;

    public AbstractWordsDictionary(String language) {
        this.language = language;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("language", language);
        map.put("words", words);
        return map;
    }

    @Override
    public String getLanguage() {
        return this.language;
    }

    @Override
    public DictionaryWord getWord(String word) {
        return words.get(word);
    }

    @Override
    public DictionaryWord addWord(String word) {
        return this.addWord(word, 0);
    }

    @Override
    public DictionaryWord addWord(String word, float weight) {
        return this.addWord(word, null, weight, null);
    }

    @Override
    public DictionaryWord addWord(String word, String lemma, float weight) {
        return this.addWord(word, lemma, weight, null);
    }

    @Override
    public DictionaryWord addWord(String word, float weight, PartOfSpeech partOfSpeech) {
        return this.addWord(word, null, weight, partOfSpeech);
    }

    @Override
    public DictionaryWord addWord(String word, String lemma, float weight, PartOfSpeech partOfSpeech) {
        // the lemma should be also a word of this dictionary
        DictionaryWord dictionaryLemma = getWord(lemma);

        // is lemma is not a word of this dictionary then add it
        if (dictionaryLemma == null && lemma != null) {
            dictionaryLemma = new Word(lemma);
            words.put(lemma, dictionaryLemma);
        }

        DictionaryWord dictionaryWord = getWord(word);
        if (dictionaryWord == null) {
            dictionaryWord = new Word(word, dictionaryLemma, weight, partOfSpeech);
        } else {
            dictionaryWord.addValue(partOfSpeech, weight);
            dictionaryWord.addLemma(partOfSpeech, dictionaryLemma);
        }

        return words.put(word, dictionaryWord);
    }

    @Override
    public float getWordValue(String word, PartOfSpeech partOfSpeech) {
        Float value = null;
        DictionaryWord dictionaryWord = this.words.get(word);

        if (dictionaryWord != null) {
            value = dictionaryWord.getOptimisticValue(partOfSpeech);
        }

        return value == null ? 0 : value;
    }

    @Override
    public float getWordValue(DictionaryWord word, PartOfSpeech partOfSpeech) {
        Float value = null;

        if (word != null) {
            value = word.getOptimisticValue(partOfSpeech);
        }

        return value == null ? 0 : value;
    }
}
