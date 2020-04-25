package org.grupolys.samulan.util.dictionary;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * This interface should be a replace for the Dictionary class to be deprecated.
 *
 * First iteration will consists in simply extract of all methods into an interface.
 *
 * Most advanced refactoring will consist in major and breaking changes.
 */
public interface Dictionary extends Serializable {

    /* BEGIN OF GETTERS AND SETTERS SECTION */
    Map<String, Float> getValues();
    void setValues(Map<String, Float> values);

    Map<String, Map<String, Float>> getClassValues();
    void setClassValues(Map<String, Map<String, Float>> classValues);

    Map<String, String> getLemmas();
    void setLemmas(Map<String, String> lemmas);

    Map<String, Map<String, String>> getClassLemmas();
    void setClassLemmas(Map<String, Map<String, String>> classLemmas);

    Map<String, Float> getEmoticons();
    void setEmoticons(Map<String, Float> emoticons);

    Set<String> getAdversativeWords();
    void setAdversativeWords(Set<String> adversativeWords);

    Set<String> getAdverbsIntensifiers();
    void setAdverbsIntensifiers(Set<String> adverbsIntensifiers);

    Set<String> getNegatingWords();
    void setNegatingWords(Set<String> negatingWords);

    String getLemma(String postag, String word);
    String getStrippedLemma(String postag, String word);

    void setClassEmotionDict(boolean classEmotionDict);
    boolean getClassEmotionDict();
    /* END OF GETTERS AND SETTERS SECTION */

    // this may be the most important and valued method to be implemented
    float getValue(String classValue, String lemma, boolean relaxed);

    boolean isWeight(String lemma);

    Set<String> getBoosterWords();
    float getBoosterValue(String word);
}
