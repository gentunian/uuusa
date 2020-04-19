package org.grupolys.dictionary;

import org.grupolys.dictionary.exceptions.InvalidWordException;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.samulan.util.dictionary.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WordsDictionary extends Dictionary {

    boolean hasWordOfType(String word, WordType type);
    List<DictionaryWord> getWords();
    DictionaryWord getWord(String word);
    Map<String, DictionaryWord> getWordsByType(WordType type);
    DictionaryWord addWord(DictionaryWord word) throws InvalidWordException;
    float getWordValue(String word, PartOfSpeech partOfSpeech, boolean includeLemma);

    String getLanguage();
    void setLanguage(String language);

    @Deprecated
    default Map<String, Float> getValues() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default void setValues(Map<String, Float> values) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default Map<String, Map<String, Float>> getClassValues() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default void setClassValues(Map<String, Map<String, Float>> classValues) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default Map<String, String> getLemmas() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default void setLemmas(Map<String, String> lemmas) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default Map<String, Map<String, String>> getClassLemmas() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    @Deprecated
    default void setClassLemmas(Map<String, Map<String, String>> classLemmas) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    @Deprecated
    default Map<String, Float> getEmoticons() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    @Deprecated
    default void setEmoticons(Map<String, Float> emoticons) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    default Set<String> getAdversativeWords() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default void setAdversativeWords(Set<String> adversativeWords) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default Set<String> getAdverbsIntensifiers() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    default void setAdverbsIntensifiers(Set<String> adverbsIntensifiers) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    @Deprecated
    default Set<String> getNegatingWords() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    @Deprecated
    default void setNegatingWords(Set<String> negatingWords) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    @Deprecated
    default String getLemma(String postag, String word) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default String getStrippedLemma(String postag, String word) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default void setClassEmotionDict(boolean classEmotionDict) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default boolean getClassEmotionDict() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default float getValue(String classValue, String lemma, boolean relaxed) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default boolean isWeight(String lemma) {
        DictionaryWord w = getWord(lemma);
        return w != null && w.isBooster();
//        String methodName = new Throwable().getStackTrace()[0].getMethodName();
//        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default void addWordsValues(WordsValues values) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    @Deprecated
    default void addLemmasValues(LemmasValues values) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
    
    @Deprecated
    default void addClassValues(ClassValues values) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }

    @Deprecated
    default void addClassLemmasValues(ClassLemmasValues values) {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException("Method not supported: You should not call this method:" + methodName);
    }
}
