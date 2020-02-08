package org.grupolys.samulan.util.dictionary;

import org.grupolys.samulan.analyser.operation.Operation;

import java.util.*;

/**
 * this class is the first to be part of refactoring dictionary.
 *
 * Major and breaking changes will come.
 */
public abstract class AbstractDictionary implements Dictionary {

    private static final String VALUES = "values";
    private static final String CLASS_VALUES = "class.values";
    private static final String STEM_VALUES = "stem.values";
    private static final String CLASS_LEMMA_VALUES = "class.lemma.values";
    private static final String EMOTICONS_VALUES = "emoticons.values";
    private static final String NEGATING_WORDS_VALUES = "negating.words.values";
    private static final String ADVERSATIVE_WORDS_VALUES = "adversative.words.values";
    private static final String ADVERBS_INTENSIFIER_VALUES = "adverbs.intensifier.values";
    private static final String LEMMA_STRIPPERS_VALUES = "lemma.strippers.values";
    private static final String LEMMAS_VALUES = "lemma.values";
    private Map<String, DictionaryValues> dict;

    private WordsValues wordsValues = new WordsValues();
    private ClassValues classValues = new ClassValues();
    private StemValues stemValues = new StemValues();
    private ClassLemmasValues classLemmasValues = new ClassLemmasValues();
    private EmoticonsValues emoticonsValues = new EmoticonsValues();
    private NegatingWordsValues negatingWordsValues = new NegatingWordsValues();
    private AdverbsIntensifiersValues adverbsIntensifiersValues = new AdverbsIntensifiersValues();
    private AdversativeWordsValues adversativeWordsValues =  new AdversativeWordsValues();
    private LemmaStrippersValues lemmaStrippersValues = new LemmaStrippersValues();
    private LemmasValues lemmasValues = new LemmasValues();
    private boolean thereIsClassEmotionDict = false;

    public AbstractDictionary() {
        dict = new HashMap<>();
        dict.put(VALUES, new WordsValues());
        dict.put(CLASS_VALUES, new ClassValues());
        dict.put(STEM_VALUES, new StemValues());
        dict.put(CLASS_LEMMA_VALUES, new ClassLemmasValues());
        dict.put(EMOTICONS_VALUES, new EmoticonsValues());
        dict.put(NEGATING_WORDS_VALUES, new NegatingWordsValues());
        dict.put(ADVERSATIVE_WORDS_VALUES, new AdversativeWordsValues());
        dict.put(ADVERBS_INTENSIFIER_VALUES, new AdverbsIntensifiersValues());
        dict.put(LEMMA_STRIPPERS_VALUES, new LemmaStrippersValues());
        dict.put(LEMMAS_VALUES, new LemmasValues());
        this.thereIsClassEmotionDict = false;
    }

    public Map<String, Float> getValues() {
        return wordsValues.getValues();
    }

    public Map<String, Map<String, Float>> getClassValues() {
        return classValues.getValues();
    }

    public Map<String, String> getLemmas() {
        return lemmasValues.getValues();
    }

    public Map<String, Map<String, String>> getClassLemmas() {
        return classLemmasValues.getValues();
    }

    public Map<String, Float> getEmoticons() {
        return emoticonsValues.getValues();
    }

    public Set<String> getAdversativeWords() {
        return adversativeWordsValues.getValues();
    }

    public Set<String> getAdverbsIntensifiers() {
        return adverbsIntensifiersValues.getValues();
    }

    @Override
    public Set<String> getNegatingWords() {
        return negatingWordsValues.getValues();
    }

    @Override
    public boolean getClassEmotionDict() {
        return thereIsClassEmotionDict;
    }

    @Override
    public float getValue(String classValue, String lemma, boolean relaxed) {
        float value = 0;
        String lowerCaseLemma = lemma.toLowerCase();
        Float lemmaValue = classValues.getValue(classValue, lowerCaseLemma);

        if (lemmaValue == null && (relaxed || !thereIsClassEmotionDict)) {
            lemmaValue = wordsValues.getValue(null, lowerCaseLemma);
        }

        if (lemmaValue == null) {
            lemmaValue = stemValues.getValue(classValue, lowerCaseLemma);
        }

        if (lemmaValue == null) {
            lemmaValue = emoticonsValues.getValue(classValue, lowerCaseLemma);
        }

        if (lemmaValue != null) {
            value = lemmaValue;
        }

        return value;
    }

    @Override
    public String getLemma(String postag, String word) {
        String wordLowerCase = word.toLowerCase();
        String lemma = classLemmasValues.getValue(postag, word);

        if (lemma == null) {
            lemma = lemmasValues.getValue(postag, word);
        }

        if (lemma == null) {
            lemma = wordLowerCase;
        }

        return lemma;
    }

    @Override
    public String getStrippedLemma(String postag, String word) {
        String wordLowerCase = word.toLowerCase();
        String lemma = null;
        List<String> postagLemmasStrippers = lemmaStrippersValues.getValue(postag, word);
        if (postagLemmasStrippers != null) {
            Iterator<String> it = postagLemmasStrippers.iterator();

            while (it.hasNext() && lemma == null) {
                String pls = it.next();
                if (word.endsWith(pls)) {
                    lemma = word.substring(0, word.length() - pls.length());
                }
            }

        }

        if (lemma == null) {
            lemma = wordLowerCase;
        }

        return lemma;
    }

    @Override
    public void setValues(Map<String, Float> values) {
        wordsValues.setValues(values);
    }

    @Override
    public void setClassValues(Map<String, Map<String, Float>> classValues) {
        this.classValues.setValues(classValues);
    }

    @Override
    public void setLemmas(Map<String, String> lemmas) {
        lemmasValues.setValues(lemmas);
    }

    @Override
    public void setClassLemmas(Map<String, Map<String, String>> classLemmas) {
        classLemmasValues.setValues(classLemmas);
    }

    @Override
    public void setEmoticons(Map<String, Float> emoticons) {
        emoticonsValues.setValues(emoticons);
    }

    @Override
    public void setAdversativeWords(Set<String> adversativeWords) {
        adversativeWordsValues.setValues(adversativeWords);
    }

    @Override
    public void setAdverbsIntensifiers(Set<String> adverbsIntensifiers) {
        adverbsIntensifiersValues.setValues(adverbsIntensifiers);
    }

    @Override
    public void setNegatingWords(Set<String> negatingWords) {
        negatingWordsValues.setValues(negatingWords);
    }

    @Override
    public void setClassEmotionDict(boolean classEmotionDict) {
        thereIsClassEmotionDict = classEmotionDict;
    }

    @Override
    public boolean isWeight(String lemma) {
        return classValues.getValue(Operation.WEIGHT, lemma) != null;
    }

    @Override
    public void addWordsValues(WordsValues values) {
        wordsValues.mergeValues(values.getValues());
    }

    @Override
    public void addClassValues(ClassValues values) {
        classValues.mergeValues(values.getValues());
    }

    @Override
    public void addLemmasValues(LemmasValues values) {
        lemmasValues.mergeValues(values.getValues());
    }

    @Override
    public void addClassLemmasValues(ClassLemmasValues values) {
        classLemmasValues.mergeValues(values.getValues());
    }
}
