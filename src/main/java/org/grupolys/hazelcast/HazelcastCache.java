package org.grupolys.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.grupolys.dictionary.DefaultDictionary;
import org.grupolys.dictionary.DefaultWordType;
import org.grupolys.dictionary.WordProperties;
import org.grupolys.dictionary.WordTypeValue;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.grupolys.spring.repositories.WordsRepository;
import org.grupolys.spring.service.SamulanRulesService;
import org.grupolys.spring.service.WordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class HazelcastCache {

    interface AtomicDictionaryChange {
        DefaultDictionary doChange(DefaultDictionary dictionary);
    }

    public static final String PROFILE_DATA = "profileData";
    private final HazelcastInstance hazelcastInstance;
    private final WordsService wordsService;

    @Autowired
    HazelcastCache(HazelcastInstance hazelcastInstance,
                   HazelcastListener hzl,
                   WordsService wordsService) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        map.addEntryListener(hzl, true);
        Set<String> dictionaries = wordsService.getDictionaries();
        for (String dictionaryId: dictionaries) {
            DefaultDictionary dictionary = map.get(dictionaryId);
            if (dictionary == null) {
                dictionary = new DefaultDictionary();
            }
            List<PersistentWord> words = wordsService.getWordsFromDictionary(dictionaryId);
            for (PersistentWord word: words) {
                addWordToDictionary(word, dictionary);
            }
            map.lock(dictionaryId);
            map.put(dictionaryId, dictionary);
            map.unlock(dictionaryId);
        }
        this.hazelcastInstance = hazelcastInstance;
        this.wordsService = wordsService;
    }

    /**
     * Locks an entry for a specific dictionary to load words on batch.
     * @param dictionaryId the id of the dictionary to lock.
     * @param words the list of words to add to the locked dictionary.
     */
    public void lockAndReloadDictionary(String dictionaryId, List<PersistentWord> words) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        DefaultDictionary dictionary = map.get(dictionaryId);
        if (dictionary != null) {
            try {
                map.lock(dictionaryId);
                for (PersistentWord word : words) {
                    addWordToDictionary(word, dictionary);
                }
                map.put(dictionaryId, dictionary);
            }
            finally{
                map.unlock(dictionaryId);
            }
        }
    }

    /**
     * This method notifies listeners that a dictionary has changed.
     * @param dictionaryId the id of the dictionary that changed.
     */
    public void onDictionaryCreated(String dictionaryId) {
        DefaultDictionary dictionary = new DefaultDictionary();
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        map.lock(dictionaryId);
        map.putIfAbsent(dictionaryId, dictionary);
        map.unlock(dictionaryId);
    }

    /**
     * This method adds a word to a dictionary and notifies listeners that the dictionary has changed.
     * @param word the word to be added to the dictionary it belongs.
     */
    public void onWordAdded(PersistentWord word) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        String dictionaryId = word.getDictionary();
        DefaultDictionary dictionary = map.get(dictionaryId);
        if (dictionary == null) {
            dictionary = new DefaultDictionary();
        }
        addWordToDictionary(word, dictionary);
        map.lock(dictionaryId);
        map.put(dictionaryId, dictionary);
        map.unlock(dictionaryId);
    }

    /**
     * Notifies listeners that a word has been updated.
     * @param word the word to be updated to its dictionary.
     */
    public void onWordUpdated(PersistentWord word) {
        this.onWordAdded(word);
    }

    /**
     * Locks a dictionary to change it atomically via `change` object.
     * @param dictionaryId the id of the dictionary to be locked.
     * @param change the change object to apply to the dictionary.
     */
    public void onDictionaryChanged(String dictionaryId, AtomicDictionaryChange change) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        DefaultDictionary dictionary = map.get(dictionaryId);
        if (dictionary != null) {
            try {
                map.lock(dictionaryId);
                map.put(dictionaryId, change.doChange(dictionary));
            } finally {
                map.unlock(dictionaryId);
            }
        }
    }

    /**
     * Helper method that adds a word to a dictionary.
     */
    private void addWordToDictionary(PersistentWord word, DefaultDictionary dictionary) {
        WordProperties properties = new WordProperties();
        for (PartOfSpeech partOfSpeech: word.getPartOfSpeech().keySet()) {
            WordTypeValue value = new WordTypeValue(word.getValue(partOfSpeech),word.getLemma(partOfSpeech));
            properties.getValues().put(partOfSpeech, value);
        }
        if (word.getBooster() != 0) {
            properties.getValues().put(DefaultWordType.BOOSTER, new WordTypeValue(word.getBooster()));
        }
        if (word.getEmoticon() != 0) {
            properties.getValues().put(DefaultWordType.EMOTICON, new WordTypeValue(word.getEmoticon()));
        }
        properties.setNegating(word.getNegating());
        dictionary.addWord(word.getWord(), properties);
    }
}
