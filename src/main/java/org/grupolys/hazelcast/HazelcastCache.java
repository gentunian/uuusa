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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class HazelcastCache {

    public static final String PROFILE_DATA = "profileData";
    private final HazelcastInstance hazelcastInstance;

    private final WordsRepository wordsRepository;

    @Autowired
    HazelcastCache(HazelcastInstance hazelcastInstance,
                   HazelcastListener hzl,
                   WordsRepository wordsRepository) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        map.addEntryListener(hzl, true);
        Set<String> dictionaries = wordsRepository.findDictionaries();
        for (String dictionaryId: dictionaries) {
            DefaultDictionary dictionary = map.get(dictionaryId);
            if (dictionary == null) {
                dictionary = new DefaultDictionary();
            }
            List<PersistentWord> words = wordsRepository.findAllByDictionary(dictionaryId);
            for (PersistentWord word: words) {
                addWordToDictionary(word, dictionary);
            }
            map.lock(dictionaryId);
            map.put(dictionaryId, dictionary);
            map.unlock(dictionaryId);
        }
        this.hazelcastInstance = hazelcastInstance;
        this.wordsRepository = wordsRepository;
    }

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

    public void onDictionaryCreated(String dictionaryId) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        map.lock(dictionaryId);
        map.putIfAbsent(dictionaryId, new DefaultDictionary());
        map.unlock(dictionaryId);
    }

    public void onWordAdded(PersistentWord word) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        String dictionaryId = word.getDictionary();
        DefaultDictionary dictionary = map.get(dictionaryId);
        if (dictionary != null) {
            addWordToDictionary(word, dictionary);
            map.lock(dictionaryId);
            map.put(dictionaryId, dictionary);
            map.unlock(dictionaryId);
        }
    }

    public void onWordUpdated(PersistentWord word) {
        this.onWordAdded(word);
    }
}
