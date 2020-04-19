package org.grupolys.dictionary;

import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.samulan.util.dictionary.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultDictionary implements Dictionary, Serializable {

    // used for returning empty sets for deprecated methods
    private Set<String> emptySet = new HashSet<>();

    private Map<String, WordProperties> words;

    public DefaultDictionary() {
        this.words = new HashMap<>();
    }

    public void addWord(String word, WordProperties properties) {
        this.words.put(word, properties);
    }

    @Override
    public float getValue(String classValue, String lemma, boolean relaxed) {
        PartOfSpeech partOfSpeech = PartOfSpeech.getPartOfSpeech(classValue);
        WordProperties properties = this.words.get(lemma);
        if (properties == null) {
            return 0;
        }

        Double value;

        if (properties.values.get(DefaultWordType.EMOTICON) != null) {
            value = properties.values.get(DefaultWordType.EMOTICON).value;
            if (value != null) {
                return value.floatValue();
            }
        }

        value = properties.values.get(partOfSpeech).value;
        return value != null ? value.floatValue() : 0f;
    }

    @Override
    public String getLemma(String postag, String word) {
        String lemma = word;
        PartOfSpeech partOfSpeech = PartOfSpeech.getPartOfSpeech(postag);
        WordProperties properties = this.words.get(word);
        if (properties != null) {
            lemma = properties.values.get(partOfSpeech).lemma;
        }
        return lemma == null ? word : lemma;
    }

    @Override
    public Set<String> getNegatingWords() {
        Set<String> negatingWords = new HashSet<>();
        this.words.forEach((word, values) -> {
            if (values.negating) {
                negatingWords.add(word);
            }
        });
        return negatingWords;
    }

    @Override
    public Set<String> getBoosterWords() {
        Set<String> boosterWords = new HashSet<>();
        this.words.forEach((word, values) -> {
            if (values.values.get(DefaultWordType.BOOSTER) != null) {
                boosterWords.add(word);
            }
        });
        return boosterWords;
    }

    @Override
    public float getBoosterValue(String word) {
        WordProperties properties = this.words.get(word);
        if (properties == null) {
            return 0f;
        }
        WordTypeValue value = properties.values.get(DefaultWordType.BOOSTER);
        if (value == null || value.value == null) {
            return 0f;
        }
        return value.value.floatValue();
    }

    @Override
    public boolean isWeight(String lemma) {
        WordProperties properties = this.words.get(lemma);
        if (properties == null) {
            return false;
        }
        return properties.values.get(DefaultWordType.BOOSTER) != null;
    }

    @Override
    public String getStrippedLemma(String postag, String word) {
        return word;
    }

    @Override
    public void setNegatingWords(Set<String> negatingWords) {
        throw new NotImplementedException();
    }

    @Override
    public void addWordsValues(WordsValues values) {
        throw new NotImplementedException();
    }

    @Override
    public void addLemmasValues(LemmasValues values) {
        throw new NotImplementedException();
    }

    @Override
    public void addClassValues(ClassValues values) {
        throw new NotImplementedException();
    }

    @Override
    public void addClassLemmasValues(ClassLemmasValues values) {
        throw new NotImplementedException();
    }


    @Override
    public void setClassEmotionDict(boolean classEmotionDict) {
        throw new NotImplementedException();
    }

    @Override
    public boolean getClassEmotionDict() {
        return true;
    }

    @Override
    public Map<String, Float> getValues() {
        return null;
    }

    @Override
    public void setValues(Map<String, Float> values) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, Map<String, Float>> getClassValues() {
        return null;
    }

    @Override
    public void setClassValues(Map<String, Map<String, Float>> classValues) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, String> getLemmas() {
        return null;
    }

    @Override
    public void setLemmas(Map<String, String> lemmas) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, Map<String, String>> getClassLemmas() {
        return null;
    }

    @Override
    public void setClassLemmas(Map<String, Map<String, String>> classLemmas) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, Float> getEmoticons() {
        return null;
    }

    @Override
    public void setEmoticons(Map<String, Float> emoticons) {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> getAdversativeWords() {
        return emptySet;
    }

    @Override
    public void setAdversativeWords(Set<String> adversativeWords) {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> getAdverbsIntensifiers() {
        return emptySet;
    }

    @Override
    public void setAdverbsIntensifiers(Set<String> adverbsIntensifiers) {
        throw new NotImplementedException();
    }
}
