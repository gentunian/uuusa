package org.grupolys.dictionary;

import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.samulan.util.dictionary.Dictionary;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DictionaryWord models a simple word for a Dictionary.
 *
 * It should hold complete information about a word, such as,
 * part of speech, lemma, value weight for a specific part of speech, etc.
 *
 * A word is any String. That word may have one or many part of speech,
 * such as the word: 'white'. It may be noun, and have a correspondent value,
 * or it may be adjective and have a different value when used as adjective.
 */
public interface DictionaryWord {

    /**
     * Returns a DictionaryWord that is the lemma of this word based on the partOfSpeech.
     * @param partOfSpeech part of speech of the word we want to retrieve the lemma.
     * @return the word lemma for this word based on specific part of speech or null if it doesn't have a lemma.
     */
    DictionaryWord getLemma(PartOfSpeech partOfSpeech);
    Map<PartOfSpeech, DictionaryWord> getLemmas();

    /**
     * Returns the string this DictionaryWord represents.
     * @return the word string.
     */
    String getWord();

    /**
     * Adds a value for this DictionaryWord that the word should have when its part of speech is partOfSpeech.
     * @param value the value this word should have when its part of speech is partOfSpeech.
     * @param partOfSpeech part of speech that determines which role this word plays in a sentence.
     */
    Float addValue(float value, PartOfSpeech partOfSpeech);
    boolean removeValue(PartOfSpeech partOfSpeech);

    DictionaryWord addLemma(DictionaryWord lemma, PartOfSpeech partOfSpeech);
    boolean removeLemma(PartOfSpeech partOfSpeech);

    /**
     * Returns the value this word has when its part of speech in a sentence is partOfSpeech, or null if
     * partOfSpeech doesn't apply to this word.
     * @param partOfSpeech the part of speech we want to retrieve for this word.
     * @return the value this word has for partOfSpeech or null if it doesn't.
     */
    Float getValue(PartOfSpeech partOfSpeech);

    /**
     * Same as {@link #getValue(PartOfSpeech)} but for Word that has not been categorized with a part of speech.
     * @return the value this word has for partOfSpeech or null if it doesn't.
     */
    Float getValue();

    Map<PartOfSpeech, Float> getValues();

    /**
     * Returns an optimistic value for this word, that is, if a lemma is present then lemma should have precedence.
     * @param partOfSpeech the part of speech we want to retrieve for this word.
     * @return the value this word has for partOfSpeech with precedence for lemma or null if it doesn't.
     */
    Float getOptimisticValue(PartOfSpeech partOfSpeech);

    Set<PartOfSpeech> getAllPartOfSpeech();

    boolean isNegating();

    boolean isBooster();

    float getBoosterValue();
}
