package org.grupolys.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.grupolys.dictionary.*;
import org.grupolys.dictionary.exceptions.InvalidWordException;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.model.persistence.PersistentWord2;
import org.grupolys.spring.repositories.Words2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class HazelcastCache {

    public static final String PROFILE_DATA = "profileData";
    private final HazelcastInstance hazelcastInstance;

    private final Words2Repository wordsRepository;
//    private final ProfilesRepository profilesRepository;

    @Autowired
    HazelcastCache(HazelcastInstance hazelcastInstance,
                   HazelcastListener hzl,
                   Words2Repository wordsRepository) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        map.addEntryListener(hzl, true);
        Set<String> dictionaries = wordsRepository.findDictionaries();
        for (String dictionaryId: dictionaries) {
            DefaultDictionary dictionary = map.get(dictionaryId);
            if (dictionary == null) {
                dictionary = new DefaultDictionary();
            }
            List<PersistentWord2> words = wordsRepository.findAllByDictionary(dictionaryId);
            for (PersistentWord2 word: words) {
                addWordToDictionary2(word, dictionary);
            }
            map.lock(dictionaryId);
            map.put(dictionaryId, dictionary);
            map.unlock(dictionaryId);
        }
        this.hazelcastInstance = hazelcastInstance;
        this.wordsRepository = wordsRepository;
    }

    private void addWordToDictionary2(PersistentWord2 word, DefaultDictionary dictionary) {
        WordProperties properties = new WordProperties();
        for (PartOfSpeech partOfSpeech: word.getPartOfSpeech().keySet()) {
            WordTypeValue value = new WordTypeValue(word.getValue(partOfSpeech),word.getLemma(partOfSpeech));
            properties.getValues().put(partOfSpeech, value);
        }
        if (word.getBooster() != 0) {
            properties.getValues().put(DefaultWordType.BOOSTER, new WordTypeValue(word.getBooster()));
        }
        properties.setNegating(word.getNegating());
        dictionary.addWord(word.getWord(), properties);
    }

//    private void addWords(List<PersistentWord2> words, IMap<String, WordsDictionary> map) {
//        for(PersistentWord2 word: words) {
//            String dictionaryId = word.getDictionary();
//            WordsDictionary dictionary = map.get(dictionaryId);
//            try {
//                addWordToDictionary(word, dictionary);
//                map.lock(dictionaryId);
//                map.put(dictionaryId, dictionary);
//                map.unlock(dictionaryId);
//            } catch (InvalidWordException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void addWordToDictionary(PersistentWord2 word, WordsDictionary dictionary) throws InvalidWordException {
        for (PartOfSpeech partOfSpeech: word.getPartOfSpeech().keySet()) {
            String lemma = word.getLemma(partOfSpeech);

            // the lemma should be also a word of this dictionary
            DictionaryWord dictionaryLemma = dictionary.getWord(lemma);

            // if lemma is not a word of this dictionary then add it
            if (dictionaryLemma == null && lemma != null) {
                try {
                    dictionaryLemma = dictionary.addWord(new Word(lemma, null, 0, partOfSpeech));
                } catch (IllegalArgumentException e) {
                    throw new InvalidWordException("Add word '" + word.getWord() + "' to dictionary failed: " +
                            "Trying to add lemma '" + lemma + "' for word with id '" + word.getId() + "' was not possible.");
                }
            }

            dictionary.addWord(
                    new Word(word.getWord(),
                            dictionaryLemma,
                            word.getValue(partOfSpeech).floatValue(),
                            partOfSpeech,
                            word.getNegating(),
                            word.getBooster().floatValue()
                    )
            );
        }
    }

    public void onDictionaryCreated2(String dictionaryId) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        map.lock(dictionaryId);
        map.putIfAbsent(dictionaryId, new DefaultDictionary());
        map.unlock(dictionaryId);
    }

    public void onDictionaryCreated(String dictionaryId) {
        IMap<String, WordsDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        map.lock(dictionaryId);
        map.putIfAbsent(dictionaryId, new DefaultWordsDictionary());
        map.unlock(dictionaryId);
    }

    public void onWordAdded2(PersistentWord2 word) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        // TODO: String dictionaryId = word.getDictionary();
        String dictionaryId = word.getDictionary();
        DefaultDictionary dictionary = map.get(dictionaryId);
        if (dictionary != null) {
            addWordToDictionary2(word, dictionary);
            map.lock(dictionaryId);
            map.put(dictionaryId, dictionary);
            map.unlock(dictionaryId);
        }
    }

    public void onWordAdded(PersistentWord2 word) {
        IMap<String, WordsDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        // TODO: String dictionaryId = word.getDictionary();
        WordsDictionary dictionary = map.get(word.getDictionary());
        if (dictionary != null) {
            try {
                addWordToDictionary(word, dictionary);
                map.lock(word.getDictionary());
                map.put(word.getDictionary(), dictionary);
                map.unlock(word.getDictionary());
            } catch (InvalidWordException e) {
                System.out.println("Cannot add word '" + word.getWord() + "' to dictionary '" + word.getDictionary() + "'.");
                e.printStackTrace();
            }
        }
    }

    public void onWordUpdated2(PersistentWord2 word) {
        IMap<String, DefaultDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        String dictionaryId = word.getDictionary();
        DefaultDictionary dictionary = map.get(dictionaryId);
        if (dictionary != null) {
            addWordToDictionary2(word, dictionary);
            map.lock(dictionaryId);
            map.put(dictionaryId, dictionary);
            map.unlock(dictionaryId);
        }
    }

    public void onWordUpdated(PersistentWord2 word) {
        IMap<String, WordsDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        // TODO: String dictionaryId = word.getDictionary();
       WordsDictionary dictionary = map.get(word.getDictionary());
       DictionaryWord dictionaryWord = dictionary.getWord(word.getWord());
       if (dictionaryWord != null) {
           for (PartOfSpeech partOfSpeech: word.getPartOfSpeech().keySet()) {
               dictionaryWord.addValue(word.getValue(partOfSpeech).floatValue(), partOfSpeech);
           }
           if (word.getNegating() != null) {
               dictionaryWord.setNegating(word.getNegating());
           }
           if (word.getBooster() != null) {
               dictionaryWord.setBooster(word.getBooster());
           }
           map.lock(word.getDictionary());
           map.put(word.getDictionary(), dictionary);
           map.unlock(word.getDictionary());
       }
    }
}
