package org.grupolys.dictionary;

import org.grupolys.dictionary.exceptions.InvalidWordException;
import org.grupolys.profiles.PartOfSpeech;

import java.io.Serializable;
import java.util.*;

public class DefaultWordsDictionary implements WordsDictionary, Serializable {

    private final Map<String, DictionaryWord> words = new HashMap<>();
    private final Map<WordType, Map<String, DictionaryWord>> classifiedWords = new HashMap<>();

    private String language;

    @Override
    public List<DictionaryWord> getWords() {
        return new ArrayList<>(words.values());
    }

    @Override
    public String getLanguage() {
        return this.language;
    }

    @Override
    public void setLanguage(String language) {
        if (this.language != null) throw new IllegalStateException("Cannot change language already set.");
        this.language = language;
    }

    @Override
    public DictionaryWord getWord(String word) {
        return words.get(word);
    }

    @Override
    public Map<String, DictionaryWord> getWordsByType(WordType type) {
        return classifiedWords.get(type);
    }

    @Override
    public DictionaryWord addWord(DictionaryWord word) throws InvalidWordException {
        // Lemmas must exist previously in this dictionary before adding the word
        Map<PartOfSpeech, DictionaryWord> lemmas = word.getLemmas();
        for (Map.Entry<PartOfSpeech, DictionaryWord> entry: lemmas.entrySet()) {
            if (!hasWordOfType(entry.getValue().getWord(), entry.getKey())) {
                throw new InvalidWordException(entry.getKey() + " lemma '" + entry.getValue().getWord() +
                        "' did not exist in this dictionary.");
            }
        }

        final DictionaryWord existingWord = words
                .putIfAbsent(word.getWord(), word) == null? word : words.get(word.getWord());
        word.getAllPartOfSpeech().forEach(partOfSpeech -> {
            // Update word value for each partOfSpeech
            existingWord.addValue(word.getValue(partOfSpeech), partOfSpeech);
            addWordToClassifiedWords(existingWord, partOfSpeech);
        });
        if (word.isBooster()) {
            addWordToClassifiedWords(existingWord, DefaultWordType.BOOSTER);
        }
        if (word.isNegating()) {
            addWordToClassifiedWords(existingWord, DefaultWordType.NEGATING);
        }

        return existingWord;
    }

    @Override
    public DictionaryWord removeWord(String word, PartOfSpeech partOfSpeech) {
//        DictionaryWord dictionaryWord = getWord(word);
//        if (dictionaryWord != null) {
//            dictionaryWord.removeValue(partOfSpeech);
//            dictionaryWord.removeLemma(partOfSpeech);
//            if (dictionaryWord.getAllPartOfSpeech().size() == 0) {
//                words.remove(word);
//                removeFromClassifiedWords(dictionaryWord, DefaultWordType.BOOSTER);
//                removeFromClassifiedWords(dictionaryWord, DefaultWordType.NEGATING);
//                removeFromClassifiedWords(dictionaryWord, partOfSpeech);
//            }
//        }
//        return dictionaryWord;
        return null;
    }

    private void addWordToClassifiedWords(DictionaryWord word, WordType type) {
        Map<String, DictionaryWord> wordsSet = classifiedWords.get(type);
        if (wordsSet == null) {
            wordsSet = new HashMap<>();
        }
        wordsSet.put(word.getWord(), word);
        classifiedWords.put(type, wordsSet);
    }

    private void removeFromClassifiedWords(DictionaryWord word, WordType type) {
        Map<String, DictionaryWord> wordsByType = classifiedWords.get(type);
        if (wordsByType != null) {
            wordsByType.remove(word.getWord());
        }
    }

    @Override
    public boolean hasWordOfType(String word, WordType type) {
        Map<String, DictionaryWord> wordsByType = classifiedWords.get(type);
        if (wordsByType == null) {
            return false;
        }
        return wordsByType.get(word) != null;
    }

    @Override
    public float getWordValue(String wordName, PartOfSpeech partOfSpeech, boolean includeLemma) {
        Float value = null;
        DictionaryWord word = this.words.get(wordName);

        if (word != null) {
            value = includeLemma ? word.getOptimisticValue(partOfSpeech) : word.getValue(partOfSpeech);
        }

        return value == null ? 0 : value;
    }

    @Override
    public String getLemma(String postag, String word) {
        PartOfSpeech partOfSpeech = PartOfSpeech.getPartOfSpeech(postag);
        DictionaryWord aword = getWord(word);
        if (aword != null) {
            DictionaryWord alemma = aword.getLemma(partOfSpeech);
            if (alemma != null) {
                return alemma.getWord();
            }
        }
        return word;
    }

    @Override
    public float getValue(String classValue, String lemma, boolean relaxed) {
        PartOfSpeech partOfSpeech = PartOfSpeech.getPartOfSpeech(classValue);
        if (getWord(lemma) == null) {
            return 0;
        }
        return getWord(lemma).getValue(partOfSpeech);
    }

    @Override
    public Set<String> getAdverbsIntensifiers() {
        return new HashSet<>();
    }

    @Override
    public String getStrippedLemma(String postag, String word) {
        return word;
    }
}
