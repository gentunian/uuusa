package org.grupolys.dictionary;

import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.samulan.util.dictionary.*;

import java.util.Map;
import java.util.Set;

public interface WordsDictionary extends Dictionary {

    DictionaryWord getWord(String word);
    DictionaryWord addWord(String word);
    DictionaryWord addWord(String word, float weight);
    DictionaryWord addWord(String word, String lemma, float weight);
    DictionaryWord addWord(String word, float weight, PartOfSpeech partOfSpeech);
    DictionaryWord addWord(String word, String lemma, float weight, PartOfSpeech partOfSpeech);
    float getWordValue(String word, PartOfSpeech partOfSpeech);
    float getWordValue(DictionaryWord word, PartOfSpeech partOfSpeech);
    String getLanguage();
    Map<String, Object> toMap();

    @Deprecated
    default Map<String, Float> getValues() {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default void setValues(Map<String, Float> values) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default Map<String, Map<String, Float>> getClassValues() {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default void setClassValues(Map<String, Map<String, Float>> classValues) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default Map<String, String> getLemmas() {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default void setLemmas(Map<String, String> lemmas) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default Map<String, Map<String, String>> getClassLemmas() {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    @Deprecated
    default void setClassLemmas(Map<String, Map<String, String>> classLemmas) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    @Deprecated
    default Map<String, Float> getEmoticons() {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    @Deprecated
    default void setEmoticons(Map<String, Float> emoticons) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    default Set<String> getAdversativeWords() {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default void setAdversativeWords(Set<String> adversativeWords) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default Set<String> getAdverbsIntensifiers() {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    default void setAdverbsIntensifiers(Set<String> adverbsIntensifiers) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    @Deprecated
    default Set<String> getNegatingWords() {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    @Deprecated
    default void setNegatingWords(Set<String> negatingWords) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    @Deprecated
    default String getLemma(String postag, String word) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default String getStrippedLemma(String postag, String word) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default void setClassEmotionDict(boolean classEmotionDict) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default boolean getClassEmotionDict() {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default float getValue(String classValue, String lemma, boolean relaxed) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default boolean isWeight(String lemma) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default void addWordsValues(WordsValues values) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    @Deprecated
    default void addLemmasValues(LemmasValues values) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
    
    @Deprecated
    default void addClassValues(ClassValues values) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }

    @Deprecated
    default void addClassLemmasValues(ClassLemmasValues values) {
        throw new UnsupportedOperationException("Method not supported: You should not call this method.");
    }
}
