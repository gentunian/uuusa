package org.grupolys.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.grupolys.dictionary.DefaultWordsDictionary;
import org.grupolys.dictionary.DictionaryWord;
import org.grupolys.dictionary.Word;
import org.grupolys.dictionary.WordsDictionary;
import org.grupolys.dictionary.exceptions.InvalidWordException;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.grupolys.spring.repositories.ProfilesRepository;
import org.grupolys.spring.repositories.WordsRepository;
import org.grupolys.spring.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class HazelcastCache {

    public static final String PROFILE_DATA = "profileData";
    private final HazelcastInstance hazelcastInstance;

//    private final DictionaryProfileStore profileCreator;

    private final WordsRepository wordsRepository;
    private final ProfilesRepository profilesRepository;

    @Autowired
    HazelcastCache(HazelcastInstance hazelcastInstance,
                   HazelcastListener hzl,
                   ConfigService configService,
                   WordsRepository wordsRepository,
                   ProfilesRepository profilesRepository) {
        IMap<String, WordsDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        map.addEntryListener(hzl, true);
        addWords(wordsRepository.findAllFromActiveDictionaries(), map);
        this.hazelcastInstance = hazelcastInstance;
        this.profilesRepository = profilesRepository;
        this.wordsRepository = wordsRepository;
    }

    private void addWords(List<PersistentWord> words, IMap<String, WordsDictionary> map) {
        for(PersistentWord word: words) {
            String dictionaryId = word.getDictionary();
            WordsDictionary dictionary = map.get(dictionaryId);
            if (dictionary == null) {
                dictionary = new DefaultWordsDictionary();
            }
            try {
                addWordToDictionary(word, dictionary);
                map.lock(dictionaryId);
                map.put(dictionaryId, dictionary);
                map.unlock(dictionaryId);
            } catch (InvalidWordException e) {
                e.printStackTrace();
            }
        }
    }

    private void addWordToDictionary(PersistentWord word, WordsDictionary dictionary) throws InvalidWordException {
        PartOfSpeech partOfSpeech = PartOfSpeech.getPartOfSpeech(word.getPartOfSpeech());
        String lemma = word.getLemma();

        // the lemma should be also a word of this dictionary
        DictionaryWord dictionaryLemma = dictionary.getWord(lemma);

        // if lemma is not a word of this dictionary then add it
        if (dictionaryLemma == null && lemma != null) {
            dictionaryLemma = dictionary.addWord(new Word(lemma, null, 0, partOfSpeech));
        }

        dictionary.addWord(
                new Word(word.getWord(),
                        dictionaryLemma,
                        word.getValue().floatValue(),
                        partOfSpeech,
                        word.getNegating(),
                        word.getBooster().floatValue()
                )
        );
    }

    public void updateRules(String dictionary) {
        addWords(wordsRepository.findAllByDictionary(dictionary), hazelcastInstance.getMap(PROFILE_DATA));
    }

    public void onWordUpdate(PersistentWord word) {
        addWords(Collections.singletonList(word), hazelcastInstance.getMap(PROFILE_DATA));
    }

    public void onWordDelete(PersistentWord word) {
        String dictionaryId = word.getDictionary();
        IMap<String, WordsDictionary> map = hazelcastInstance.getMap(PROFILE_DATA);
        WordsDictionary dictionary = map.get(dictionaryId);
        if (dictionary != null) {
            PartOfSpeech partOfSpeech = PartOfSpeech.getPartOfSpeech(word.getPartOfSpeech());
            dictionary.removeWord(word.getWord(), partOfSpeech);
            map.lock(dictionaryId);
            map.put(dictionaryId, dictionary);
            map.unlock(dictionaryId);
        }
    }
}
